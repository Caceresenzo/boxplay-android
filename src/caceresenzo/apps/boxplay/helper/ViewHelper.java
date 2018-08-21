package caceresenzo.apps.boxplay.helper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.budiyev.android.imageloader.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import caceresenzo.android.libs.application.ApplicationUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.MusicActivity;
import caceresenzo.apps.boxplay.activities.VideoActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicStoreSubCategory;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalDataType;
import caceresenzo.libs.boxplay.models.element.enums.ElementLanguage;
import caceresenzo.libs.boxplay.models.element.implementations.MusicElement;
import caceresenzo.libs.boxplay.models.store.music.MusicFile;
import caceresenzo.libs.boxplay.models.store.music.enums.MusicAuthorType;
import caceresenzo.libs.boxplay.models.store.music.enums.MusicGenre;
import caceresenzo.libs.boxplay.models.store.video.VideoFile;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.models.store.video.VideoSeason;
import caceresenzo.libs.boxplay.models.store.video.enums.VideoFileType;
import caceresenzo.libs.boxplay.models.store.video.enums.VideoType;

public class ViewHelper {
	private BoxPlayApplication boxPlayApplication;
	private BoxPlayActivity boxPlayActivity;
	
	private static Map<MenuIdItem, MenuItem> drawerMenuIds = new HashMap<MenuIdItem, MenuItem>();
	private static Map<Object, String> enumCacheTranslation = new HashMap<Object, String>();
	
	/*
	 * Cache
	 */
	public void setBoxPlayActivity(BoxPlayActivity boxPlayActivity) {
		this.boxPlayActivity = boxPlayActivity;
		
		/*
		 * Vlc
		 */
		checkVlc();
		DATEFORMAT_VIDEO_DURATION.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public void prepareCache(BoxPlayApplication application) {
		this.boxPlayApplication = application;
		
		/*
		 * Menu cache
		 */
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_store_video, true), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_store_music, true), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_feed), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_friends), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_chat), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_culture_searchngo), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_premium_adult), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_mylist_watchlater), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_other_settings), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_other_about), null);
		
		/*
		 * Translation cache
		 */
		// Video
		enumCacheTranslation.put(VideoFileType.ANIME, boxPlayApplication.getString(R.string.boxplay_store_video_file_type_anime));
		enumCacheTranslation.put(VideoFileType.SERIE, boxPlayApplication.getString(R.string.boxplay_store_video_file_type_serie));
		enumCacheTranslation.put(VideoFileType.ANIMEMOVIE, boxPlayApplication.getString(R.string.boxplay_store_video_file_type_animemovie));
		enumCacheTranslation.put(VideoFileType.MOVIE, boxPlayApplication.getString(R.string.boxplay_store_video_file_type_movie));
		enumCacheTranslation.put(VideoFileType.UNKNOWN, boxPlayApplication.getString(R.string.boxplay_store_video_enum_unknown));
		
		enumCacheTranslation.put(VideoType.EPISODE, boxPlayApplication.getString(R.string.boxplay_store_video_type_episode));
		enumCacheTranslation.put(VideoType.OAV, boxPlayApplication.getString(R.string.boxplay_store_video_type_oav));
		enumCacheTranslation.put(VideoType.SPECIAL, boxPlayApplication.getString(R.string.boxplay_store_video_type_special));
		enumCacheTranslation.put(VideoType.MOVIE, boxPlayApplication.getString(R.string.boxplay_store_video_type_movie));
		enumCacheTranslation.put(VideoType.OTHER, boxPlayApplication.getString(R.string.boxplay_store_video_type_other));
		enumCacheTranslation.put(VideoType.UNKNOWN, boxPlayApplication.getString(R.string.boxplay_store_video_enum_unknown));
		
		enumCacheTranslation.put(ElementLanguage.FR, boxPlayApplication.getString(R.string.boxplay_store_video_language_french));
		enumCacheTranslation.put(ElementLanguage.ENSUBFR, boxPlayApplication.getString(R.string.boxplay_store_video_language_english_subtitle_french));
		enumCacheTranslation.put(ElementLanguage.JPSUBFR, boxPlayApplication.getString(R.string.boxplay_store_video_language_japanese_subtitle_french));
		enumCacheTranslation.put(ElementLanguage.UNKNOWN, boxPlayApplication.getString(R.string.boxplay_store_video_enum_unknown));
		
		enumCacheTranslation.put(VideoStoreSubCategory.YOURLIST, boxPlayApplication.getString(R.string.boxplay_store_video_category_your_list));
		enumCacheTranslation.put(VideoStoreSubCategory.RECOMMENDED, boxPlayApplication.getString(R.string.boxplay_store_video_category_recommended));
		enumCacheTranslation.put(VideoStoreSubCategory.ANIMES, boxPlayApplication.getString(R.string.boxplay_store_video_category_animes));
		enumCacheTranslation.put(VideoStoreSubCategory.MOVIES, boxPlayApplication.getString(R.string.boxplay_store_video_category_movies));
		enumCacheTranslation.put(VideoStoreSubCategory.SERIES, boxPlayApplication.getString(R.string.boxplay_store_video_category_series));
		enumCacheTranslation.put(VideoStoreSubCategory.RANDOM, boxPlayApplication.getString(R.string.boxplay_store_video_category_random));
		enumCacheTranslation.put(VideoStoreSubCategory.RELEASE, boxPlayApplication.getString(R.string.boxplay_store_video_category_release));
		
		// Music
		enumCacheTranslation.put(MusicAuthorType.AUTHOR, boxPlayApplication.getString(R.string.boxplay_store_music_author_type_author));
		enumCacheTranslation.put(MusicAuthorType.BAND, boxPlayApplication.getString(R.string.boxplay_store_music_author_type_band));
		enumCacheTranslation.put(MusicAuthorType.UNKNOWN, boxPlayApplication.getString(R.string.boxplay_store_music_type_unknown));
		
		enumCacheTranslation.put(MusicGenre.AFRICAN, boxPlayApplication.getString(R.string.boxplay_store_music_type_african));
		enumCacheTranslation.put(MusicGenre.ASIAN, boxPlayApplication.getString(R.string.boxplay_store_music_type_asian));
		enumCacheTranslation.put(MusicGenre.COMEDY, boxPlayApplication.getString(R.string.boxplay_store_music_type_comedy));
		enumCacheTranslation.put(MusicGenre.COUNTRY, boxPlayApplication.getString(R.string.boxplay_store_music_type_country));
		enumCacheTranslation.put(MusicGenre.EASY_LISTENING, boxPlayApplication.getString(R.string.boxplay_store_music_type_easy_listening));
		enumCacheTranslation.put(MusicGenre.ELECTRONIC, boxPlayApplication.getString(R.string.boxplay_store_music_type_electronic));
		enumCacheTranslation.put(MusicGenre.FOLK, boxPlayApplication.getString(R.string.boxplay_store_music_type_folk));
		enumCacheTranslation.put(MusicGenre.HIP_HOP, boxPlayApplication.getString(R.string.boxplay_store_music_type_hip_hop));
		enumCacheTranslation.put(MusicGenre.JAZZ, boxPlayApplication.getString(R.string.boxplay_store_music_type_jazz));
		enumCacheTranslation.put(MusicGenre.LATIN, boxPlayApplication.getString(R.string.boxplay_store_music_type_latin));
		enumCacheTranslation.put(MusicGenre.POP, boxPlayApplication.getString(R.string.boxplay_store_music_type_pop));
		enumCacheTranslation.put(MusicGenre.RAP, boxPlayApplication.getString(R.string.boxplay_store_music_type_rap));
		enumCacheTranslation.put(MusicGenre.ROCK, boxPlayApplication.getString(R.string.boxplay_store_music_type_rock));
		enumCacheTranslation.put(MusicGenre.SOCA, boxPlayApplication.getString(R.string.boxplay_store_music_type_soca));
		enumCacheTranslation.put(MusicGenre.UNKNOWN, boxPlayApplication.getString(R.string.boxplay_store_music_type_unknown));
		
		enumCacheTranslation.put(MusicStoreSubCategory.YOURLIST, boxPlayApplication.getString(R.string.boxplay_store_music_category_your_list));
		enumCacheTranslation.put(MusicStoreSubCategory.RECOMMENDED, boxPlayApplication.getString(R.string.boxplay_store_music_category_recommended));
		enumCacheTranslation.put(MusicStoreSubCategory.ALBUMS, boxPlayApplication.getString(R.string.boxplay_store_music_category_albums));
		enumCacheTranslation.put(MusicStoreSubCategory.RANDOM, boxPlayApplication.getString(R.string.boxplay_store_music_category_random));
		enumCacheTranslation.put(MusicStoreSubCategory.RELEASE, boxPlayApplication.getString(R.string.boxplay_store_music_category_release));
		
		// Search n' Go
		enumCacheTranslation.put(AdditionalDataType.THUMBNAIL, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_thumbnail));
		enumCacheTranslation.put(AdditionalDataType.NAME, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_name));
		enumCacheTranslation.put(AdditionalDataType.ORIGINAL_NAME, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_original_name));
		enumCacheTranslation.put(AdditionalDataType.ALTERNATIVE_NAME, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_alternative_name));
		enumCacheTranslation.put(AdditionalDataType.OTHER_NAME, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_other_name));
		enumCacheTranslation.put(AdditionalDataType.TYPE, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_type));
		enumCacheTranslation.put(AdditionalDataType.QUALITY, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_quality));
		enumCacheTranslation.put(AdditionalDataType.VERSION, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_version));
		enumCacheTranslation.put(AdditionalDataType.TRADUCTION_TEAM, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_traduction_team));
		enumCacheTranslation.put(AdditionalDataType.GENDERS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_genders));
		enumCacheTranslation.put(AdditionalDataType.STATUS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_status));
		enumCacheTranslation.put(AdditionalDataType.COUNTRY, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_country));
		enumCacheTranslation.put(AdditionalDataType.DIRECTOR, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_director));
		enumCacheTranslation.put(AdditionalDataType.AUTHORS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_authors));
		enumCacheTranslation.put(AdditionalDataType.ACTORS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_actors));
		enumCacheTranslation.put(AdditionalDataType.ARTISTS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_artists));
		enumCacheTranslation.put(AdditionalDataType.STUDIOS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_studios));
		enumCacheTranslation.put(AdditionalDataType.RELEASE_DATE, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_release_date));
		enumCacheTranslation.put(AdditionalDataType.VIEWS, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_views));
		enumCacheTranslation.put(AdditionalDataType.DURATION, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_duration));
		enumCacheTranslation.put(AdditionalDataType.RESUME, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_resume));
		enumCacheTranslation.put(AdditionalDataType.RATING, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_rating));
		
		enumCacheTranslation.put(AdditionalDataType.ITEM_VIDEO, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_item_video));
		enumCacheTranslation.put(AdditionalDataType.ITEM_CHAPTER, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_item_chapter));
		
		enumCacheTranslation.put(AdditionalDataType.NULL, boxPlayApplication.getString(R.string.boxplay_culture_searchngo_search_result_data_type_null));
	}
	
	public void recache() {
		if (boxPlayApplication == null) {
			return;
		}
		
		// boxPlayActivity.toast(R.string.boxplay_viewhelper_recaching).show();
		
		drawerMenuIds.clear();
		enumCacheTranslation.clear();
		
		prepareCache(boxPlayApplication);
	}
	
	/*
	 * Menu Help
	 */
	public void unselectAllMenu() {
		Menu menu = boxPlayActivity.getNavigationView().getMenu();
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
			MenuItem item = drawerMenuIds.get(menuIdItem);
			if (item == null) {
				drawerMenuIds.put(menuIdItem, item = menu.findItem(menuIdItem.getId()));
			}
			
			if (item != null && item.isChecked()) {
				item.setChecked(false);
			}
		}
		// BoxPlayActivity.getBoxPlayActivity().toast(String.format("\"looped over %s item\"", drawerMenuIds.keySet().size())).show();
	}
	
	public void updateSeachMenu(int nextId) {
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
			if (menuIdItem.getId() == nextId) {
				try {
					boxPlayActivity.getOptionsMenu().findItem(R.id.menu_main_action_search).setVisible(menuIdItem.isSearchAllowed());
				} catch (Exception exception) {
					;
				}
				break;
			}
		}
	}
	
	/*
	 * Fragment Help
	 */
	private Fragment lastFragment;
	private int lastFragmentMenuItemId;
	
	public Fragment getLastFragment() {
		return lastFragment;
	}
	
	public void setLastFragment(Fragment lastFragment) {
		this.lastFragment = lastFragment;
	}
	
	public int getLastFragmentMenuItemId() {
		return lastFragmentMenuItemId;
	}
	
	public void setLastFragmentMenuItemId(int lastFragmentMenuItemId) {
		this.lastFragmentMenuItemId = lastFragmentMenuItemId;
	}
	
	/*
	 * View Help
	 */
	public void downloadToImageView(ImageView imageView, String url) {
		downloadToImageView(null, imageView, url);
	}
	
	/*
	 * View Help
	 */
	public void downloadToImageView(Context context, ImageView imageView, String url) {
		if (imageView == null) {
			return;
		}
		
		if (url == null) {
			imageView.setImageDrawable(new ColorDrawable(color(R.color.colorError)));
			return;
		}
		
		ImageLoader.with(context != null ? context : boxPlayApplication) //
				.from(url) //
				.errorDrawable(new ColorDrawable(color(R.color.colorError))) //
				.load(imageView); //
	}
	
	public void clearImageCache() {
		ImageLoader imageLoader = ImageLoader.with(boxPlayApplication);
		imageLoader.clearMemoryCache();
		imageLoader.clearStorageCache();
		imageLoader.clearAllCaches();
	}
	
	/*
	 * Video Help
	 */
	private static VideoGroup passingVideoGroup;
	
	public void startVideoActivity(View view, VideoGroup group) {
		passingVideoGroup = group;
		
		Intent intent = new Intent(boxPlayActivity, VideoActivity.class);
		
		String transitionName = boxPlayActivity.getString(R.string.transition_view_reveal);
		
		View viewStart = view;
		
		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(boxPlayActivity, viewStart, transitionName);
		ActivityCompat.startActivity(boxPlayActivity, intent, options.toBundle());
		
		// boxPlayActivity.startActivity(new Intent(boxPlayActivity, VideoActivity.class));
	}
	
	public VideoGroup getPassingVideoGroup() {
		return passingVideoGroup;
	}
	
	public String formatVideoFile(VideoFile video) {
		VideoSeason season = video.getParentSeason();
		VideoGroup group = season.getParentGroup();
		
		String raw;
		if (group.hasSeason()) {
			raw = boxPlayActivity.getString(R.string.boxplay_store_video_activity_vlc_title);
			
			return String.format(raw, //
					enumToStringCacheTranslation(video.getFileType()).toUpperCase(), //
					group.getTitle(), //
					season.getSeasonValue(), //
					enumToStringCacheTranslation(video.getVideoType()).toLowerCase(), //
					video.getRawEpisodeValue() //
			);
		} else {
			raw = boxPlayActivity.getString(R.string.boxplay_store_video_activity_vlc_title_movie);
			
			return String.format(raw, //
					enumToStringCacheTranslation(video.getFileType()).toUpperCase(), //
					group.getTitle() //
			);
		}
	}
	
	/*
	 * Music Help
	 */
	private static MusicElement passingMusicElement;
	
	public void startMusiecActivity(MusicElement element) {
		if (element == null) {
			return;
		}
		
		if (element instanceof MusicFile && ((MusicFile) element).getParentAlbum() != null) {
			element = ((MusicFile) element).getParentAlbum();
		}
		
		passingMusicElement = element;
		boxPlayActivity.startActivity(new Intent(boxPlayActivity, MusicActivity.class));
	}
	
	public MusicElement getPassingMusicElement() {
		return passingMusicElement;
	}
	
	/*
	 * Translation Help
	 */
	public String enumToStringCacheTranslation(Object enumType) {
		return enumToStringCacheTranslation(enumType, false);
	}
	
	public String enumToStringCacheTranslation(Object enumType, boolean moreThanOne) {
		if (enumType == null) {
			return null;
		}
		
		String raw = enumCacheTranslation.get(enumType);
		
		if (raw == null) {
			return String.valueOf(enumType);
		}
		
		if (!raw.contains("%s")) {
			return raw;
		}
		return String.format(raw, moreThanOne ? "s" : "");
	}
	
	/*
	 * VLC Help
	 */
	private static boolean vlcInstalled = false;
	
	public boolean isVlcInstalled() {
		return vlcInstalled;
	}
	
	public void checkVlc() {
		vlcInstalled = ApplicationUtils.isAppInstalled(boxPlayActivity, "org.videolan.vlc");
	}
	
	/*
	 * Time Help
	 */
	public static final SimpleDateFormat DATEFORMAT_VIDEO_DURATION = new SimpleDateFormat("HH:mm:ss");
	
	/*
	 * Classes
	 */
	
	class MenuIdItem {
		private int id;
		private boolean allowSearch;
		
		public MenuIdItem(int id) {
			this(id, false);
		}
		
		public MenuIdItem(int id, boolean allowSearch) {
			this.id = id;
			this.allowSearch = allowSearch;
		}
		
		public int getId() {
			return id;
		}
		
		public boolean isSearchAllowed() {
			return allowSearch;
		}
	}
	
	public MenuIdItem getMenuIdItemById(int id) {
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
			if (menuIdItem.getId() == id) {
				return menuIdItem;
			}
		}
		return null;
	}
	
	/*
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static int color(@ColorRes int colorRessource) {
		if (Build.VERSION.SDK_INT < 23) {
			return BoxPlayApplication.getBoxPlayApplication().getResources().getColor(colorRessource);
		} else {
			return BoxPlayApplication.getBoxPlayApplication().getColor(colorRessource);
		}
	}
	
}