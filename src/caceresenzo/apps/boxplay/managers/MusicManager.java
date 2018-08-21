package caceresenzo.apps.boxplay.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.MusicActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.fragments.store.MusicPlayerFragment;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.providers.media.music.MusicController;
import caceresenzo.apps.boxplay.providers.media.music.MusicService;
import caceresenzo.libs.boxplay.factory.MusicFactory;
import caceresenzo.libs.boxplay.factory.MusicFactory.MusicFactoryListener;
import caceresenzo.libs.boxplay.models.store.music.MusicAlbum;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.MusicGroup;
import caceresenzo.libs.boxplay.models.store.music.MusicPlaylist;

public class MusicManager extends AbstractManager {
	
	public static final String PREF_MUSIC_PLAYING_SONG_INDEX = "music_playing_song_index";
	
	private MusicController musicController;
	
	private MusicFactory musicFactory = new MusicFactory();
	private List<MusicGroup> groups;
	
	private MusicFile lastMusicFileOpen;
	
	private MusicDatabaseOpenHelper musicDatabaseOpenHelper;
	
	private List<SlidingUpPanelLayout> slidingUpPanelLayouts;
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		musicController = MusicController.getMusicController();
		
		groups = new ArrayList<MusicGroup>();
		
		musicDatabaseOpenHelper = new MusicDatabaseOpenHelper(boxPlayApplication);
		
		slidingUpPanelLayouts = new ArrayList<>();
	}
	
	public void callFactory() {
		groups.clear();
		
		musicFactory.parseServerJson(new MusicFactoryListener() {
			@Override
			public void onJsonMissingFileType() {
				boxPlayApplication.snackbar("Warning. (Music)Factory returned onJsonMissingFileType();", Snackbar.LENGTH_LONG).show();
			}
			
			@Override
			public void onJsonNull() {
				boxPlayApplication.snackbar(R.string.boxplay_error_manager_json_null, Snackbar.LENGTH_LONG).show();
			}
			
			@Override
			public void onMusicGroupCreated(MusicGroup musicGroup) {
				groups.add(musicGroup);
			}
		}, getManagers().getDataManager().getJsonData());
		
		Collections.shuffle(groups);
		
		callDatabase();
		
		try {
			MusicPlayerFragment.getPlayerFragment().updateVisibility();
			MusicService.connect(MusicPlayerFragment.getPlayerFragment());
		} catch (Exception exception) {
			;
		}
	}
	
	private void callDatabase() {
		if (musicController.getMusicPlaylist() != null && !musicController.getMusicPlaylist().isEmpty()) {
			saveDatabase();
		}
		
		musicController.getMusicPlaylist().clear();
		musicController.getMusicPlaylist().addAll(musicDatabaseOpenHelper.restorePlaylist());
		musicController.setPlayingSongNumber(getManagers().getPreferences().getInt(PREF_MUSIC_PLAYING_SONG_INDEX, 0));
		
		MusicPlayerFragment.getPlayerFragment().notifyPlaylistUpdate(true);
	}
	
	public void saveDatabase() {
		musicDatabaseOpenHelper.savePlaylist(musicController.getMusicPlaylist());
		getManagers().getPreferences().edit().putInt(PREF_MUSIC_PLAYING_SONG_INDEX, musicController.getPlayingSongNumber()).commit();
	}
	
	public void playFile(MusicFile musicFile, int index, boolean newPlaylist, boolean userClickedOnList) {
		lastMusicFileOpen = musicFile;
		
		Activity context;
		if (MusicActivity.getMusicActivity() != null) {
			context = MusicActivity.getMusicActivity();
		} else {
			context = boxPlayApplication.getAttachedActivity();
		}
		
		saveDatabase();
		
		MusicController controller = MusicController.getMusicController();
		controller.play(musicFile, index, newPlaylist, userClickedOnList, context);
	}
	
	public void registerMusicSlidingPanel(SlidingUpPanelLayout slidingUpPanelLayout) {
		if (slidingUpPanelLayout != null && !slidingUpPanelLayouts.contains(slidingUpPanelLayout)) {
			slidingUpPanelLayouts.add(slidingUpPanelLayout);
			
			FrameLayout frameLayout = (FrameLayout) slidingUpPanelLayout.findViewById(R.id.activity_boxplay_framelayout_container_music_player);
			// slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
			
			MusicPlayerFragment musicPlayerFragment = MusicPlayerFragment.getPlayerFragment();
			
			boxPlayApplication.getSupportFragmentManager() //
					.beginTransaction() //
					.replace(frameLayout.getId(), musicPlayerFragment) //
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) //
					.commit(); //
			
			musicPlayerFragment.attachSlidingUpPanelLayout(slidingUpPanelLayout);
		}
	}
	
	public void updateMusicInterface(MusicFile music, boolean pause) {
		updateMusicInterface(music, pause, false);
	}
	
	public void updateMusicInterface(MusicFile music, boolean pause, boolean itemJustAdded) {
		for (SlidingUpPanelLayout slidingUpPanelLayout : slidingUpPanelLayouts) {
			if (slidingUpPanelLayout == null) {
				continue;
			}
			
			if (MusicPlayerFragment.getPlayerFragment() != null) {
				MusicPlayerFragment.getPlayerFragment().notifyUpdate(music, pause, itemJustAdded);
			}
			// ToastUtils.makeLong(boxPlayActivity, "update asked (music mng)");
		}
	}
	
	public List<MusicGroup> getGroups() {
		return groups;
	}
	
	public MusicFile getLastMusicFileOpen() {
		return lastMusicFileOpen;
	}
	
	public MusicPlaylist getMusicPlaylist() {
		return musicController.getMusicPlaylist();
	}
	
	class MusicDatabaseOpenHelper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION = 2;
		public static final String DATABASE_NAME = "music_playlist";
		public static final String KEY_POSITION = "position";
		public static final String KEY_MUSIC_IDENTIFIER = "identifier";
		public static final String TABLE_NAME = "playlist";
		public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_POSITION + " TEXT, " + KEY_MUSIC_IDENTIFIER + " TEXT);";
		
		MusicDatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(TABLE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(database);
		}
		
		public void savePlaylist(MusicPlaylist musicPlaylist) {
			try {
				SQLiteDatabase database = getWritableDatabase();
				onUpgrade(database, 1, 1); // Clear
				
				for (int i = 0; i < musicPlaylist.size(); i++) {
					ContentValues content = new ContentValues();
					content.put(KEY_POSITION, i);
					content.put(KEY_MUSIC_IDENTIFIER, musicPlaylist.get(i).getIdentifier());
					database.insert(TABLE_NAME, null, content);
				}
				
				close();
			} catch (Exception exception) {
				exception.printStackTrace();
				try {
					onUpgrade(getWritableDatabase(), 1, 1); // Clear
				} catch (Exception exception2) {
					exception2.printStackTrace();
				}
			}
		}
		
		public MusicPlaylist restorePlaylist() {
			MusicPlaylist musicPlaylist = new MusicPlaylist();
			
			try {
				SQLiteDatabase database = getReadableDatabase();
				
				Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
				
				while (cursor.moveToNext()) {
					String musicIdentifier = cursor.getString(cursor.getColumnIndex(KEY_MUSIC_IDENTIFIER));
					
					boolean found = false;
					for (MusicGroup group : groups) {
						if (found || group.getAlbums() == null) {
							break;
						}
						
						for (MusicAlbum album : group.getAlbums()) {
							if (found || album.getMusics() == null) {
								break;
							}
							
							for (MusicFile music : album.getMusics()) {
								if (music.getIdentifier().equals(musicIdentifier)) {
									musicPlaylist.add(music);
									found = true;
									break;
								}
							}
						}
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				try {
					onUpgrade(getWritableDatabase(), 1, 1); // Clear
				} catch (Exception exception2) {
					exception2.printStackTrace();
				}
			}
			
			close();
			
			return musicPlaylist;
		}
	}
	
}