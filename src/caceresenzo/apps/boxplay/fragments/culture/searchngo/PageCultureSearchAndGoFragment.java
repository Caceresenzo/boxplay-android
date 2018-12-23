package caceresenzo.apps.boxplay.fragments.culture.searchngo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.nex3z.flowlayout.FlowLayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.ArraySet;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import caceresenzo.android.libs.input.InputMethodUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoHistoryActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchAndGoSearchCallback;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderWeakCache;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.bytes.ByteFormat;
import caceresenzo.libs.string.StringUtils;
import net.cachapa.expandablelayout.ExpandableLayout;

public class PageCultureSearchAndGoFragment extends BaseBoxPlayFragment {
	
	/* Constants */
	public static final int MAX_CONTENT_ITEM_DISPLAYABLE = 70;
	
	/* Managers */
	private SearchAndGoManager searchAndGoManager;
	
	/* Local lists */
	private List<SearchAndGoResult> results;
	
	/* Views */
	private TextInputEditText searchBarTextInputEditText;
	private TextInputLayout searchBarContainerTextInputLayout;
	private RelativeLayout progressContainerRelativeLayout;
	private TextView actualProgressTextView, lastProgressTextView;
	private ImageButton historyImageButton;
	private LinearLayout searchResultLinearLayout;
	private LinearLayout providerFlowingListContainerLinearLayout;
	private RelativeLayout expandRelativeLayout;
	private ImageSwitcher arrowImageSwitcher;
	private ExpandableLayout settingsExpandableLayout;
	private Button clearCacheButton;
	private ProgressBar loadingProgressBar;
	private FrameLayout informationContainerFrameLayout;
	private TextView informationTextView;
	
	/* Variables */
	private AtomicLong searchResultIncrementer;
	private String actualQuery;
	private String lastProgress;
	
	/* Constructor */
	public PageCultureSearchAndGoFragment() {
		super();
		
		this.searchAndGoManager = BoxPlayApplication.getManagers().getSearchAndGoManager();
		
		this.results = new ArrayList<>();
		
		this.searchResultIncrementer = new AtomicLong();
		this.actualQuery = "";
		this.lastProgress = "-";
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo, container, false);
		
		searchBarContainerTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_culture_searchngo_textinputlayout_searchbar_container);
		searchBarTextInputEditText = (TextInputEditText) view.findViewById(R.id.fragment_culture_searchngo_textinputedittext_searchbar);
		progressContainerRelativeLayout = (RelativeLayout) view.findViewById(R.id.fragment_culture_searchngo_relativelayout_progress_container);
		actualProgressTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_progress_actual);
		lastProgressTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_progress_last);
		historyImageButton = (ImageButton) view.findViewById(R.id.fragment_culture_searchngo_imagebutton_history);
		searchResultLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_culture_searchngo_linearlayout_search_result);
		settingsExpandableLayout = (ExpandableLayout) view.findViewById(R.id.fragment_culture_searchngo_expandablelayout_container);
		providerFlowingListContainerLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_culture_searchngo_linearlayout_provider_flowing_list_container);
		expandRelativeLayout = (RelativeLayout) view.findViewById(R.id.fragment_culture_searchngo_relativelayout_expand_button);
		arrowImageSwitcher = (ImageSwitcher) view.findViewById(R.id.fragment_culture_searchngo_imageswitcher_expand_arrow);
		clearCacheButton = (Button) view.findViewById(R.id.fragment_culture_searchngo_flowlayout_memory_clear_cache);
		loadingProgressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_progressbar_loading);
		informationContainerFrameLayout = (FrameLayout) view.findViewById(R.id.fragment_culture_searchngo_framelayout_info_container);
		informationTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_info_text);
		
		searchBarTextInputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					searchConfirm();
					
					return true;
				}
				
				return false;
			}
		});
		
		historyImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				SearchAndGoHistoryActivity.start();
			}
		});
		
		expandRelativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (settingsExpandableLayout.isExpanded()) {
					settingsExpandableLayout.collapse();
				} else {
					settingsExpandableLayout.expand();
				}
			}
		});
		
		settingsExpandableLayout.setOnExpansionUpdateListener(new ExpandableLayout.OnExpansionUpdateListener() {
			int oldArrowRessource = -1;
			
			@Override
			public void onExpansionUpdate(float expansionFraction, int state) {
				int arrowRessource = -1;
				
				switch (state) {
					case ExpandableLayout.State.COLLAPSING: {
						arrowRessource = R.drawable.icon_keyboard_arrow_down_white_24dp;
						break;
					}
					
					case ExpandableLayout.State.EXPANDING: {
						arrowRessource = R.drawable.icon_keyboard_arrow_up_white_24dp;
						break;
					}
				}
				
				if (arrowRessource != -1 && oldArrowRessource != arrowRessource) {
					oldArrowRessource = arrowRessource;
					
					arrowImageSwitcher.setImageResource(arrowRessource);
				}
			}
		});
		
		Animation in = AnimationUtils.loadAnimation(context, R.anim.fade_in);
		Animation out = AnimationUtils.loadAnimation(context, R.anim.fade_out);
		arrowImageSwitcher.setInAnimation(in);
		arrowImageSwitcher.setOutAnimation(out);
		arrowImageSwitcher.setFactory(new ViewFactory() {
			public View makeView() {
				return new ImageView(context);
			}
		});
		expandRelativeLayout.callOnClick();
		
		/* Flowing-list of Providers */
		{
			final ProviderManager[] creatableProviders = ProviderManager.values();
			Set<String> enabledProvidersSet = boxPlayApplication.getPreferences().getStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), searchAndGoManager.createDefaultProviderSet());
			
			final SearchAndGoProvider[] instancedSearchAndGoProviders = new SearchAndGoProvider[creatableProviders.length];
			final boolean[] enabledStates = new boolean[creatableProviders.length];
			
			FlowLayout flowLayout = view.findViewById(R.id.fragment_culture_searchngo_flowlayout_provider_container);
			for (int i = 0; i < creatableProviders.length; i++) {
				ProviderManager manager = creatableProviders[i];
				SearchAndGoProvider provider = instancedSearchAndGoProviders[i] = manager.create();
				
				boolean enabled = enabledStates[i] = enabledProvidersSet.contains(manager.toString());
				
				View itemView = LayoutInflater.from(context).inflate(R.layout.item_culture_searchandgo_provider, flowLayout, false);
				
				final int providerIndex = i;
				new SearchAndGoProviderItemViewBinder(itemView).bind(provider, enabled, new SearchAndGoProviderItemListener() {
					@Override
					public void onClick(View view, boolean nowEnabled) {
						enabledStates[providerIndex] = nowEnabled;
						
						/* Saving everything */
						Set<String> newEnabledProviders = new ArraySet<>();
						List<SearchAndGoProvider> actualProviders = searchAndGoManager.getProviders();
						
						actualProviders.clear();
						
						for (int i = 0; i < creatableProviders.length; i++) {
							if (enabledStates[i]) {
								actualProviders.add(instancedSearchAndGoProviders[i]);
								
								newEnabledProviders.add(creatableProviders[i].toString());
							}
						}
						
						boxPlayApplication.getPreferences().edit().putStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), newEnabledProviders).commit();
					}
				});
				
				flowLayout.addView(itemView);
			}
		}
		
		clearCacheButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int memorySize = ProviderWeakCache.computeMemorySizeAndDestroy();
				
				boxPlayApplication.toast(R.string.boxplay_culture_searchngo_settings_memory_clear_cache_cleared, ByteFormat.toHumanBytes(memorySize, 1)).show();
			}
		});
		
		boolean inSearch = this.searchAndGoManager.bindCallback(new SearchAndGoSearchCallback() {
			private String getString(int ressourceId, Object... formatArgs) { /* Avoid un-contextualized fragments */
				return boxPlayApplication.getString(ressourceId, formatArgs);
			}
			
			@Override
			public void onSearchStart() {
				searchStart();
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_global_started));
			}
			
			@Override
			public void onSearchSorting() {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_global_sorting));
			}
			
			@Override
			public void onSearchFinish(Map<String, SearchAndGoResult> workmap) {
				results.clear();
				results.addAll(workmap.values());
				
				if (results.size() > MAX_CONTENT_ITEM_DISPLAYABLE) {
					boxPlayApplication.toast(R.string.boxplay_culture_searchngo_content_limit_reached, results.size(), MAX_CONTENT_ITEM_DISPLAYABLE).show();
					
					while (results.size() > MAX_CONTENT_ITEM_DISPLAYABLE) {
						results.remove(results.size() - 1);
					}
				}
				
				updateResultList();
				
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_global_finished));
				searchStop();
			}
			
			@Override
			public void onSearchFail(Exception exception) {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_global_failed));
				searchStop();
			}
			
			@Override
			public void onProviderStarted(SearchAndGoProvider provider) {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_provider_started, provider.getSiteName()));
			}
			
			@Override
			public void onProviderSorting(SearchAndGoProvider provider) {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_provider_sorting, provider.getSiteName()));
			}
			
			@Override
			public void onProviderFinished(SearchAndGoProvider provider, Map<String, SearchAndGoResult> workmap) {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_provider_finished, provider.getSiteName()));
			}
			
			@Override
			public void onProviderSearchFail(SearchAndGoProvider provider, Exception exception) {
				updateProgress(getString(R.string.boxplay_culture_searchngo_search_status_provider_failed, provider.getSiteName(), exception.getLocalizedMessage()));
			}
			
			@Override
			public void onForcedResumeMessage(String rawMessage) {
				updateProgress(rawMessage);
			}
		});
		setSearchBarHidden(inSearch);
		
		if (inSearch) {
			searchAndGoManager.forceSendAllMessage();
		}
		
		return view;
	}
	
	/**
	 * Confirm a search.<br>
	 * This will check the actual query and call {@link SearchAndGoManager#search(String)} to start the full search.<br>
	 * This will also call {@link InputMethodUtils#hideKeyboard(Activity)}.
	 */
	public void searchConfirm() {
		actualQuery = searchBarTextInputEditText.getText().toString();
		
		if (!StringUtils.validate(actualQuery)) {
			return;
		}
		
		searchAndGoManager.search(actualQuery);
		InputMethodUtils.hideKeyboard(boxPlayApplication.getAttachedActivity());
	}
	
	/**
	 * Refresh actual result list by applying the same list with {@link #applyResultList(List)}.
	 */
	public void updateResultList() {
		applyResultList(results);
	}
	
	/**
	 * Apply a new list and start {@link View} creation by recursively calling {@link #createNextSearchResultView(AsyncLayoutInflater, Iterator, long)}.
	 * 
	 * @param newResultList
	 *            New result list from the query.
	 */
	public void applyResultList(List<SearchAndGoResult> newResultList) {
		searchResultLinearLayout.removeAllViews();
		
		if (!newResultList.isEmpty()) {
			createNextSearchResultView(new AsyncLayoutInflater(boxPlayApplication.getAttachedActivity()), newResultList.iterator(), searchResultIncrementer.incrementAndGet());
		}
	}
	
	/**
	 * Create {@link View} recursively.
	 * 
	 * @param asyncLayoutInflater
	 *            Global {@link AsyncLayoutInflater} inflating {@link View}s.
	 * @param searchAndGoResultIterator
	 *            Actual {@link Iterator} of the result {@link List}.
	 * @param sourceSearchIncrementation
	 *            Source id used when creating the first {@link View} just to be sure that were are not overlapping 2 different search results.
	 */
	private void createNextSearchResultView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<SearchAndGoResult> searchAndGoResultIterator, final long sourceSearchIncrementation) {
		if (searchAndGoResultIterator.hasNext()) {
			asyncLayoutInflater.inflate(R.layout.item_culture_searchngo_search_element, searchResultLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (sourceSearchIncrementation == searchResultIncrementer.get()) {
						new SearchAndGoResultViewBinder(view).bind(searchAndGoResultIterator.next());
						
						parent.addView(view);
						
						createNextSearchResultView(asyncLayoutInflater, searchAndGoResultIterator, sourceSearchIncrementation);
					}
				}
			});
		}
	}
	
	/**
	 * Call when the search has started, this will hide unwanted elements.
	 */
	public void searchStart() {
		setSearchBarHidden(true);
	}
	
	/**
	 * Call when the search has finish, this will show wanted elements.
	 */
	public void searchStop() {
		setSearchBarHidden(false);
	}
	
	/**
	 * From 2 possible state, in search or not, hide or display elements.
	 * 
	 * @param hidden
	 *            State.
	 */
	public void setSearchBarHidden(boolean hidden) {
		historyImageButton.setEnabled(!hidden);
		
		progressContainerRelativeLayout.setVisibility(hidden ? View.VISIBLE : View.GONE);
		searchBarTextInputEditText.setVisibility(hidden ? View.GONE : View.VISIBLE);
		searchBarContainerTextInputLayout.setVisibility(searchBarTextInputEditText.getVisibility());
		historyImageButton.setVisibility(searchBarTextInputEditText.getVisibility());
		providerFlowingListContainerLinearLayout.setVisibility(searchBarTextInputEditText.getVisibility());
		searchResultLinearLayout.setVisibility(hidden ? View.GONE : View.VISIBLE);
		
		loadingProgressBar.setVisibility(hidden ? View.VISIBLE : View.GONE);
		
		informationContainerFrameLayout.setVisibility(View.GONE);
		
		if (searchAndGoManager.getProviders().isEmpty() && !hidden) {
			informationContainerFrameLayout.setVisibility(View.VISIBLE);
			searchResultLinearLayout.setVisibility(View.GONE);
			informationTextView.setText(R.string.boxplay_culture_searchngo_info_no_provider);
		}
	}
	
	/**
	 * Apply custom query to the search bar.
	 * 
	 * @param query
	 *            Target query.
	 */
	public void applyQuery(String query) {
		if (!StringUtils.validate(query)) {
			return;
		}
		
		if (searchBarTextInputEditText != null) {
			searchBarTextInputEditText.setText(query);
			searchConfirm();
			
			actualQuery = query;
		}
	}
	
	/**
	 * @return Actual query.
	 */
	public String getActualQuery() {
		if (searchBarTextInputEditText != null) {
			actualQuery = searchBarTextInputEditText.getText().toString();
		}
		
		return actualQuery;
	}
	
	/**
	 * Change actual progress information.<br>
	 * This will set a new progress to the main {@link TextView}, and set actual (before new) progress to the little {@link TextView}.
	 * 
	 * @param progress
	 *            New progress string.
	 */
	public void updateProgress(String progress) {
		searchAndGoManager.getUpdateMessages().add(progress);
		
		actualProgressTextView.setText(progress);
		lastProgressTextView.setText(lastProgress);
		
		lastProgress = progress;
	}
	
	/**
	 * View binder to quicly bind layout to item.
	 * 
	 * @author Enzo CACERES
	 */
	class SearchAndGoResultViewBinder {
		
		/* Views */
		private View view;
		private TextView titleTextView, contentTextView, providerTextView, typeTextView;
		private ImageView thumbnailImageView;
		
		/* Constructor */
		public SearchAndGoResultViewBinder(View itemView) {
			view = itemView;
			
			titleTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_title);
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_content);
			providerTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_provider);
			typeTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_type);
			
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_culture_searchngo_search_element_imageview_thumbnail);
		}
		
		/**
		 * Bind loaded view with this item.
		 * 
		 * @param result
		 *            Target result.
		 */
		public void bind(final SearchAndGoResult result) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					SearchAndGoDetailActivity.start(result);
				}
			});
			
			titleTextView.setText(result.getName());
			contentTextView.setText(result.hasDescription() ? result.getDescription() : "-/-");
			providerTextView.setText(result.getParentProvider().getSiteName().toUpperCase());
			typeTextView.setText(viewHelper.enumToStringCacheTranslation(result.getType()));
			
			viewHelper.downloadToImageView(thumbnailImageView, result.getBestImageUrl(), result.getRequireHeaders());
		}
	}
	
	/**
	 * Advanced view binder to bind SearchAndGoProvider item to layout and allow real-time saving and usability for the {@link SearchAndGoManager}.
	 * 
	 * @author Enzo CACERES
	 */
	class SearchAndGoProviderItemViewBinder {
		
		/* Views */
		private CardView containerCardView;
		private TextView contentTextView;
		
		/* Constructor */
		public SearchAndGoProviderItemViewBinder(View itemView) {
			containerCardView = (CardView) itemView.findViewById(R.id.item_culture_searchandgo_provider_cardview_container);
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_provider_textview_container);
		}
		
		/**
		 * Bind the item to the loaded view.
		 * 
		 * @param provider
		 *            Target provider instance.
		 * @param enabled
		 *            If the provider is already enabled.
		 * @param searchAndGoProviderItemListener
		 *            Callback.
		 */
		public void bind(final SearchAndGoProvider provider, boolean enabled, final SearchAndGoProviderItemListener searchAndGoProviderItemListener) {
			contentTextView.setText(provider.getSiteName());
			
			containerCardView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					boolean contain = searchAndGoManager.getProvidersAsClasses().contains(provider.getClass());
					
					changeCardColor(!contain);
					searchAndGoProviderItemListener.onClick(view, !contain);
				}
			});
			
			changeCardColor(enabled);
		}
		
		/**
		 * Change {@link CardView} color to "disable the outline" and make it fully colored.
		 * 
		 * @param selected
		 *            If the {@link SearchAndGoProvider} is now selected.
		 */
		private void changeCardColor(boolean selected) {
			int colorRessource = R.color.colorBackground;
			
			if (selected) {
				colorRessource = R.color.colorAccent;
			}
			
			containerCardView.setBackgroundColor(ViewHelper.color(colorRessource));
		}
	}
	
	/**
	 * Simple Callback.
	 * 
	 * @author Enzo CACERES
	 */
	interface SearchAndGoProviderItemListener {
		
		/**
		 * Called when the provider's view has been clicked.
		 * 
		 * @param view
		 *            Original view.
		 * @param nowEnabled
		 *            New enabled state.
		 */
		void onClick(View view, boolean nowEnabled);
		
	}
	
}