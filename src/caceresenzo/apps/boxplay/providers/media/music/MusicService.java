package caceresenzo.apps.boxplay.providers.media.music;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import caceresenzo.android.libs.AndroidUtils;
import caceresenzo.android.libs.service.ServiceUtils;
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.store.MusicPlayerFragment;
import caceresenzo.apps.boxplay.receivers.MusicNotificationBroadcast;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicPlaylist;
import caceresenzo.libs.parse.ParseUtils;

@SuppressWarnings({ "deprecation", "unused" })
public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener {
	
	public static final String TAG = MusicService.class.getSimpleName();
	
	public static final String NOTIFY_PREVIOUS = "caceresenzo.apps.boxplay.PREVIOUS";
	public static final String NOTIFY_DELETE = "caceresenzo.apps.boxplay.DELETE";
	public static final String NOTIFY_PAUSE = "caceresenzo.apps.boxplay.PAUSE";
	public static final String NOTIFY_PLAY = "caceresenzo.apps.boxplay.PLAY";
	public static final String NOTIFY_NEXT = "caceresenzo.apps.boxplay.NEXT";
	
	public static final String MESSAGE_PLAY = "PLAY";
	public static final String MESSAGE_PAUSE = "PAUSE";
	public static final String MESSAGE_PRELOAD = "PRELOAD";
	
	public static final String KEY_CREATE_NEW_PLAYLIST = "NEW_PLAYLIST";
	
	public static final int NOTIFICATION_ID = 100;
	
	public static MusicService SERVICE;
	public static Intent INTENT;
	public static MusicFile LASTMUSIC;
	
	private AudioManager audioManager;
	private MediaPlayer mediaPlayer;
	private MusicController controller;
	private MusicControls controls;
	private MainTask mainTask;
	private boolean songLoading = false;
	
	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	private Timer timer;
	private boolean supportBigNotification = false, supportLockScreenControls = false;
	
	private Bitmap dummyAlbumArt;
	
	private final Handler HANDLER = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (mediaPlayer != null) {
				if (songLoading) {
					return;
				}
				
				if (mediaPlayer.getDuration() == 0) {
					ToastUtils.makeLong(getApplicationContext(), "Error: Song time is 0.");
					return;
				}
				
				int progress = (mediaPlayer.getCurrentPosition() * 100) / mediaPlayer.getDuration();
				int integer[] = new int[3];
				integer[0] = mediaPlayer.getCurrentPosition();
				integer[1] = mediaPlayer.getDuration();
				integer[2] = progress;
				
				try {
					controller.getProgressBarHandler().sendMessage(controller.getProgressBarHandler().obtainMessage(0, integer));
				} catch (Exception exception) {
					;
				}
			}
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		SERVICE = this;
		
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		mediaPlayer = new MediaPlayer();
		controller = MusicController.getMusicController();
		controls = MusicControls.getMusicControls();
		
		supportBigNotification = AndroidUtils.currentVersionSupportBigNotification();
		supportLockScreenControls = AndroidUtils.currentVersionSupportLockScreenControls();
		
		timer = new Timer();
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				// controls.nextControl(getApplicationContext());
			}
		});
	}
	
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			MusicFile music = BoxPlayApplication.getManagers().getMusicManager().getLastMusicFileOpen();
			
			try {
				MusicPlayerFragment.getPlayerFragment().updateVisibility();
			} catch (Exception exception) {
				;
			}
			
			preparePlaylist(music, intent.getBooleanExtra(KEY_CREATE_NEW_PLAYLIST, true), true);
			
			if (supportLockScreenControls) {
				registerRemoteClient();
			}
			
			String path = music.getUrl();
			playMusicFile(path, music);
			updateNotification();
			
			controller.setSongChangeHandler(new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message message) {
					Object[] object = (Object[]) message.obj;
					
					boolean newPlaylist = false, userClickedOnList = false;
					MusicFile music = getActualSong();
					
					if (object != null) {
						newPlaylist = ParseUtils.parseBoolean(object[0], false);
						userClickedOnList = ParseUtils.parseBoolean(object[0], false);
						music = (MusicFile) object[1];
					}
					
					processPlay(music, newPlaylist, userClickedOnList);
					
					return false;
				}
			}));
			
			controller.setPlayPauseHandler(new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message message) {
					String object = (String) message.obj;
					
					if (mediaPlayer == null) {
						return false;
					}
					
					switch (object) {
						case MESSAGE_PLAY:
							controller.setSongPaused(false);
							
							if (supportLockScreenControls) {
								remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
							}
							
							if (LASTMUSIC == getActualSong()) {
								mediaPlayer.start();
							} else {
								processPlay(getActualSong(), false, false);
							}
							break;
						case MESSAGE_PAUSE:
							controller.setSongPaused(true);
							
							if (supportLockScreenControls) {
								remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
							}
							
							mediaPlayer.pause();
							break;
						
						default:
							break;
					}
					
					updateNotification();
					
					try {
						askInterfaceUpdate(controller.getMusicPlaylist().get(controller.getPlayingSongNumber()), controller.isSongPaused());
					} catch (Exception exception) {
						;
					}
					
					Log.d(TAG, "Pressed: " + object);
					return false;
				}
			}));
			
		} catch (Exception exception) {
			Log.d(TAG, "Error in initialization.", exception);
		}
		return START_STICKY;
	}
	
	public void processPlay(MusicFile music, boolean newPlaylist, boolean usedClickedOnList) {
		preparePlaylist(music, newPlaylist, usedClickedOnList);
		updateNotification();
		
		try {
			playMusicFile(music.getUrl(), music);
			askInterfaceUpdate(music, false);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void preparePlaylist(MusicFile music, boolean newPlaylist, boolean userClickedOnList) {
		if (music == null) {
			return;
		}
		
		MusicPlaylist playlist = controller.getMusicPlaylist();
		
		if (playlist == null) {
			controller.setMusicPlaylist(playlist = new MusicPlaylist());
			// ToastUtils.makeLong(SERVICE, "created new instance");
		}
		
		if (newPlaylist) {
			playlist.clear();
			playlist.addAll(music.getParentAlbum().getMusics());
		}
		// ToastUtils.makeLong(SERVICE, "target: " + music.getTitle() + "\nnew playlist: " + newPlaylist + ", contain: " + controller.getMusicPlaylist().contains(music) + ", size: " + controller.getMusicPlaylist().size());
		
		try {
			MusicPlayerFragment.getPlayerFragment().notifyPlaylistUpdate(userClickedOnList);
		} catch (Exception exception) {
			;
		}
	}
	
	@SuppressLint("NewApi")
	private void updateNotification() {
		if (getActualSong() == null) {
			return;
		}
		
		String songName = getActualSong().getTitle();
		String albumName = getActualSong().getParentAlbum().getTitle();
		
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_music_player);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_music_player_large);
		
		Notification notification = new NotificationCompat.Builder(getApplicationContext()) //
				.setSmallIcon(R.drawable.icon_audiotrack_light) //
				.setContentTitle(songName) //
				.build(); //
		
		setIntentForNotification(simpleContentView);
		setIntentForNotification(expandedView);
		
		notification.contentView = simpleContentView;
		if (supportBigNotification) {
			notification.bigContentView = expandedView;
		}
		
		// try {
		// long albumId = controller.getMusicPlaylist().get(controller.getSongNumber()).getAlbumId();
		// Bitmap albumArt = PlayerFunctions.getAlbumart(getApplicationContext(), albumId);
		// if (albumArt != null) {
		// notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
		// if (supportBigNotification) {
		// notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
		// }
		// } else {
		// notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.placeholder_video);
		// if (supportBigNotification) {
		// notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.placeholder_video);
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.icon_audiotrack);
		if (supportBigNotification) {
			notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.icon_audiotrack);
		}
		
		if (controller.isSongPaused()) {
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			
			if (supportBigNotification) {
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		} else {
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);
			
			if (supportBigNotification) {
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}
		
		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if (supportBigNotification) {
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}
	
	public void setIntentForNotification(RemoteViews view) {
		PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, previousPendingIntent);
		
		PendingIntent deletePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_DELETE), PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, deletePendingIntent);
		
		PendingIntent pausePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pausePendingIntent);
		
		PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, nextPendingIntent);
		
		PendingIntent playPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(NOTIFY_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, playPendingIntent);
	}
	
	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
		
		if (MusicPlayerFragment.getPlayerFragment() != null) {
			MusicPlayerFragment.getPlayerFragment().getTrackProgressBar().setVisibility(View.GONE);
		}
		
		super.onDestroy();
	}
	
	@SuppressLint("NewApi")
	private void playMusicFile(final String path, final MusicFile music) {
		LASTMUSIC = music;
		
		if (supportLockScreenControls) {
			updateMetadata(music);
			remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
		}
		
		if (MusicPlayerFragment.getPlayerFragment() != null) {
			MusicPlayerFragment.getPlayerFragment().lockControls(true);
		}
		
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			songLoading = true;
			
			if (mainTask != null) {
				mainTask = null;
			}
			
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					songLoading = false;
					
					if (MusicPlayerFragment.getPlayerFragment() != null) {
						MusicPlayerFragment.getPlayerFragment().lockControls(false);
					}
					
					if (mainTask == null) {
						timer.scheduleAtFixedRate(mainTask = new MainTask(), 0, 100);
					}
					
					if (!MusicController.isPreloadableFileEnabled()) {
						mediaPlayer.start();
					}
					
					askInterfaceUpdate(music, false);
				}
			});
			mediaPlayer.prepareAsync();
		} catch (IOException exception) {
			Log.d(TAG, "Error when playing music...", exception);
			songLoading = false;
		}
	}
	
	private void askInterfaceUpdate(MusicFile music, boolean pause) {
		if (BoxPlayApplication.getManagers() != null && BoxPlayApplication.getManagers().getMusicManager() != null) {
			BoxPlayApplication.getManagers().getMusicManager().updateMusicInterface(music, pause);
		}
	}
	
	@SuppressLint("NewApi")
	private void registerRemoteClient() {
		remoteComponentName = new ComponentName(getApplicationContext(), new MusicNotificationBroadcast().componentName());
		
		try {
			if (remoteControlClient == null) {
				audioManager.registerMediaButtonEventReceiver(remoteComponentName);
				
				Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
				mediaButtonIntent.setComponent(remoteComponentName);
				PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
				
				remoteControlClient = new RemoteControlClient(mediaPendingIntent);
				audioManager.registerRemoteControlClient(remoteControlClient);
			}
			
			remoteControlClient.setTransportControlFlags( //
					RemoteControlClient.FLAG_KEY_MEDIA_PLAY | //
							RemoteControlClient.FLAG_KEY_MEDIA_PAUSE | //
							RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE | //
							RemoteControlClient.FLAG_KEY_MEDIA_STOP | //
							RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS | //
							RemoteControlClient.FLAG_KEY_MEDIA_NEXT //
			);
		} catch (Exception exception) {
			;
		}
	}
	
	@SuppressLint("NewApi")
	private void updateMetadata(final MusicFile music) {
		if (remoteControlClient == null) {
			return;
		}
		
		try {
			// dummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.player_bg);
			dummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.icon_audiotrack_light);
			if (dummyAlbumArt == null) {
				dummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.icon_audiotrack_light);
			}
			
			MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
			
			metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, music.getParentAlbum().getTitle());
			metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, music.getParentAlbum().getAuthors().toString());
			metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, music.getTitle());
			metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, dummyAlbumArt);
			metadataEditor.apply();
			
			audioManager.requestAudioFocus(MusicService.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		} catch (Exception exception) {
			; // NullPointerException ??
		}
	}
	
	@Override
	public void onAudioFocusChange(int focusChange) {
		;
	}
	
	public MusicFile getActualSong() {
		try {
			return controller.getMusicPlaylist().get(controller.getPlayingSongNumber());
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			if (controller.getMusicPlaylist().size() == 0 || controller.getMusicPlaylist().size() >= controller.getPlayingSongNumber()) {
				destroyService(SERVICE);
				return null;
			}
			
			return controller.getMusicPlaylist().get(controller.getMusicPlaylist().size());
		} catch (Exception exception) {
			try {
				return controller.getMusicPlaylist().get(0);
			} catch (Exception exception2) {
				destroyService(SERVICE);
				return null;
			}
		}
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	public static void connect(MusicPlayerFragment musicPlayerFragment) {
		MusicFile target = null;
		boolean paused = true;
		if (SERVICE != null) {
			target = SERVICE.getActualSong();
			paused = SERVICE.getMediaPlayer().isPlaying();
		} else {
			MusicController musicController = MusicController.getMusicController();
			if (!musicController.getMusicPlaylist().isEmpty()) {
				try {
					target = musicController.getMusicPlaylist().get(musicController.getPlayingSongNumber());
				} catch (Exception exception) {
					try {
						target = musicController.getMusicPlaylist().get(0);
					} catch (Exception exception2) {
						;
					}
				}
			}
		}
		
		// ToastUtils.makeLong(BoxPlayApplication.getBoxPlayApplication(), "Service connected: " + target);
		musicPlayerFragment.notifyUpdate(target, paused);
	}
	
	/**
	 * Send message from timer
	 */
	private class MainTask extends TimerTask {
		public void run() {
			HANDLER.sendEmptyMessage(0);
		}
	}
	
	public static boolean isRunning() {
		return ServiceUtils.isServiceRunning(BoxPlayApplication.getBoxPlayApplication(), MusicService.class);
	}
	
	public static MusicService getMusicService() {
		return SERVICE;
	}
	
	public static Intent getServiceIntent() {
		return INTENT;
	}
	
	public static void createService(Context context, boolean newPlaylist) {
		Intent intent = new Intent(context, MusicService.class);
		intent.putExtra(MusicService.KEY_CREATE_NEW_PLAYLIST, newPlaylist);
		context.startService(intent);
	}
	
	public static void destroyService(Context context) {
		Intent serviceIntent = new Intent(context, MusicService.class);
		context.stopService(serviceIntent);
	}
	
}