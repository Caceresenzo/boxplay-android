package caceresenzo.apps.boxplay.providers.media.music;

import android.content.Context;
import caceresenzo.android.libs.service.ServiceUtils;

public class MusicControls {
	
	private static MusicControls CONTROLS;
	
	private MusicController controller;
	
	public MusicControls() {
		this.controller = MusicController.getMusicController();
	}
	
	public void playControl(Context context) {
		// if (!MusicService.isRunning()) {
		// MusicController.setPreloadableFileEnabled(true);
		// MusicService.createService(context, false);
		// }
		
		sendMessage(MusicService.MESSAGE_PLAY);
	}
	
	public void pauseControl(Context context) {
		sendMessage(MusicService.MESSAGE_PAUSE);
	}
	
	public void playPauseControl(Context context) {
		if (controller.isSongPaused()) {
			playControl(context);
		} else {
			pauseControl(context);
		}
	}
	
	public void nextControl(Context context) {
		if (!ServiceUtils.isServiceRunning(context, MusicService.class)) {
			return;
		}
		
		if (controller.getMusicPlaylist().size() > 0) {
			if (controller.getPlayingSongNumber() < (controller.getMusicPlaylist().size() - 1)) {
				controller.incrementSongNumber(1);
			} else {
				controller.setPlayingSongNumber(0);
			}
			
			controller.getSongChangeHandler().sendMessage(controller.getSongChangeHandler().obtainMessage());
		}
		
		controller.setSongPaused(false);
	}
	
	public void previousControl(Context context) {
		if (!ServiceUtils.isServiceRunning(context, MusicService.class)) {
			return;
		}
		
		if (controller.getMusicPlaylist().size() > 0) {
			if (controller.getPlayingSongNumber() > 0) {
				controller.incrementSongNumber(-1);
			} else {
				controller.setPlayingSongNumber(controller.getMusicPlaylist().size() - 1);
			}
			
			controller.getSongChangeHandler().sendMessage(controller.getSongChangeHandler().obtainMessage());
		}
		
		controller.setSongPaused(false);
	}
	
	private void sendMessage(String message) {
		try {
			controller.getPlayPauseHandler().sendMessage(controller.getPlayPauseHandler().obtainMessage(0, message));
		} catch (Exception exception) {
			;
		}
	}
	
	public static interface OnPlayPauseToggleListener {
		void onToggled();
	}
	
	public static MusicControls getMusicControls() {
		if (CONTROLS == null) {
			CONTROLS = new MusicControls();
		}
		
		return CONTROLS;
	}
}
