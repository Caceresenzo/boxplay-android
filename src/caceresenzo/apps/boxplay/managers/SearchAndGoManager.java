package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.support.v4.util.ArraySet;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.managers.XManagers.SubManager;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.InternetSource;
import caceresenzo.libs.boxplay.common.extractor.image.manga.implementations.GenericMangaLelChapterExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.implementations.AndroidOpenloadVideoExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.implementations.GenericVidozaVideoExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.implementations.OpenloadVideoExtractor;
import caceresenzo.libs.boxplay.culture.searchngo.callback.ProviderSearchCallback;
import caceresenzo.libs.boxplay.culture.searchngo.callback.SearchCallback;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderCallback;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.codec.chartable.JsonCharTable;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.string.SimpleLineStringBuilder;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.HelpedThread;

public class SearchAndGoManager extends AbstractManager {
	
	private static final String TAG = SearchAndGoManager.class.getSimpleName();
	
	public static final int MAX_SEARCH_QUERY_COUNT = 10;
	
	private static final Map<Class<? extends ContentExtractor>, ContentExtractor> EXTRACTORS = new HashMap<>();
	
	static {
		/* Video */
		EXTRACTORS.put(OpenloadVideoExtractor.class, new AndroidOpenloadVideoExtractor());
		EXTRACTORS.put(GenericVidozaVideoExtractor.class, new GenericVidozaVideoExtractor());
		
		/* Manga */
		EXTRACTORS.put(GenericMangaLelChapterExtractor.class, new GenericMangaLelChapterExtractor());
	}
	
	private SearchHistorySubManager searchHistorySubManager;
	
	private Worker worker;
	private List<SearchAndGoProvider> providers;
	
	private SearchAndGoSearchCallback callback;
	
	private List<SearchHistoryItem> queryHistory = new ArrayList<>();
	
	@Override
	public void initialize() {
		this.searchHistorySubManager = new SearchHistorySubManager();
		this.searchHistorySubManager.load();
		
		this.worker = new Worker();
		
		this.providers = new ArrayList<>();
		readProviders();
		
		registerCallbacks();
	}
	
	@Override
	protected void destroy() {
		this.searchHistorySubManager.save();
	}
	
	public ContentExtractor getExtractorFromBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return null;
		}
		
		for (Entry<Class<? extends ContentExtractor>, ContentExtractor> entry : EXTRACTORS.entrySet()) {
			InternetSource internetSource = entry.getValue();
			
			if (internetSource.matchUrl(baseUrl)) {
				Log.i(TAG, String.format("An instance of ContentExtractor has been asked (base url: %s, returned class: %s)", baseUrl, entry.getKey()));
				
				try {
					return entry.getValue().getClass().newInstance();
				} catch (Exception exception) {
					Log.e(TAG, "Failed to instanciate new instance of content explorer, returning saved one", exception);
				}
				
				entry.getValue().getLogger().clear();
				return entry.getValue();
			}
		}
		
		Log.e(TAG, String.format("No compatible ContentExtract instance found for base url %s", baseUrl));
		return null;
	}
	
	public boolean hasCompatibleExtractor(String baseUrl) {
		return getExtractorFromBaseUrl(baseUrl) != null;
	}
	
	public void bindCallback(SearchAndGoSearchCallback callback) {
		this.callback = callback;
	}
	
	public void search(String query) {
		if (worker.isRunning()) {
			boxPlayApplication.toast("Worker not available").show();
			return;
		}
		
		worker.updateLocal(query).start();
		
		/**
		 * Updating search history
		 */
		SearchHistoryItem searchHistoryItem = null;
		for (SearchHistoryItem historyItem : getSearchHistory()) {
			if (historyItem.getQuery().equals(query)) {
				searchHistoryItem = historyItem;
				break;
			}
		}
		
		if (searchHistoryItem == null) {
			searchHistoryItem = new SearchHistoryItem(query);
			getSearchHistory().add(searchHistoryItem);
		} else {
			searchHistoryItem.updateDate();
		}
		
		getSearchSuggestionSubManager().save();
	}
	
	public List<SearchAndGoProvider> getProviders() {
		return providers;
	}
	
	public void readProviders() {
		Set<String> enabledProvidersSet = getManagers().getPreferences().getStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), createDefaultProviderSet());
		
		for (ProviderManager creatableProvider : ProviderManager.values()) {
			if (enabledProvidersSet.contains(creatableProvider.toString())) {
				providers.add(creatableProvider.create());
			}
		}
	}
	
	public Set<String> createDefaultProviderSet() {
		ProviderManager[] creatableProviders = ProviderManager.values();
		
		Set<String> defaultValue = new ArraySet<String>();
		for (ProviderManager creatableProvider : creatableProviders) {
			defaultValue.add(creatableProvider.toString());
		}
		
		return defaultValue;
	}
	
	public List<SearchHistoryItem> getSearchHistory() {
		return queryHistory;
	}
	
	private class Worker extends HelpedThread {
		private String localSearchQuery = "";
		private List<SearchAndGoProvider> localProviders = new ArrayList<>();
		
		@Override
		protected void onRun() {
			try {
				SearchAndGoProvider.provide(localProviders, localSearchQuery, true);
			} catch (Exception exception) {
				; // Handled by callbacks
			}
		}
		
		private Worker updateLocal(String query) {
			this.localSearchQuery = query;
			
			this.localProviders.clear();
			this.localProviders.addAll(providers);
			
			return this;
		}
		
		@Override
		protected void onCancelled() {
			;
		}
		
		@Override
		protected void onFinished() {
			worker = new Worker(); // New instance, this one will be forgot
		}
	}
	
	private void registerCallbacks() {
		ProviderCallback.registerSearchallback(new SearchCallback() { // Unused for now
			@Override
			public void onSearchStarting() {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onSearchStart();
						}
					});
				}
			}
			
			@Override
			public void onSearchSorting() {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onSearchSorting();
						}
					});
				}
			}
			
			@Override
			public void onSearchFinished(final Map<String, SearchAndGoResult> workmap) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onSearchFinish(workmap);
						}
					});
				}
			}
			
			@Override
			public void onSearchFail(final Exception exception) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onSearchFail(exception);
						}
					});
				}
			}
		});
		
		ProviderCallback.registerProviderSearchallback(new ProviderSearchCallback() {
			@Override
			public void onProviderSearchStarting(final SearchAndGoProvider provider) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onProviderStarted(provider);
						}
					});
				}
			}
			
			@Override
			public void onProviderSorting(final SearchAndGoProvider provider) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onProviderSorting(provider);
						}
					});
				}
			}
			
			@Override
			public void onProviderSearchFinished(final SearchAndGoProvider provider, final Map<String, SearchAndGoResult> workmap) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onProviderFinished(provider, workmap);
						}
					});
				}
			}
			
			@Override
			public void onProviderFailed(final SearchAndGoProvider provider, final Exception exception) {
				if (callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							callback.onProviderSearchFail(provider, exception);
						}
					});
				}
			}
		});
	}
	
	public SearchHistorySubManager getSearchSuggestionSubManager() {
		return searchHistorySubManager;
	}
	
	public class SearchHistorySubManager extends SubManager implements JsonCharTable {
		private final String JSON_KEY_HISTORY = "history";
		private final String JSON_KEY_HISTORY_QUERY = "query";
		private final String JSON_KEY_HISTORY_DATE = "date";
		
		private File suggestionFile = new File(getManagers().getBaseDataDirectory(), "search_history.json");
		
		private final Comparator<SearchHistoryItem> QUERY_COMPARATOR = new Comparator<SearchHistoryItem>() {
			@Override
			public int compare(SearchHistoryItem item1, SearchHistoryItem item2) {
				return (int) (item2.getDate().getTime() - item1.getDate().getTime());
			}
		};
		
		@SuppressWarnings("unchecked")
		public void load() {
			queryHistory.clear();
			
			try {
				JsonObject json = (JsonObject) new JsonParser().parse(StringUtils.fromFile(suggestionFile));
				
				List<Map<String, Object>> searchHistoryList = (List<Map<String, Object>>) json.get(JSON_KEY_HISTORY);
				
				for (Map<String, Object> searchHistoryDataMap : searchHistoryList) {
					String query = ParseUtils.parseString(searchHistoryDataMap.get(JSON_KEY_HISTORY_QUERY), null);
					long date = ParseUtils.parseLong(searchHistoryDataMap.get(JSON_KEY_HISTORY_DATE), -1);
					
					if (query == null || date == -1) {
						continue;
					}
					
					queryHistory.add(new SearchHistoryItem(query, date));
				}
				
				Collections.sort(queryHistory, QUERY_COMPARATOR);
				// queryHistory.sort(QUERY_COMPARATOR);
			} catch (Exception exception) {
				;
			}
			
			save();
		}
		
		public void save() {
			// queryHistory.sort(QUERY_COMPARATOR);
			Collections.sort(queryHistory, QUERY_COMPARATOR);
			
			while (queryHistory.size() > MAX_SEARCH_QUERY_COUNT) {
				queryHistory.remove(queryHistory.size() - 1);
			}
			
			SimpleLineStringBuilder builder = new SimpleLineStringBuilder();
			
			builder.appendln("{");
			
			builder.appendln(TAB + "\"" + JSON_KEY_HISTORY + "\": [");
			
			List<String> alreadySavedQuery = new ArrayList<>();
			
			Iterator<SearchHistoryItem> iterator = queryHistory.iterator();
			while (iterator.hasNext()) {
				SearchHistoryItem suggestion = iterator.next();
				
				if (alreadySavedQuery.contains(suggestion.getQuery())) {
					continue;
				}
				alreadySavedQuery.add(suggestion.getQuery());
				
				builder.appendln(TAB + TAB + "{");
				
				builder.appendln(TAB + TAB + TAB + "\"" + JSON_KEY_HISTORY_QUERY + "\": \"" + (suggestion.getQuery().replace("\\", "\\\\").replace("\"", "\\\"")) + "\",");
				builder.appendln(TAB + TAB + TAB + "\"" + JSON_KEY_HISTORY_DATE + "\": " + suggestion.getDate().getTime() + "");
				
				builder.appendln(TAB + TAB + "}" + (iterator.hasNext() ? "," : ""));
			}
			
			builder.appendln(TAB + "]");
			
			builder.appendln("}");
			
			try {
				getManagers().writeLocalFile(suggestionFile, builder.toString());
			} catch (Exception exception) {
				Log.e(getClass().getSimpleName(), "Failed to save search history.", exception);
			}
		}
	}
	
	/**
	 * Class to hold information about a search history
	 * 
	 * @author Enzo CACERES
	 */
	public static class SearchHistoryItem {
		private final String query;
		private long date;
		
		public SearchHistoryItem(String query) {
			this(query, System.currentTimeMillis());
		}
		
		public SearchHistoryItem(String query, long date) {
			this.query = query;
			this.date = date;
		}
		
		public String getQuery() {
			return query;
		}
		
		public Date getDate() {
			return new Date(date);
		}
		
		public void updateDate() {
			setDate(new Date(System.currentTimeMillis()));
		}
		
		public void setDate(Date date) {
			this.date = date.getTime();
		}
		
		@Override
		public String toString() {
			return "SearchHistoryItem[query=" + query + ", date=" + date + "]";
		}
	}
	
	public static interface SearchAndGoSearchCallback {
		
		void onSearchStart();
		
		void onSearchSorting();
		
		void onSearchFinish(Map<String, SearchAndGoResult> workmap);
		
		void onSearchFail(Exception exception);
		
		void onProviderStarted(SearchAndGoProvider provider);
		
		void onProviderSorting(SearchAndGoProvider provider);
		
		void onProviderFinished(SearchAndGoProvider provider, Map<String, SearchAndGoResult> workmap);
		
		void onProviderSearchFail(SearchAndGoProvider provider, Exception exception);
		
	}
	
}