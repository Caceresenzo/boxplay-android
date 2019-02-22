package caceresenzo.apps.boxplay.helper.implementations;

import java.util.HashMap;
import java.util.Map;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory;
import caceresenzo.apps.boxplay.helper.AbstractHelper;
import caceresenzo.apps.boxplay.helper.HelperManager;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalDataType;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderSearchCapability.SearchCapability;
import caceresenzo.libs.boxplay.models.element.enums.ElementLanguage;
import caceresenzo.libs.boxplay.models.store.video.enums.VideoFileType;
import caceresenzo.libs.boxplay.models.store.video.enums.VideoType;

public class CacheHelper extends AbstractHelper {
	
	/* Cache */
	private Map<Object, String> cache = new HashMap<>();
	
	/* Constructor */
	public CacheHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	@Override
	public void initialize(HelperManager helperManager) {
		super.initialize(helperManager);
		
		initializeCache();
	}
	
	private void initializeCache() {
		fillCache();
	}
	
	private void destroyCache() {
		cache.clear();
	}
	
	private void fillCache() {
		/* Store > Video */
		cache(VideoFileType.ANIME, R.string.boxplay_store_video_file_type_anime);
		cache(VideoFileType.SERIE, R.string.boxplay_store_video_file_type_serie);
		cache(VideoFileType.ANIMEMOVIE, R.string.boxplay_store_video_file_type_animemovie);
		cache(VideoFileType.MOVIE, R.string.boxplay_store_video_file_type_movie);
		cache(VideoFileType.UNKNOWN, R.string.boxplay_store_video_enum_unknown);
		
		cache(VideoType.EPISODE, R.string.boxplay_store_video_type_episode);
		cache(VideoType.OAV, R.string.boxplay_store_video_type_oav);
		cache(VideoType.SPECIAL, R.string.boxplay_store_video_type_special);
		cache(VideoType.MOVIE, R.string.boxplay_store_video_type_movie);
		cache(VideoType.OTHER, R.string.boxplay_store_video_type_other);
		cache(VideoType.UNKNOWN, R.string.boxplay_store_video_enum_unknown);
		
		cache(ElementLanguage.FR, R.string.boxplay_store_video_language_french);
		cache(ElementLanguage.ENSUBFR, R.string.boxplay_store_video_language_english_subtitle_french);
		cache(ElementLanguage.JPSUBFR, R.string.boxplay_store_video_language_japanese_subtitle_french);
		cache(ElementLanguage.UNKNOWN, R.string.boxplay_store_video_enum_unknown);
		
		cache(VideoStoreSubCategory.YOURLIST, R.string.boxplay_store_video_category_your_list);
		cache(VideoStoreSubCategory.RECOMMENDED, R.string.boxplay_store_video_category_recommended);
		cache(VideoStoreSubCategory.ANIMES, R.string.boxplay_store_video_category_animes);
		cache(VideoStoreSubCategory.MOVIES, R.string.boxplay_store_video_category_movies);
		cache(VideoStoreSubCategory.SERIES, R.string.boxplay_store_video_category_series);
		cache(VideoStoreSubCategory.RANDOM, R.string.boxplay_store_video_category_random);
		cache(VideoStoreSubCategory.RELEASE, R.string.boxplay_store_video_category_release);
		
		/* Culture > Search n' Go */
		cache(SearchCapability.ANIME, R.string.boxplay_culture_searchngo_search_result_type_anime);
		cache(SearchCapability.DRAMA, R.string.boxplay_culture_searchngo_search_result_type_drama);
		cache(SearchCapability.SERIES, R.string.boxplay_culture_searchngo_search_result_type_series);
		cache(SearchCapability.VIDEO, R.string.boxplay_culture_searchngo_search_result_type_video);
		cache(SearchCapability.HENTAI, R.string.boxplay_culture_searchngo_search_result_type_hentai);
		cache(SearchCapability.TOKUSATSU, R.string.boxplay_culture_searchngo_search_result_type_tokusatsu);
		cache(SearchCapability.OST, R.string.boxplay_culture_searchngo_search_result_type_ost);
		cache(SearchCapability.MANGA, R.string.boxplay_culture_searchngo_search_result_type_manga);
		cache(SearchCapability.MOVIE, R.string.boxplay_culture_searchngo_search_result_type_movie);
		cache(SearchCapability.DEFAULT, R.string.boxplay_culture_searchngo_search_result_type_default);
		
		cache(AdditionalDataType.THUMBNAIL, R.string.boxplay_culture_searchngo_search_result_data_type_thumbnail);
		cache(AdditionalDataType.NAME, R.string.boxplay_culture_searchngo_search_result_data_type_name);
		cache(AdditionalDataType.ORIGINAL_NAME, R.string.boxplay_culture_searchngo_search_result_data_type_original_name);
		cache(AdditionalDataType.ALTERNATIVE_NAME, R.string.boxplay_culture_searchngo_search_result_data_type_alternative_name);
		cache(AdditionalDataType.OTHER_NAME, R.string.boxplay_culture_searchngo_search_result_data_type_other_name);
		cache(AdditionalDataType.TYPE, R.string.boxplay_culture_searchngo_search_result_data_type_type);
		cache(AdditionalDataType.QUALITY, R.string.boxplay_culture_searchngo_search_result_data_type_quality);
		cache(AdditionalDataType.VERSION, R.string.boxplay_culture_searchngo_search_result_data_type_version);
		cache(AdditionalDataType.RANK, R.string.boxplay_culture_searchngo_search_result_data_type_version);
		cache(AdditionalDataType.TRADUCTION_TEAM, R.string.boxplay_culture_searchngo_search_result_data_type_traduction_team);
		cache(AdditionalDataType.GENDERS, R.string.boxplay_culture_searchngo_search_result_data_type_genders);
		cache(AdditionalDataType.LAST_CHAPTER, R.string.boxplay_culture_searchngo_search_result_data_type_last_chapter);
		cache(AdditionalDataType.STATUS, R.string.boxplay_culture_searchngo_search_result_data_type_status);
		cache(AdditionalDataType.COUNTRY, R.string.boxplay_culture_searchngo_search_result_data_type_country);
		cache(AdditionalDataType.DIRECTOR, R.string.boxplay_culture_searchngo_search_result_data_type_director);
		cache(AdditionalDataType.AUTHORS, R.string.boxplay_culture_searchngo_search_result_data_type_authors);
		cache(AdditionalDataType.ACTORS, R.string.boxplay_culture_searchngo_search_result_data_type_actors);
		cache(AdditionalDataType.ARTISTS, R.string.boxplay_culture_searchngo_search_result_data_type_artists);
		cache(AdditionalDataType.STUDIOS, R.string.boxplay_culture_searchngo_search_result_data_type_studios);
		cache(AdditionalDataType.CHANNELS, R.string.boxplay_culture_searchngo_search_result_data_type_channels);
		cache(AdditionalDataType.LAST_UPDATED, R.string.boxplay_culture_searchngo_search_result_data_type_last_updated);
		cache(AdditionalDataType.RELEASE_DATE, R.string.boxplay_culture_searchngo_search_result_data_type_release_date);
		cache(AdditionalDataType.ANIMATION_STUDIO, R.string.boxplay_culture_searchngo_search_result_data_type_animation_studio);
		cache(AdditionalDataType.PUBLISHERS, R.string.boxplay_culture_searchngo_search_result_data_type_publishers);
		cache(AdditionalDataType.VIEWS, R.string.boxplay_culture_searchngo_search_result_data_type_views);
		cache(AdditionalDataType.DURATION, R.string.boxplay_culture_searchngo_search_result_data_type_duration);
		cache(AdditionalDataType.UNDER_LICENSE, R.string.boxplay_culture_searchngo_search_result_data_type_under_license);
		cache(AdditionalDataType.RESUME, R.string.boxplay_culture_searchngo_search_result_data_type_resume);
		cache(AdditionalDataType.RATING, R.string.boxplay_culture_searchngo_search_result_data_type_rating);
		cache(AdditionalDataType.SIMPLE_HTML, R.string.boxplay_culture_searchngo_search_result_data_type_simple_html);
		
		cache(AdditionalDataType.ITEM_VIDEO, R.string.boxplay_culture_searchngo_search_result_data_type_item_video);
		cache(AdditionalDataType.ITEM_CHAPTER, R.string.boxplay_culture_searchngo_search_result_data_type_item_chapter);
		
		cache(AdditionalDataType.NULL, R.string.boxplay_culture_searchngo_search_result_data_type_null);
		
		/* Api > Response Status (unused) */
		// register(ApiResponseStatus.ERR_INVALID_PAGE, R.string.boxplay_identification_response_error_err_invalid_page);
		// register(ApiResponseStatus.ERR_INVALID_LIMIT, R.string.boxplay_identification_response_error_err_invalid_limit);
		// register(ApiResponseStatus.ERR_MOVIE_NOT_FOUND, R.string.boxplay_identification_response_error_err_movie_not_found);
		// register(ApiResponseStatus.ERR_MOVIE_NOT_AVAILABLE, R.string.boxplay_identification_response_error_err_movie_not_available);
		// register(ApiResponseStatus.ERR_MOVIE_LIST_UNAVAILABLE, R.string.boxplay_identification_response_error_err_movie_list_unavailable);
		// register(ApiResponseStatus.ERR_SERIES_NOT_FOUND, R.string.boxplay_identification_response_error_err_series_not_found);
		// register(ApiResponseStatus.ERR_SERIES_NOT_AVAILABLE, R.string.boxplay_identification_response_error_err_series_not_available);
		// register(ApiResponseStatus.ERR_SERIES_LIST_UNAVAILABLE, R.string.boxplay_identification_response_error_err_series_list_unavailable);
		// register(ApiResponseStatus.ERR_ANIMES_NOT_FOUND, R.string.boxplay_identification_response_error_err_animes_not_found);
		// register(ApiResponseStatus.ERR_ANIMES_NOT_AVAILABLE, R.string.boxplay_identification_response_error_err_animes_not_available);
		// register(ApiResponseStatus.ERR_ANIMES_LIST_UNAVAILABLE, R.string.boxplay_identification_response_error_err_animes_list_unavailable);
		// register(ApiResponseStatus.ERR_SEASON_NOT_FOUND, R.string.boxplay_identification_response_error_err_season_not_found);
		// register(ApiResponseStatus.ERR_USER_LOGIN, R.string.boxplay_identification_response_error_err_user_login);
		// register(ApiResponseStatus.ERR_USER_NOT_FOUND, R.string.boxplay_identification_response_error_err_user_not_found);
		// register(ApiResponseStatus.ERR_USER_REGISTER, R.string.boxplay_identification_response_error_err_user_register);
		// register(ApiResponseStatus.ERR_USER_ALREADY_EXIST, R.string.boxplay_identification_response_error_err_user_already_exist);
		// register(ApiResponseStatus.ERR_USER_USER_INVALID, R.string.boxplay_identification_response_error_err_user_user_invalid);
		// register(ApiResponseStatus.ERR_USER_INVALID_FORMAT, R.string.boxplay_identification_response_error_err_user_invalid_format);
		// register(ApiResponseStatus.OK, R.string.boxplay_identification_response_ok);
		// register(ApiResponseStatus.UNKNOWN, R.string.boxplay_identification_response_unknown);
	}
	
	public void recache() {		
		boxPlayApplication.toast(R.string.boxplay_viewhelper_recaching).show();
		
		destroyCache();
		initializeCache();
	}
	
	public void cache(Object object, int stringRessourceId) {
		if (boxPlayApplication == null) {
			return;
		}
		
		cache.put(object, boxPlayApplication.getString(stringRessourceId));
	}
	
	public String translate(Object enumType) {
		return translate(enumType, false);
	}
	
	public String translate(Object enumType, boolean moreThanOne) {
		if (enumType == null) {
			return null;
		}
		
		String raw = cache.get(enumType);
		
		if (raw == null) {
			return String.valueOf(enumType);
		}
		
		if (!raw.contains("%s")) {
			return raw;
		}
		
		return String.format(raw, moreThanOne ? "s" : "");
	}
	
}