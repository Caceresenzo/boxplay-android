package caceresenzo.apps.boxplay.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.helper.LocaleHelper;

public class YoutubeDownloadActivity extends Activity {
	
	private static final int ITAG_FOR_AUDIO = 140;
	
	private static String youtubeLink;
	
	private LinearLayout mainLayout;
	private ProgressBar mainProgressBar;
	private List<YoutubeFragmentedVideo> formatsToShowList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youtube_download);
		
		mainLayout = (LinearLayout) findViewById(R.id.main_layout);
		mainProgressBar = (ProgressBar) findViewById(R.id.prgrBar);
		
		if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
			String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
			
			if (ytLink != null && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
				getYoutubeDownloadUrl(youtubeLink = ytLink);
			} else {
				Toast.makeText(this, "error_no_yt_link", Toast.LENGTH_LONG).show();
				finish();
			}
		} else if (savedInstanceState != null && youtubeLink != null) {
			getYoutubeDownloadUrl(youtubeLink);
		} else {
			finish();
		}
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	private void getYoutubeDownloadUrl(String youtubeLink) {
		new YouTubeExtractor(this) {
			
			@Override
			public void onExtractionComplete(SparseArray<YtFile> youtubeFiles, VideoMeta videoMeta) {
				mainProgressBar.setVisibility(View.GONE);
				
				if (youtubeFiles == null) {
					TextView textView = new TextView(YoutubeDownloadActivity.this);
					textView.setText("error");
					textView.setMovementMethod(LinkMovementMethod.getInstance());
					mainLayout.addView(textView);
					return;
				}
				
				formatsToShowList = new ArrayList<>();
				for (int i = 0, itag; i < youtubeFiles.size(); i++) {
					itag = youtubeFiles.keyAt(i);
					YtFile ytFile = youtubeFiles.get(itag);
					
					if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
						addFormatToList(ytFile, youtubeFiles);
					}
				}
				
				Collections.sort(formatsToShowList, new Comparator<YoutubeFragmentedVideo>() {
					@Override
					public int compare(YoutubeFragmentedVideo ytFragmentedVideo1, YoutubeFragmentedVideo ytFragmentedVideo2) {
						return ytFragmentedVideo1.height - ytFragmentedVideo2.height;
					}
				});
				
				for (YoutubeFragmentedVideo files : formatsToShowList) {
					addButtonToMainLayout(videoMeta.getTitle(), files);
				}
			}
		}.extract(youtubeLink, true, false);
	}
	
	private void addFormatToList(YtFile ytFile, SparseArray<YtFile> ytFiles) {
		int height = ytFile.getFormat().getHeight();
		
		if (height != -1) {
			for (YoutubeFragmentedVideo frVideo : formatsToShowList) {
				if (frVideo.height == height && (frVideo.videoFile == null || frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {
					return;
				}
			}
		}
		
		YoutubeFragmentedVideo fragmentedVideo = new YoutubeFragmentedVideo();
		fragmentedVideo.height = height;
		
		if (ytFile.getFormat().isDashContainer()) {
			if (height > 0) {
				fragmentedVideo.videoFile = ytFile;
				fragmentedVideo.audioFile = ytFiles.get(ITAG_FOR_AUDIO);
			} else {
				fragmentedVideo.audioFile = ytFile;
			}
		} else {
			fragmentedVideo.videoFile = ytFile;
		}
		
		formatsToShowList.add(fragmentedVideo);
	}
	
	private void addButtonToMainLayout(final String videoTitle, final YoutubeFragmentedVideo fragmentedVideo) {
		String buttonText;
		if (fragmentedVideo.height == -1) {
			buttonText = "Audio " + fragmentedVideo.audioFile.getFormat().getAudioBitrate() + " kbit/s";
		} else {
			buttonText = (fragmentedVideo.videoFile.getFormat().getFps() == 60) ? fragmentedVideo.height + "p60" : fragmentedVideo.height + "p";
		}
		
		Button button = new Button(this);
		button.setText(buttonText);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String filename;
				
				if (videoTitle.length() > 55) {
					filename = videoTitle.substring(0, 55);
				} else {
					filename = videoTitle;
				}
				
				filename = filename.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/", "");
				filename += (fragmentedVideo.height == -1) ? "" : "-" + fragmentedVideo.height + "p";
				String downloadIds = "";
				boolean hideAudioDownloadNotification = false;
				
				if (fragmentedVideo.videoFile != null) {
					downloadIds += downloadFromUrl(fragmentedVideo.videoFile.getUrl(), videoTitle, filename + "." + fragmentedVideo.videoFile.getFormat().getExt(), false);
					downloadIds += "-";
					hideAudioDownloadNotification = true;
				}
				
				if (fragmentedVideo.audioFile != null) {
					downloadIds += downloadFromUrl(fragmentedVideo.audioFile.getUrl(), videoTitle, filename + "." + fragmentedVideo.audioFile.getFormat().getExt(), hideAudioDownloadNotification);
				}
				
				if (fragmentedVideo.audioFile != null) {
					cacheDownloadIds(downloadIds);
				}
				
				finish();
			}
		});
		mainLayout.addView(button);
	}
	
	private long downloadFromUrl(String downloadUrl, String downloadTitle, String fileName, boolean hide) {
		Uri uri = Uri.parse(downloadUrl);
		DownloadManager.Request request = new DownloadManager.Request(uri);
		request.setTitle(downloadTitle);
		if (hide) {
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			request.setVisibleInDownloadsUi(false);
		} else {
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
		
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		return manager.enqueue(request);
	}
	
	private void cacheDownloadIds(String downloadIds) {
		File downloadCacheFile = new File(this.getCacheDir().getAbsolutePath() + "/" + downloadIds);
		try {
			downloadCacheFile.createNewFile();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	private class YoutubeFragmentedVideo {
		int height;
		YtFile audioFile;
		YtFile videoFile;
	}
	
}
