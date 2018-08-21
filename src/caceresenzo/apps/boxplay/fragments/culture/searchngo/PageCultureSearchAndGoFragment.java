package caceresenzo.apps.boxplay.fragments.culture.searchngo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.MaterialSearchBar.OnSearchActionListener;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchAndGoSearchCallback;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchHistoryItem;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;

public class PageCultureSearchAndGoFragment extends Fragment {
	
	public static final int MAX_CONTENT_ITEM_DISPLAYABLE = 70;
	
	private BoxPlayApplication boxPlayApplication;
	
	private SearchAndGoManager searchAndGoManager;
	
	private DialogCreator dialogCreator;
	
	private List<SearchAndGoResult> results;
	private List<SearchHistoryItem> searchQueryHistory;
	
	private MaterialSearchBar materialSearchBar;
	private RelativeLayout progressContainerRelativeLayout;
	private TextView actualProgressTextView, lastProgressTextView;
	private ImageButton bookmarkImageButton, historyImageButton, settingsImageButton;
	private RecyclerView searchResultRecyclerView;
	
	private ProgressBar loadingProgressBar;
	
	private FrameLayout informationContainerFrameLayout;
	private TextView informationTextView;
	
	private SearchAndGoResultViewAdapter searchAdapter;
	
	private OnSearchActionListener onSearchActionListener;
	
	public PageCultureSearchAndGoFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		
		this.searchAndGoManager = BoxPlayApplication.getManagers().getSearchAndGoManager();
		this.dialogCreator = new DialogCreator();
		
		this.results = new ArrayList<>();
		this.searchQueryHistory = searchAndGoManager.getSearchHistory();
		
		this.searchAndGoManager.bindCallback(new SearchAndGoSearchCallback() {
			private String getString(int ressourceId, Object... formatArgs) {
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
					BoxPlayApplication.getBoxPlayApplication().toast(R.string.boxplay_culture_searchngo_content_limit_reached, results.size(), MAX_CONTENT_ITEM_DISPLAYABLE).show();
					
					while (results.size() > MAX_CONTENT_ITEM_DISPLAYABLE) {
						results.remove(results.size() - 1);
					}
				}
				
				searchAdapter.notifyDataSetChanged();
				
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo, container, false);
		
		materialSearchBar = (MaterialSearchBar) view.findViewById(R.id.fragment_culture_searchngo_materialsearchbar_searchbar);
		
		materialSearchBar.setOnSearchActionListener(onSearchActionListener = new OnSearchActionListener() {
			@Override
			public void onSearchConfirmed(CharSequence text) {
				searchAndGoManager.search(actualQuery = text.toString());
			}
			
			@Override
			public void onSearchStateChanged(boolean enabled) {
				if (!enabled) {
					results.clear();
					searchAdapter.notifyDataSetChanged();
					
					informationContainerFrameLayout.setVisibility(View.VISIBLE);
					informationTextView.setText(R.string.boxplay_culture_searchngo_info_make_a_search);
					
					actualQuery = null;
				}
			}
			
			@Override
			public void onButtonClicked(int buttonCode) {
				materialSearchBar.hideSuggestionsList();
			}
		});
		
		progressContainerRelativeLayout = (RelativeLayout) view.findViewById(R.id.fragment_culture_searchngo_relativelayout_progress_container);
		
		actualProgressTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_progress_actual);
		lastProgressTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_progress_last);
		
		bookmarkImageButton = (ImageButton) view.findViewById(R.id.fragment_culture_searchngo_imagebutton_bookmark);
		historyImageButton = (ImageButton) view.findViewById(R.id.fragment_culture_searchngo_imagebutton_history);
		settingsImageButton = (ImageButton) view.findViewById(R.id.fragment_culture_searchngo_imagebutton_settings);
		
		historyImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialogCreator.showHistoryDialog();
			}
		});
		settingsImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialogCreator.showSettingsDialog();
			}
		});
		
		searchResultRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_culture_searchngo_recyclerview_search_result);
		searchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		searchResultRecyclerView.setAdapter(searchAdapter = new SearchAndGoResultViewAdapter(results));
		searchResultRecyclerView.setHasFixedSize(true);
		searchResultRecyclerView.setNestedScrollingEnabled(false);
		
		loadingProgressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_progressbar_loading);
		
		informationContainerFrameLayout = (FrameLayout) view.findViewById(R.id.fragment_culture_searchngo_framelayout_info_container);
		informationTextView = (TextView) view.findViewById(R.id.fragment_culture_searchngo_textview_info_text);
		
		setSearchBarHidden(false);
		
		return view;
	}
	
	public void searchStart() {
		setSearchBarHidden(true);
	}
	
	public void searchStop() {
		setSearchBarHidden(false);
	}
	
	public void setSearchBarHidden(boolean hidden) {
		bookmarkImageButton.setClickable(!hidden);
		historyImageButton.setClickable(!hidden);
		settingsImageButton.setClickable(!hidden);
		
		progressContainerRelativeLayout.setVisibility(hidden ? View.VISIBLE : View.GONE);
		materialSearchBar.setVisibility(hidden ? View.GONE : View.VISIBLE);
		searchResultRecyclerView.setVisibility(hidden ? View.GONE : View.VISIBLE);
		
		loadingProgressBar.setVisibility(hidden ? View.VISIBLE : View.GONE);
		
		informationContainerFrameLayout.setVisibility(View.GONE);
		
		if (searchAndGoManager.getProviders().isEmpty() && !hidden) {
			informationContainerFrameLayout.setVisibility(View.VISIBLE);
			searchResultRecyclerView.setVisibility(View.GONE);
			informationTextView.setText(R.string.boxplay_culture_searchngo_info_no_provider);
		}
		
		if (!hidden) { // Sometimes, text is not applied
			materialSearchBar.setText(materialSearchBar.getText());
		}
	}
	
	public void applyQuery(String query) {
		if (materialSearchBar != null && onSearchActionListener != null) {
			materialSearchBar.setText(query);
			onSearchActionListener.onSearchConfirmed(query);
			
			actualQuery = query;
		}
	}
	
	private String actualQuery = "";
	
	public String getActualQuery() {
		return actualQuery;
	}
	
	private String lastProgress = "-";
	
	public void updateProgress(String progress) {
		actualProgressTextView.setText(progress);
		lastProgressTextView.setText(lastProgress);
		
		lastProgress = progress;
	}
	
	class SearchAndGoResultViewAdapter extends RecyclerView.Adapter<SearchAndGoResultViewHolder> {
		private List<SearchAndGoResult> list;
		
		public SearchAndGoResultViewAdapter(List<SearchAndGoResult> list) {
			this.list = list;
		}
		
		@Override
		public SearchAndGoResultViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchngo_search_element, viewGroup, false);
			return new SearchAndGoResultViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(SearchAndGoResultViewHolder viewHolder, int position) {
			SearchAndGoResult item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class SearchAndGoResultViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView titleTextView, contentTextView, providerTextView, typeTextView;
		private ImageView thumbnailImageView;
		
		public SearchAndGoResultViewHolder(View itemView) {
			super(itemView);
			
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
			contentTextView.setText("-/-"); // TOOD: Make a description generator
			providerTextView.setText(result.getParentProvider().getSiteName().toUpperCase());
			typeTextView.setText(result.getType().toString().toUpperCase());
			
			BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, result.getBestImageUrl());
		}
	}
	
	/**
	 * Class to quickly create dialog used by the Search n' Go fragment
	 * 
	 * TODO: Make a better settings system
	 * 
	 * @author Enzo CACERES
	 */
	class DialogCreator {
		private final int SETTINGS_DIALOG_SELECTION_PROVIDERS = 0;
		
		private SharedPreferences preferences = BoxPlayApplication.getBoxPlayApplication().getPreferences();
		
		private AlertDialog searchHistoryDialog, settingsDialog, providersSettingsDialog;
		
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
		
		public void showSettingsDialog() {
			if (settingsDialog != null) {
				settingsDialog.show();
				return;
			}
			
			AlertDialog.Builder builder = createBuilder();
			builder.setTitle(getString(R.string.boxplay_culture_searchngo_dialog_settings));
			
			String[] settings = new String[] { getString(R.string.boxplay_culture_searchngo_dialog_settings_item_provider) };
			
			builder.setItems(settings, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case SETTINGS_DIALOG_SELECTION_PROVIDERS: {
							showProvidersSettingsDialog();
							break;
						}
						
						default: {
							break;
						}
					}
				}
			});
			
			settingsDialog = builder.create();
			settingsDialog.show();
		}
		
		public void showProvidersSettingsDialog() {
			if (providersSettingsDialog != null) {
				providersSettingsDialog.show();
				return;
			}
			
			AlertDialog.Builder builder = createBuilder();
			builder.setTitle(R.string.boxplay_culture_searchngo_dialog_settings_item_provider);
			
			final ProviderManager[] creatableProviders = ProviderManager.values();
			Set<String> enabledProvidersSet = preferences.getStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), searchAndGoManager.createDefaultProviderSet());
			
			final SearchAndGoProvider[] instancedSearchAndGoProviders = new SearchAndGoProvider[creatableProviders.length];
			final String[] providerSites = new String[creatableProviders.length];
			final boolean[] checkedItems = new boolean[creatableProviders.length];
			
			for (int i = 0; i < creatableProviders.length; i++) {
				SearchAndGoProvider provider = creatableProviders[i].create();
				
				instancedSearchAndGoProviders[i] = provider;
				
				providerSites[i] = provider.getSiteName();
				
				checkedItems[i] = enabledProvidersSet.contains(creatableProviders[i].toString());
			}
			
			builder.setMultiChoiceItems(providerSites, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checkedItems[which] = isChecked;
				}
			});
			
			builder.setPositiveButton(getString(R.string.boxplay_culture_searchngo_dialog_settings_item_provider_button_validate), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Set<String> newEnabledProviders = new ArraySet<>();
					List<SearchAndGoProvider> actualProviders = searchAndGoManager.getProviders();
					
					actualProviders.clear();
					
					for (int i = 0; i < creatableProviders.length; i++) {
						if (checkedItems[i]) {
							actualProviders.add(instancedSearchAndGoProviders[i]);
							
							newEnabledProviders.add(creatableProviders[i].toString());
						}
					}
					
					preferences.edit().putStringSet(getString(R.string.boxplay_other_settings_culture_searchngo_pref_enabled_providers_key), newEnabledProviders).commit();
					
					setSearchBarHidden(false);
				}
			});
			
			builder.setNegativeButton(getString(R.string.boxplay_culture_searchngo_dialog_settings_item_provider_button_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					providersSettingsDialog = null; // Nullify it so everything will be recreated with before values
				}
			});
			
			providersSettingsDialog = builder.create();
			providersSettingsDialog.show();
		}
	}
	
}