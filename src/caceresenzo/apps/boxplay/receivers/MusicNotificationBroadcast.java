package caceresenzo.apps.boxplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import caceresenzo.apps.boxplay.providers.media.music.MusicController;
import caceresenzo.apps.boxplay.providers.media.music.MusicControls;
import caceresenzo.apps.boxplay.providers.media.music.MusicService;

public class MusicNotificationBroadcast extends BroadcastReceiver {
	
	public static final String TAG = MusicNotificationBroadcast.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
			if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
				return;
			}
			
			switch (keyEvent.getKeyCode()) {
				case KeyEvent.KEYCODE_HEADSETHOOK:
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
					if (!MusicController.getMusicController().isSongPaused()) {
						MusicControls.getMusicControls().pauseControl(context);
					} else {
						MusicControls.getMusicControls().playControl(context);
					}
					break;
				}
				case KeyEvent.KEYCODE_MEDIA_PLAY: {
					break;
				}
				case KeyEvent.KEYCODE_MEDIA_PAUSE: {
					break;
				}
				case KeyEvent.KEYCODE_MEDIA_STOP: {
					break;
				}
				case KeyEvent.KEYCODE_MEDIA_NEXT: {
					Log.d(TAG, TAG + ": KEYCODE_MEDIA_NEXT");
					MusicControls.getMusicControls().nextControl(context);
					break;
				}
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
					Log.d(TAG, TAG + ": KEYCODE_MEDIA_PREVIOUS");
					MusicControls.getMusicControls().previousControl(context);
					break;
				}
			}
		} else {
			switch (intent.getAction()) {
				case MusicService.NOTIFY_PLAY: {
					MusicControls.getMusicControls().playControl(context);
					break;
				}
				case MusicService.NOTIFY_PAUSE: {
					MusicControls.getMusicControls().pauseControl(context);
					break;
				}
				case MusicService.NOTIFY_NEXT: {
					MusicControls.getMusicControls().nextControl(context);
					break;
				}
				case MusicService.NOTIFY_DELETE: {
					// Intent serviceIntent = new Intent(context, MusicService.class);
					// context.stopService(serviceIntent);
					// Intent activityIntent = new Intent(context, BoxPlayActivity.class);
					// activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// context.startActivity(activityIntent);
					MusicService.destroyService(context);
					break;
				}
				case MusicService.NOTIFY_PREVIOUS: {
					MusicControls.getMusicControls().previousControl(context);
					break;
				}
				
				default: {
					break;
				}
			}
		}
	}
	
	public String componentName() {
		return this.getClass().getName();
	}
	
}
