package caceresenzo.apps.boxplay.fragments.culture.searchngo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.nex3z.flowlayout.FlowLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.ArraySet;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
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
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchAndGoSearchCallback;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchHistoryItem;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.string.StringUtils;
import net.cachapa.expandablelayout.ExpandableLayout;

public class PageCultureSearchAndGoFragment extends BaseBoxPlayFragment {
	
	/* Constants */
	public static final int MAX_CONTENT_ITEM_DISPLAYABLE = 70;
	
	/* Managers */
	private SearchAndGoManager searchAndGoManager;
	
	private DialogCreator dialogCreator;
	
	/* Local lists */
	private List<SearchAndGoResult> results;
	private List<SearchHistoryItem> searchQueryHistory;
	
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
		this.dialogCreator = new DialogCreator();
		
		this.results = new ArrayList<>();
		this.searchQueryHistory = searchAndGoManager.getSearchHistory();
		
		this.searchAndGoManager.bindCallback(new SearchAndGoSearchCallback() {
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
		});
		
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
				dialogCreator.showHistoryDialog();
			}
		});
		historyImageButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (!searchAndGoManager.getSearchHistory().isEmpty() && historyImageButton.isEnabled()) {
					boxPlayApplication.toast(R.string.boxplay_culture_searchngo_history_clear).show();
					searchAndGoManager.getSearchSuggestionSubManager().clear();
					return true;
				}
				
				return false;
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
				new SearchAndGoProviderItemViewHolder(itemView).bind(provider, enabled, new SearchAndGoProviderItemListener() {
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
		
		setSearchBarHidden(false);
		
		return view;
	}
	
	public void searchConfirm() {
		actualQuery = searchBarTextInputEditText.getText().toString();
		
		if (!StringUtils.validate(actualQuery)) {
			return;
		}
		
		searchAndGoManager.search(actualQuery);
		InputMethodUtils.hideKeyboard(boxPlayApplication.getAttachedActivity());
	}
	
	public void updateResultList() {
		applyResultList(results);
	}
	
	public void applyResultList(List<SearchAndGoResult> newResultList) {
		searchResultLinearLayout.removeAllViews();
		
		if (!newResultList.isEmpty()) {
			createNextSearchResultView(new AsyncLayoutInflater(boxPlayApplication.getAttachedActivity()), newResultList.iterator(), searchResultIncrementer.incrementAndGet());
		}
	}
	
	private void createNextSearchResultView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<SearchAndGoResult> searchAndGoResultIterator, final long sourceSearchIncrementation) {
		if (searchAndGoResultIterator.hasNext()) {
			asyncLayoutInflater.inflate(R.layout.item_culture_searchngo_search_element, searchResultLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (sourceSearchIncrementation == searchResultIncrementer.get()) {
						new SearchAndGoResultViewHolder(view).bind(searchAndGoResultIterator.next());
						
						parent.addView(view);
						
						createNextSearchResultView(asyncLayoutInflater, searchAndGoResultIterator, sourceSearchIncrementation);
					}
				}
			});
		}
	}
	
	public void searchStart() {
		setSearchBarHidden(true);
	}
	
	public void searchStop() {
		setSearchBarHidden(false);
	}
	
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
	
	public String getActualQuery() {
		return actualQuery;
	}
	
	public void updateProgress(String progress) {
		actualProgressTextView.setText(progress);
		lastProgressTextView.setText(lastProgress);
		
		lastProgress = progress;
	}
	
	class SearchAndGoResultViewHolder {
		private View view;
		private TextView titleTextView, contentTextView, providerTextView, typeTextView;
		private ImageView thumbnailImageView;
		
		public SearchAndGoResultViewHolder(View itemView) {
			view = itemView;
			
			titleTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_title);
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_content);
			providerTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_provider);
			typeTextView = (TextView) itemView.findViewById(R.id.item_culture_searchngo_search_element_textview_type);
			
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_culture_searchngo_search_element_imageview_thumbnail);
		}
		
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
	 * Class to quickly create dialog used by the Search n' Go fragment
	 * 
	 * Help: https://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
	 * 
	 * TODO: Make a better settings system
	 * 
	 * @author Enzo CACERES
	 */
	class DialogCreator {
		private AlertDialog searchHistoryDialog;
		
		private AlertDialog.Builder createBuilder() {
			return new AlertDialog.Builder(BoxPlayActivity.getBoxPlayActivity());
		}
		
		public void showHistoryDialog() {
			AlertDialog.Builder builder = createBuilder();
			builder.setTitle(getString(R.string.boxplay_culture_searchngo_dialog_search_history));
			
			String[] queryArray = new String[searchQueryHistory.size()];
			
			for (int i = 0; i < searchQueryHistory.size(); i++) {
				queryArray[i] = searchQueryHistory.get(i).getQuery();
			}
			
			builder.setItems(queryArray, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						SearchHistoryItem historyItem = searchQueryHistory.get(which);
						
						historyItem.updateDate();
						applyQuery(historyItem.getQuery());
					} catch (Exception exception) {
						Log.wtf("Error when applying query history", exception);
					}
				}
			});
			
			searchHistoryDialog = builder.create();
			searchHistoryDialog.show();
		}
	}
	
	class SearchAndGoProviderItemViewHolder extends RecyclerView.ViewHolder {
		private CardView containerCardView;
		private TextView contentTextView;
		
		public SearchAndGoProviderItemViewHolder(View itemView) {
			super(itemView);
			
			containerCardView = (CardView) itemView.findViewById(R.id.item_culture_searchandgo_provider_cardview_container);
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_provider_textview_container);
		}
		
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
		
		private void changeCardColor(boolean selected) {
			int colorRessource = R.color.colorBackground;
			
			if (selected) {
				colorRessource = R.color.colorAccent;
			}
			
			containerCardView.setBackgroundColor(ViewHelper.color(colorRessource));
		}
	}
	
	interface SearchAndGoProviderItemListener {
		
		void onClick(View view, boolean nowEnabled);
		
	}
	
}