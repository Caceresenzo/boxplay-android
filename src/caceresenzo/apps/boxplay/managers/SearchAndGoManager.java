package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.util.ArraySet;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.managers.XManagers.SubManager;
import caceresenzo.libs.boxplay.common.extractor.video.modifiers.IHentaiVideoContentProvider;
import caceresenzo.libs.boxplay.culture.searchngo.callback.delegate.CallbackDelegate;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.codec.chartable.JsonCharTable;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.string.SimpleLineStringBuilder;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class SearchAndGoManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = SearchAndGoManager.class.getSimpleName();
	
	/* Constants */
	public static final int MAX_SEARCH_QUERY_COUNT = 10;
	
	/* Managers */
	private PremiumManager premiumManager;
	
	/* Sub Managers */
	private SearchHistorySubManager searchHistorySubManager;
	private List<SearchHistoryItem> queryHistory = new ArrayList<>();
	
	/* Provider */
	private List<SearchAndGoProvider> providers;
	
	/* Callbacks */
	private SearchAndGoSearchCallback callback;
	
	/* Worker */
	private SearchAndGoWorker worker;
	
	/* Variables */
	private List<String> updateMessages;
	
	@Override
	public void initialize() {
		this.premiumManager = BoxPlayApplication.getManagers().getPremiumManager();
		
		this.searchHistorySubManager = new SearchHistorySubManager();
		this.searchHistorySubManager.load();
		
		this.worker = new SearchAndGoWorker();
		
		this.providers = new ArrayList<>();
		this.updateMessages = new ArrayList<>();
		
		readProviders();
	}
	
	@Override
	protected void destroy() {
		this.searchHistorySubManager.save();
	}
	
	/**
	 * Attach a callback to the manager to easily communicate.
	 * 
	 * @param callback
	 *            Target callback.
	 * @return If a search is actually running, if true, you must resume it.
	 */
	public boolean bindCallback(SearchAndGoSearchCallback callback) {
		this.callback = callback;
		
		return worker.isInSearch();
	}
	
	/**
	 * Force to send all already sended message as raw string to be able to resume a search.<br>
	 * All message with be send with {@link SearchAndGoSearchCallback#onForcedResumeMessage(String)}.
	 */
	public void forceSendAllMessage() {
		for (String message : new ArrayList<>(updateMessages)) {
			callback.onForcedResumeMessage(message);
		}
	}
	
	/**
	 * Start a {@link SearchAndGoWorker} and do a search.
	 * 
	 * @param query
	 *            Target query.
	 */
	public void search(String query) {
		if (worker.isRunning()) {
			boxPlayApplication.toast("Worker not available").show();
			return;
		}
		
		worker.updateLocal(query).start();
		
		/* Updating search history */
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
	
	/**
	 * @return A {@link List} of actual enabled {@link SearchAndGoProvider} instanced.
	 */
	public List<SearchAndGoProvider> getProviders() {
		return providers;
	}
	
	/**
	 * Same as {@link #getProviders()}, but this time get only classes.
	 * 
	 * @return A {@link List} of class.
	 */
	public List<Class<? extends SearchAndGoProvider>> getProvidersAsClasses() {
		List<Class<? extends SearchAndGoProvider>> classes = new ArrayList<>();
		
		for (SearchAndGoProvider provider : getProviders()) {
			classes.add(provider.getClass());
		}
		
		return classes;
	}
	
	/**
	 * Read and store in a {@link List} {@link SearchAndGoProvider} that the user has chose.
	 * 
	 * @return A {@link List} of instanced {@link SearchAndGoProvider}.
	 */
	public List<SearchAndGoProvider> readProviders() {
		providers.clear();
		
		Set<String> enabledProvidersSet = getManagers().getPreferences().getStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), createDefaultProviderSet());
		
		for (ProviderManager creatableProvider : ProviderManager.values()) {
			if (enabledProvidersSet.contains(creatableProvider.toString())) {
				providers.add(creatableProvider.create());
			}
		}
		
		return providers;
	}
	
	/**
	 * Create a {@link Set} of {@link ProviderManager} values containing all values.
	 * 
	 * @return A {@link Set} to use as a default value when getting it from a {@link SharedPreferences}.
	 */
	public Set<String> createDefaultProviderSet() {
		ProviderManager[] creatableProviders = ProviderManager.values();
		
		Set<String> defaultValue = new ArraySet<String>();
		for (ProviderManager creatableProvider : creatableProviders) {
			defaultValue.add(creatableProvider.toString());
		}
		
		return defaultValue;
	}
	
	/**
	 * @return A {@link List} of {@link #MAX_SEARCH_QUERY_COUNT} sized history item.
	 */
	public List<SearchHistoryItem> getSearchHistory() {
		return queryHistory;
	}
	
	/**
	 * @return A {@link List} of already sended message for the actual search.
	 */
	public List<String> getUpdateMessages() {
		return updateMessages;
	}
	
	/**
	 * Working thread that will be calling {@link SearchAndGoProvider#provide(List, String, boolean, CallbackDelegate)}.
	 * 
	 * @author Enzo CACERES
	 */
	class SearchAndGoWorker extends WorkerThread {
		
		/* Variables */
		private boolean inSearch;
		private String localSearchQuery;
		private List<SearchAndGoProvider> localProviders;
		
		/* Constructor */
		public SearchAndGoWorker() {
			super();
			
			localProviders = new ArrayList<>();
		}
		
		@Override
		protected void execute() {
			inSearch = true;
			try {
				boolean hentaiAllowed = premiumManager != null && premiumManager.isPremiumKeyValid();
				
				for (SearchAndGoProvider provider : localProviders) {
					if (provider instanceof IHentaiVideoContentProvider) {
						((IHentaiVideoContentProvider) provider).allowHentai(hentaiAllowed);
					}
				}
				
				SearchAndGoProvider.provide(localProviders, localSearchQuery, true, createCallbackDelegate());
			} catch (Exception exception) {
				; /* Handled by callbacks */
			}
			inSearch = false;
		}
		
		/**
		 * Update Worker thread settings.
		 * 
		 * @param query
		 *            Target search query
		 * @return Itself
		 */
		protected SearchAndGoWorker updateLocal(String query) {
			this.localSearchQuery = query;
			
			this.localProviders.clear();
			this.localProviders.addAll(providers);
			
			getUpdateMessages().clear();
			
			return this;
		}
		
		@Override
		protected void done() {
			worker = new SearchAndGoWorker(); /* New instance, this one will be forgot */
		}
		
		/**
		 * @return Searching state
		 */
		public boolean isInSearch() {
			return inSearch;
		}
	}
	
	/**
	 * Create a {@link CallbackDelegate} for the BoxPlay Search n' Go Library and convert it to be used with {@link Handler}.
	 * 
	 * @return
	 */
	private CallbackDelegate createCallbackDelegate() {
		return new CallbackDelegate() {
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
			
		};
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
		
		public void clear() {
			queryHistory.clear();
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
		
		void onForcedResumeMessage(String rawMessage);
		
	}
	
}