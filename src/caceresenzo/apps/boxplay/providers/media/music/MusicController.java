package caceresenzo.apps.boxplay.providers.media.music;

import android.app.Activity;
import android.os.Handler;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicPlaylist;

public class MusicController {
	
	private static MusicController CONTROLLER;
	private static boolean PREALOADABLEFILE;
	
	private MusicPlaylist musicPlaylist = new MusicPlaylist();
	private int songNumber = 0;
	private boolean songPaused = true, songChanged = false;
	private Handler songChangeHandler = new Handler(), playPauseHandler = new Handler(), progressBarHandler = new Handler();
	
	public MusicController() {
		;
	}
	
	public void play(MusicFile musicFile, int index, boolean newPlaylist, boolean userClickedOnList, Activity context) {
		setSongPaused(false);
		setPlayingSongNumber(index);
		
		if (!MusicService.isRunning()) {
			MusicService.createService(context, newPlaylist);
		} else {
			getSongChangeHandler().sendMessage(getSongChangeHandler().obtainMessage(0, new Object[] { newPlaylist, musicFile, userClickedOnList }));
		}
	}
	
	public MusicPlaylist getMusicPlaylist() {
		return musicPlaylist;
	}
	
	public void setMusicPlaylist(MusicPlaylist musicPlaylist) {
		this.musicPlaylist = musicPlaylist;
	}
	
	public int getPlayingSongNumber() {
		return songNumber;
	}
	
	public void setPlayingSongNumber(int songNumber) {
		this.songNumber = songNumber;
	}
	
	public void incrementSongNumber(int value) {
		this.songNumber += value;
	}
	
	public boolean isSongPaused() {
		return songPaused;
	}
	
	public void setSongPaused(boolean songPaused) {
		this.songPaused = songPaused;
	}
	
	public boolean isSongChanged() {
		return songChanged;
	}
	
	public void setSongChanged(boolean songChanged) {
		this.songChanged = songChanged;
	}
	
	public Handler getSongChangeHandler() {
		return songChangeHandler;
	}
	
	public void setSongChangeHandler(Handler songChangeHandler) {
		this.songChangeHandler = songChangeHandler;
	}
	
	public Handler getPlayPauseHandler() {
		return playPauseHandler;
	}
	
	public void setPlayPauseHandler(Handler playPauseHandler) {
		this.playPauseHandler = playPauseHandler;
	}
	
	public Handler getProgressBarHandler() {
		return progressBarHandler;
	}
	
	public void setProgressBarHandler(Handler progressBarHandler) {
		this.progressBarHandler = progressBarHandler;
	}
	
	public static MusicController getMusicController() {
		if (CONTROLLER == null) {
			CONTROLLER = new MusicController();
		}
		
		return CONTROLLER;
	}
	
	public static boolean isPreloadableFileEnabled() {
		boolean actualValue = PREALOADABLEFILE;
		PREALOADABLEFILE = false;
		return actualValue;
	}
	
	public static void setPreloadableFileEnabled(boolean enabled) {
		MusicController.PREALOADABLEFILE = enabled;
	}
	
}
