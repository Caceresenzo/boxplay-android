package caceresenzo.apps.boxplay.fragments.store;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicElementRowItem;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicElementRowViewAdapter;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicListRowItem;
import caceresenzo.apps.boxplay.fragments.store.PageMusicStoreFragment.MusicListRowViewAdapter;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoElementRowItem;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoElementRowViewAdapter;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoListRowItem;
import caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoListRowViewAdapter;

public abstract class StorePageFragment extends Fragment {
	
	protected static StorePageFragment INSTANCE;
	protected static List<StorePageFragment> INSTANCES;
	
	protected RecyclerView recyclerView;
	protected SwipeRefreshLayout swipeRefreshLayout;
	protected OnRefreshListener onRefreshListener;
	
	protected boolean hasTitle = false;
	
	protected List<RowListItem> rowListItems;
	
	protected StoreRowViewAdapter storeRowViewAdapter;
	protected StoreRowViewAdapter storeSearchRowViewAdapter;
	
	protected StoreSearchHandler<?> storeSearchHandler;
	
	public StorePageFragment() {
		INSTANCE = this;
		
		if (INSTANCES == null) {
			INSTANCES = new ArrayList<StorePageFragment>();
		}
		
		boolean alreadyContain = false;
		for (StorePageFragment storePageFragment : INSTANCES) {
			if (storePageFragment.getClass() == getClass()) {
				alreadyContain = true;
				break;
			}
		}
		
		if (!alreadyContain) {
			INSTANCES.add(this);
		}
		
		rowListItems = new ArrayList<RowListItem>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(overrideLayout(), container, false);
		return view;
	}
	
	public int overrideLayout() {
		return R.layout.fragment_store_page;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		INSTANCE = this;
		
		recyclerView = (RecyclerView) getView().findViewById(R.id.fragment_store_page_recyclerview_list);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(storeRowViewAdapter = new StoreRowViewAdapter(rowListItems));
		
		swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.fragment_store_page_swiperefreshlayout_container);
		swipeRefreshLayout.setRefreshing(true);
		swipeRefreshLayout.setOnRefreshListener(onRefreshListener = new OnRefreshListener() {
			@Override
			public void onRefresh() {
				onUserRefresh();
			}
		});
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		INSTANCE = null;
		storeSearchHandlers.clear();
		
		if (INSTANCES != null) {
			INSTANCES.remove(this);
			
			if (INSTANCES.isEmpty()) {
				INSTANCES = null;
			}
		}
	}
	
	public abstract StoreSearchHandler<?> createSearchHandler();
	
	public StoreSearchHandler<?> getSearchHandler() {
		if (storeSearchHandler == null) {
			storeSearchHandler = createSearchHandler();
		}
		
		return storeSearchHandler;
	}
	
	protected void initializeViews(View view) {
		;
	}
	
	public abstract void onUserRefresh();
	
	public abstract void callDataUpdater(boolean newContent);
	
	public void finishUpdate(boolean newContent) {
		if (newContent) {
			BoxPlayApplication.getBoxPlayApplication().snackbar(R.string.boxplay_store_data_downloading_new_content, Snackbar.LENGTH_LONG);
		}
		
		if (swipeRefreshLayout != null && recyclerView != null) {
			swipeRefreshLayout.setRefreshing(false);
			
			if (recyclerView.getAdapter() != storeRowViewAdapter) {
				recyclerView.getAdapter().notifyDataSetChanged();
			} else {
				storeRowViewAdapter.notifyDataSetChanged();
			}
		}
	}
	
	/*
	 * Store Views
	 */
	class StoreRowViewAdapter extends RecyclerView.Adapter<StoreRowViewHolder> {
		private List<RowListItem> rows;
		
		private StoreRowViewAdapter(List<RowListItem> rows) {
			this.rows = rows;
		}
		
		@Override
		public StoreRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_store_page_child_titled, viewGroup, false);
			return new StoreRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(StoreRowViewHolder viewHolder, int position) {
			RowListItem item = rows.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return rows.size();
		}
	}
	
	/*
	 * Store Search
	 */
	private static List<StoreSearchHandler<?>> storeSearchHandlers = new ArrayList<StoreSearchHandler<?>>();
	
	abstract class StoreSearchHandler<ElementClass> {
		private OnQueryTextListener onQueryTextListener;
		private OnActionExpandListener onActionExpandListener;
		
		public StoreSearchHandler() {
			storeSearchHandlers.add(this);
		}
		
		public boolean handleSearch(MenuItem item) {
			if (item.getItemId() != R.id.menu_main_action_search) {
				return false;
			}
			final SearchView searchView = (SearchView) item.getActionView();
			
			onQueryTextListener = new OnQueryTextListener() {
				@Override
				public boolean onQueryTextChange(String newText) {
					for (StoreSearchHandler<?> handler : storeSearchHandlers) {
						handler.onQueryTextChange(newText);
					}
					return true;
				}
				
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}
			};
			
			onActionExpandListener = new OnActionExpandListener() {
				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					for (StoreSearchHandler<?> handler : storeSearchHandlers) {
						handler.onMenuItemActionCollapse(item);
					}
					return true;
				}
				
				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					for (StoreSearchHandler<?> handler : storeSearchHandlers) {
						handler.onMenuItemActionExpand(item);
					}
					return true;
				}
			};
			
			searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
				@Override
				public void onViewDetachedFromWindow(View View) { // If user do onSubmit(), cancelling will not work
					for (StoreSearchHandler<?> handler : storeSearchHandlers) {
						handler.onSearchViewClose();
					}
				}
				
				@Override
				public void onViewAttachedToWindow(View View) {
					; // Search was opened
				}
			});
			
			searchView.setOnQueryTextListener(onQueryTextListener);
			item.setOnActionExpandListener(onActionExpandListener);
			return true;
			
		}
		
		public abstract boolean onQueryTextChange(String newText);
		
		public abstract List<ElementClass> filter(String query);
		
		public boolean onMenuItemActionCollapse(MenuItem item) {
			endSearchAndRestoreRecycler();
			return true;
		}
		
		public boolean onSearchViewClose() {
			endSearchAndRestoreRecycler();
			return true;
		}
		
		public boolean onMenuItemActionExpand(MenuItem item) {
			if (onQueryTextListener != null) {
				onQueryTextListener.onQueryTextChange("");
			}
			return true;
		}
		
		private void endSearchAndRestoreRecycler() {
			if (recyclerView != null) {
				recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
				recyclerView.setAdapter(storeRowViewAdapter);
			}
		}
	}
	
	public static boolean handleSearch(MenuItem item) {
		for (StorePageFragment storePageFragment : StorePageFragment.getStorePageFragmentRegisteredInstances()) {
			if (storePageFragment != null) {
				storePageFragment.getSearchHandler().handleSearch(item);
			}
		}
		return true;
	}
	
	/*
	 * Store Views
	 */
	class StoreRowViewHolder extends RecyclerView.ViewHolder {
		private CardView mainContainerCardView;
		private RelativeLayout containerRelativeLayout;
		private TextView titleTextView;
		private View seperatorIncludeView;
		private RecyclerView recyclerView;
		
		public StoreRowViewHolder(View itemView) {
			super(itemView);
			
			mainContainerCardView = (CardView) itemView.findViewById(R.id.fragment_store_page_child_cardview_main_container);
			
			containerRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.item_store_page_all_title_relativelayout_title_container);
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_all_title_textview_title);
			
			seperatorIncludeView = itemView.findViewById(R.id.fragment_store_page_child_include_seperator);
			
			recyclerView = (RecyclerView) itemView.findViewById(R.id.fragment_store_page_child_recyclerview_list);
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		}
		
		public void applyTitle(RowListItem rowListItem) {
			if (titleTextView == null) {
				return;
			}
			
			RowListItemConfig config = rowListItem.getConfig();
			
			String title = config.getTitle();
			if (!config.isTitleEnabled() || title == null) {
				containerRelativeLayout.setVisibility(View.GONE);
				seperatorIncludeView.setVisibility(View.GONE);
				return;
			}
			
			mainContainerCardView.setOnClickListener(config.getOnClickListenerOrDefault());
			
			titleTextView.setText(title);
		}
		
		public void bind(RowListItem item) {
			applyTitle(item);
			
			switch (item.getType()) {
				case RowListItem.TYPE_VIDEO_LIST: { // 0
					recyclerView.setAdapter(new VideoListRowViewAdapter(((VideoListRowItem) item).getVideoElements()));
					break;
				}
				case RowListItem.TYPE_VIDEO_ELEMENT: { // 1
					recyclerView.setAdapter(new VideoElementRowViewAdapter(((VideoElementRowItem) item).getVideoFile()));
					break;
				}
				case RowListItem.TYPE_MUSIC_LIST: { // 100
					recyclerView.setAdapter(new MusicListRowViewAdapter(((MusicListRowItem) item).getMusicElements()));
					break;
				}
				case RowListItem.TYPE_MUSIC_ELEMENT: { // 101
					recyclerView.setAdapter(new MusicElementRowViewAdapter(((MusicElementRowItem) item).getMusicFile()));
					break;
				}
				case RowListItem.TYPE_ELEMENT_TITLE: { // 1000
					recyclerView.setAdapter(new TitleRowViewAdapter((TitleRowItem) item));
					break;
				}
				case RowListItem.TYPE_ELEMENT_ERROR: { // 1001
					recyclerView.setAdapter(new ErrorRowViewAdapter((ErrorRowItem) item));
					break;
				}
				
				default: {
					BoxPlayApplication.getBoxPlayApplication().snackbar(getString(R.string.boxplay_error_fragment_type_unbind, item.getType()), Snackbar.LENGTH_LONG).show();
					break;
				}
			}
		}
	}
	
	protected static class TitleRowViewAdapter extends RecyclerView.Adapter<TitleRowViewHolder> {
		private TitleRowItem titleRowItem;
		
		public TitleRowViewAdapter(TitleRowItem titleRowItem) {
			this.titleRowItem = titleRowItem;
		}
		
		@Override
		public TitleRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_all_title_cardview, viewGroup, false);
			return new TitleRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(TitleRowViewHolder viewHolder, int position) {
			viewHolder.bind(titleRowItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	protected static class TitleRowViewHolder extends RecyclerView.ViewHolder {
		private TextView titleTextView;
		
		public TitleRowViewHolder(View itemView) {
			super(itemView);
			
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_all_title_textview_title);
		}
		
		public void bind(TitleRowItem item) {
			titleTextView.setText(item.getTitle());
		}
	}
	
	protected static class TitleRowItem extends RowListItem {
		private String title;
		
		public TitleRowItem(Object title) {
			super();
			this.title = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(title);
		}
		
		public String getTitle() {
			return title;
		}
		
		@Override
		public int getType() {
			return TYPE_ELEMENT_TITLE;
		}
	}
	
	protected static class ErrorRowViewAdapter extends RecyclerView.Adapter<ErrorRowViewHolder> {
		private ErrorRowItem errorRowItem;
		
		public ErrorRowViewAdapter(ErrorRowItem errorRowItem) {
			this.errorRowItem = errorRowItem;
		}
		
		@Override
		public ErrorRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_all_error, viewGroup, false);
			return new ErrorRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(ErrorRowViewHolder viewHolder, int position) {
			viewHolder.bind(errorRowItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	protected static class ErrorRowViewHolder extends RecyclerView.ViewHolder {
		private TextView titleTextView, errorTextView;
		private Button button1, button2;
		
		public ErrorRowViewHolder(View itemView) {
			super(itemView);
			
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_all_error_textview_title);
			errorTextView = (TextView) itemView.findViewById(R.id.item_store_page_all_error_textview_content);
			button1 = (Button) itemView.findViewById(R.id.item_store_page_all_error_button_1);
			button2 = (Button) itemView.findViewById(R.id.item_store_page_all_error_button_2);
		}
		
		public void bind(ErrorRowItem item) {
			if (item.getTitle() == null) {
				titleTextView.setVisibility(View.GONE);
			} else {
				titleTextView.setText(item.getTitle());
			}
			
			if (item.getError() == null) {
				errorTextView.setVisibility(View.GONE);
			} else {
				errorTextView.setText(item.getError());
			}
			
			if (item.getButton1() == null || item.getOnClickListener1() == null) {
				button1.setVisibility(View.GONE);
			} else {
				button1.setText(item.getButton1());
				button1.setOnClickListener(item.getOnClickListener1());
			}
			
			if (item.getButton2() == null || item.getOnClickListener2() == null) {
				button2.setVisibility(View.GONE);
			} else {
				button2.setText(item.getButton2());
				button2.setOnClickListener(item.getOnClickListener2());
			}
			
			if (item.getButton1() == null && item.getButton2() != null) {
				button2.getLayoutParams().width = LayoutParams.MATCH_PARENT;
			}
		}
	}
	
	protected static class ErrorRowItem extends RowListItem {
		private String title, error, button1, button2;
		private OnClickListener onClickListener1, onClickListener2;
		
		public ErrorRowItem(Object title) {
			this(title, null, null, null, null, null);
		}
		
		public ErrorRowItem(Object title, Object error) {
			this(title, error, null, null, null, null);
		}
		
		public ErrorRowItem(Object title, Object error, Object button, OnClickListener onClickListener) {
			this(title, error, null, button, null, onClickListener);
		}
		
		public ErrorRowItem(Object button, OnClickListener onClickListener) {
			this(null, null, null, button, null, onClickListener);
		}
		
		public ErrorRowItem(Object title, Object error, Object button1, Object button2, OnClickListener onClickListener1, OnClickListener onClickListener2) {
			super();
			this.title = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(title);
			this.error = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(error);
			this.button1 = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(button1);
			this.button2 = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(button2);
			this.onClickListener1 = onClickListener1;
			this.onClickListener2 = onClickListener2;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getError() {
			return error;
		}
		
		public String getButton1() {
			return button1;
		}
		
		public String getButton2() {
			return button2;
		}
		
		public OnClickListener getOnClickListener1() {
			return onClickListener1;
		}
		
		public OnClickListener getOnClickListener2() {
			return onClickListener2;
		}
		
		@Override
		public int getType() {
			return TYPE_ELEMENT_ERROR;
		}
	}
	
	public abstract static class RowListItem {
		private RowListItemConfig config;
		
		public RowListItem() {
			this(new RowListItemConfig());
		}
		
		public RowListItem(RowListItemConfig config) {
			this.config = config;
		}
		
		// 0-99 -> Video Fragment
		public static final int TYPE_VIDEO_LIST = 0;
		public static final int TYPE_VIDEO_ELEMENT = 1;
		// 100-199 -> Music Fragment
		public static final int TYPE_MUSIC_LIST = 100;
		public static final int TYPE_MUSIC_ELEMENT = 101;
		// 200-299 -> Files Fragment
		// 1000-1099 -> Other Elements
		public static final int TYPE_ELEMENT_TITLE = 1000;
		public static final int TYPE_ELEMENT_ERROR = 1001;
		
		public abstract int getType();
		
		public RowListItemConfig getConfig() {
			return config;
		}
		
		public RowListItem configurate(RowListItemConfig config) {
			this.config = config;
			return this;
		}
	}
	
	public static class RowListItemConfig {
		public static final OnClickListener EMPTY_CLICK_LISTENER = new OnClickListener() {
			@Override
			public void onClick(View view) {
				;
			}
		};
		
		private String title;
		private OnClickListener onClickListener;
		
		public String getTitle() {
			return title;
		}
		
		public RowListItemConfig title(String title) {
			this.title = title;
			return this;
		}
		
		public boolean isTitleEnabled() {
			return title != null && !title.isEmpty();
		}
		
		public OnClickListener getOnClickListener() {
			return onClickListener;
		}
		
		public OnClickListener getOnClickListenerOrDefault() {
			return onClickListener != null ? onClickListener : EMPTY_CLICK_LISTENER;
		}
		
		public RowListItemConfig onClickListener(OnClickListener onClickListener) {
			this.onClickListener = onClickListener;
			return this;
		}
	}
	
	protected abstract class StorePopulator {
		abstract void populate();
	}
	
	public static StorePageFragment getStorePageFragmentBasicInstance() {
		return INSTANCE;
	}
	
	public static List<StorePageFragment> getStorePageFragmentRegisteredInstances() {
		if (INSTANCES == null) {
			return new ArrayList<StorePageFragment>();
		}
		
		return INSTANCES;
	}
	
}