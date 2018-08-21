package caceresenzo.apps.boxplay.receivers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class YoutubeDownloadFinishedReceiver extends BroadcastReceiver {
	
	private static final String TEMP_FILE_NAME = "tmp-";
	private static final Pattern ARTIST_TITLE_PATTERN = Pattern.compile("(.+?)(\\s*?)-(\\s*?)(\"|)(\\S(.+?))\\s*?([&\\*+,-/:;<=>@_\\|]+?\\s*?|)(\\z|\"|\\(|\\[|lyric|official)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();
		
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			Bundle extras = intent.getExtras();
			DownloadManager.Query query = new DownloadManager.Query();
			long downloadId = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
			query.setFilterById(downloadId);
			Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
			
			if (cursor.moveToFirst()) {
				int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
				
				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					// String inPath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
					String inPath = (cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).replace("file://", "");
					String dlTitle = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
					cursor.close();
					DownloadStatus dlStatus = getMultiFileDlStatus(context, downloadId, inPath);
					
					if (dlStatus != null && dlStatus.readyToMerge) {
						if (!dlStatus.hasVideo) {
							String artist = null, title = null;
							Matcher matcher = ARTIST_TITLE_PATTERN.matcher(dlTitle);
							if (matcher.find()) {
								artist = matcher.group(1);
								title = matcher.group(5);
							}
							convertM4a(inPath, title, artist);
							scanFile(inPath, context);
						} else {
							if (inPath.endsWith(".mp4")) {
								mergeMp4(dlStatus.otherFilePath, inPath);
							} else if (inPath.endsWith(".m4a")) {
								mergeMp4(inPath, dlStatus.otherFilePath);
							}
						}
					}
				} else if (status == DownloadManager.STATUS_FAILED) {
					removeTempOnFailure(context, downloadId);
				}
			}
			
		}
	}
	
	private void removeTempOnFailure(Context con, long downloadId) {
		File cacheFileDir = new File(con.getCacheDir().getAbsolutePath());
		for (File file : cacheFileDir.listFiles()) {
			if (file.getName().contains(downloadId + "")) {
				file.delete();
				break;
			}
		}
	}
	
	private DownloadStatus getMultiFileDlStatus(Context con, long downloadId, String filePath) {
		File cacheFileDir = new File(con.getCacheDir().getAbsolutePath());
		File cacheFile = null;
		for (File file : cacheFileDir.listFiles()) {
			if (file.getName().contains(downloadId + "")) {
				cacheFile = file;
				break;
			}
		}
		if (cacheFile != null && cacheFile.exists()) {
			DownloadStatus dlStatus = new DownloadStatus();
			dlStatus.hasVideo = cacheFile.getName().contains("-");
			BufferedReader reader = null;
			BufferedWriter writer = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile), "UTF-8"));
				dlStatus.otherFilePath = reader.readLine();
				reader.close();
				if (dlStatus.otherFilePath != null || !dlStatus.hasVideo) {
					cacheFile.delete();
					dlStatus.readyToMerge = true;
				} else {
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)));
					writer.write(filePath);
				}
				return dlStatus;
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	private void convertM4a(String inFilePath, String title, String artist) {
		String path = inFilePath.substring(0, inFilePath.lastIndexOf("/"));
		try {
			Movie inAudio = MovieCreator.build(inFilePath);
			Container out = new DefaultMp4Builder().build(inAudio);
			
			if (title != null && artist != null) {
				writeMetaData(out, artist, title);
			}
			long currentMillis = System.currentTimeMillis();
			FileOutputStream fileOutputStream = new FileOutputStream(new File(path + TEMP_FILE_NAME + currentMillis + ".m4a"));
			out.writeContainer(fileOutputStream.getChannel());
			fileOutputStream.close();
			File inFile = new File(inFilePath);
			
			if (inFile.delete()) {
				File tempOutFile = new File(path + TEMP_FILE_NAME + currentMillis + ".m4a");
				tempOutFile.renameTo(inFile);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	private void mergeMp4(String inFilePathAudio, String inFilePathVideo) {
		String path = inFilePathVideo.substring(0, inFilePathVideo.lastIndexOf("/"));
		
		try {
			Movie video = MovieCreator.build(inFilePathVideo);
			Movie audio = MovieCreator.build(inFilePathAudio);
			video.addTrack(audio.getTracks().get(0));
			Container out = new DefaultMp4Builder().build(video);
			long currentMillis = System.currentTimeMillis();
			FileOutputStream fos = new FileOutputStream(new File(path + TEMP_FILE_NAME + currentMillis + ".mp4"));
			out.writeContainer(fos.getChannel());
			fos.close();
			File inAudioFile = new File(inFilePathAudio);
			inAudioFile.delete();
			File inVideoFile = new File(inFilePathVideo);
			if (inVideoFile.delete()) {
				File tempOutFile = new File(path + TEMP_FILE_NAME + currentMillis + ".mp4");
				tempOutFile.renameTo(inVideoFile);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	private void writeMetaData(Container out, String artist, String title) {
		return;
		// MovieBox movieBox = null;
		// for (Box box : out.getBoxes()) {
		// if (box.getType().contains("moov")) {
		// movieBox = (MovieBox) box;
		// break;
		// }
		// }
		//
		// if (movieBox != null) {
		// UserDataBox userDataBox = new UserDataBox();
		// movieBox.addBox(userDataBox);
		// MetaBox metaBox = new MetaBox();
		// userDataBox.addBox(metaBox);
		// HandlerBox hBox = new HandlerBox();
		// hBox.setName(null);
		// hBox.setHandlerType("mdir");
		// metaBox.addBox(hBox);
		// AppleItemListBox listBox = new AppleItemListBox();
		// metaBox.addBox(listBox);
		// AppleNameBox titleBox = new AppleNameBox();
		// titleBox.setValue(title);
		// listBox.addBox(titleBox);
		// AppleArtistBox artistBox = new AppleArtistBox();
		// artistBox.setValue(artist);
		// listBox.addBox(artistBox);
		// AppleArtist2Box artist2Box = new AppleArtist2Box();
		// artist2Box.setValue(artist);
		// listBox.addBox(artist2Box);
		// correctChunkOffsets(out, userDataBox.getSize());
		// }
	}
	
	// From the mp4parser metadata example
	// private void correctChunkOffsets(Container container, long correction) {
	// List<Box> chunkOffsetBoxes = Path.getPaths(container, "/moov[0]/trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
	// for (Box chunkOffsetBox : chunkOffsetBoxes) {
	//
	// LinkedList<Box> stblChildren = new LinkedList<>(chunkOffsetBox.getParent().getBoxes());
	// stblChildren.remove(chunkOffsetBox);
	//
	// long[] cOffsets = ((ChunkOffsetBox) chunkOffsetBox).getChunkOffsets();
	// for (int i = 0; i < cOffsets.length; i++) {
	// cOffsets[i] += correction;
	// }
	//
	// StaticChunkOffsetBox cob = new StaticChunkOffsetBox();
	// cob.setChunkOffsets(cOffsets);
	// stblChildren.add(cob);
	// chunkOffsetBox.getParent().setBoxes(stblChildren);
	// }
	// }
	
	private void scanFile(String path, Context con) {
		File file = new File(path);
		Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
		con.sendBroadcast(scanFileIntent);
	}
	
	private class DownloadStatus {
		String otherFilePath;
		boolean readyToMerge = false;
		boolean hasVideo;
	}
}
