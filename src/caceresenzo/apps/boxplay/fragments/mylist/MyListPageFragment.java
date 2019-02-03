package caceresenzo.apps.boxplay.fragments.mylist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.activities.VideoActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.apps.boxplay.managers.MyListManager.MyList;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.SimpleData;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.string.StringUtils;

@SuppressWarnings("unused")
public abstract class MyListPageFragment extends BaseBoxPlayFragment implements MyListManager.FetchCallback {
	
	/* Managers */
	protected MyListManager myListManager;
	
	/* Content */
	private List<WatchLaterRecyclerViewItem> myListItems;
	
	/* Views */
	private RecyclerView recyclerView;
	
	private ProgressBar loadingProgressBar;
	
	private TextView infoTextView;
	
	/* Constructor */
	public MyListPageFragment() {
		super();
		
		this.myListManager = BoxPlayApplication.getManagers().getMyListManager();
		
		this.myListItems = new ArrayList<>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mylist_page, container, false);
		
		this.recyclerView = view.findViewById(R.id.fragment_mylist_page_recyclerview_content);
		
		this.loadingProgressBar = (ProgressBar) view.findViewById(R.id.fragment_mylist_page_progressbar_loading);
		
		this.infoTextView = (TextView) view.findViewById(R.id.fragment_mylist_page_textview_info_text);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		setListHidden(true);
		
		callFetch();
	}
	
	public void callFetch() {
		getMyListInstance().fetch(this);
	}
	
	public abstract MyList getMyListInstance();
	
	@Override
	public void onFetchFinished(List<MyListable> myListables) {
		if (myListables == null || myListables.isEmpty()) {
			updateInfoText("List is: " + (myListables == null ? "null" : "empty"));
			setListHidden(false);
			return;
		}
		
		myListItems.clear();
		
		for (MyListable myListable : myListables) {
			if (myListable instanceof VideoGroup) {
				myListItems.add(new StoreVideoGroupItem((VideoGroup) myListable));
			}
			//
			else if (myListable instanceof SearchAndGoResult) {
				myListItems.add(new CultureSearchAndGoResultItem((SearchAndGoResult) myListable));
			}
		}
		
		this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		this.recyclerView.setAdapter(new MyListAdapter());
		
		setListHidden(false);
	}
	
	@Override
	public void onException(Exception exception) {
		notifyError(exception);
	}
	
	private void setListHidden(boolean hidden) {
		recyclerView.setVisibility(hidden ? View.GONE : View.VISIBLE);
		
		loadingProgressBar.setVisibility(hidden ? View.VISIBLE : View.GONE);
		infoTextView.setVisibility(View.GONE);
	}
	
	private void notifyError(Exception exception) {
		setListHidden(true);
		
		updateInfoText(StringUtils.fromException(exception));
	}
	
	private void updateInfoText(String text) {
		infoTextView.setVisibility(View.VISIBLE);
		infoTextView.setText(text);
	}
	
	public class MyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			View view = inflater.inflate(R.layout.item_mylist_watchlater_base_element, parent, false);
			
			switch (viewType) {
				case WatchLaterRecyclerViewItem.TYPE_STORE_VIDEO_GROUP: {
					return new StoreVideoGroupViewHolder(view);
				}
				
				case WatchLaterRecyclerViewItem.TYPE_CULTURE_SEARCHANDGO_RESULT: {
					return new CultureSearchAndGoItemViewHolder(view);
				}
				
				default: {
					return null;
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
			((Bindable<WatchLaterRecyclerViewItem>) holder).bind(myListItems.get(position));
		}
		
		@Override
		public int getItemViewType(int position) {
			return myListItems.get(position).getType();
		}
		
		@Override
		public int getItemCount() {
			return myListItems.size();
		}
	}
	
	class StoreVideoGroupViewHolder extends BaseItemViewHolder implements Bindable<StoreVideoGroupItem> {
		public StoreVideoGroupViewHolder(View itemView) {
			super(itemView);
		}
		
		@Override
		public void bind(StoreVideoGroupItem item) {
			final VideoGroup group = item.getVideoGroup();
			
			titleTextView.setText(group.getTitle());
			viewHelper.downloadToImageView(thumbnailImageView, group.getGroupImageUrl());
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					VideoActivity.start(group); // TODO
				}
			});
		}
		
		@Override
		public int getSourceStringRessourceId() {
			return R.string.boxplay_mylist_source_store_video;
		}
	}
	
	class StoreVideoGroupItem extends WatchLaterRecyclerViewItem {
		private VideoGroup videoGroup;
		
		public StoreVideoGroupItem(VideoGroup videoGroup) {
			this.videoGroup = videoGroup;
		}
		
		public VideoGroup getVideoGroup() {
			return videoGroup;
		}
		
		@Override
		public int getType() {
			return TYPE_STORE_VIDEO_GROUP;
		}
	}
	
	class CultureSearchAndGoItemViewHolder extends BaseItemViewHolder implements Bindable<CultureSearchAndGoResultItem> {
		public CultureSearchAndGoItemViewHolder(View itemView) {
			super(itemView);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void bind(CultureSearchAndGoResultItem item) {
			final SearchAndGoResult searchAndGoResult = item.getSearchAndGoResult();
			
			titleTextView.setText(searchAndGoResult.getName());
			viewHelper.downloadToImageView(thumbnailImageView, searchAndGoResult.getImageUrl(), (Map<String, Object>) searchAndGoResult.getComplement(SimpleData.REQUIRE_HTTP_HEADERS_COMPLEMENT));
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					SearchAndGoDetailActivity.start(searchAndGoResult);
				}
			});
		}
		
		@Override
		public int getSourceStringRessourceId() {
			return R.string.boxplay_mylist_source_culture_searchngo;
		}
	}
	
	class CultureSearchAndGoResultItem extends WatchLaterRecyclerViewItem {
		private SearchAndGoResult searchAndGoResult;
		
		public CultureSearchAndGoResultItem(SearchAndGoResult searchAndGoResult) {
			this.searchAndGoResult = searchAndGoResult;
		}
		
		public SearchAndGoResult getSearchAndGoResult() {
			return searchAndGoResult;
		}
		
		@Override
		public int getType() {
			return TYPE_CULTURE_SEARCHANDGO_RESULT;
		}
	}
	
	abstract class BaseItemViewHolder extends RecyclerView.ViewHolder {
		protected View view;
		protected TextView sourceTextView, titleTextView;
		protected ImageView thumbnailImageView;
		
		public BaseItemViewHolder(View itemView) {
			super(itemView);
			
			this.view = itemView;
			
			this.sourceTextView = (TextView) itemView.findViewById(R.id.item_mylist_watchlater_base_element_textview_source);
			this.titleTextView = (TextView) itemView.findViewById(R.id.item_mylist_watchlater_base_element_textview_title);
			this.thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_mylist_watchlater_base_element_imageview_thumbnail);
			
			this.sourceTextView.setText(getSourceStringRessourceId());
		}
		
		public abstract int getSourceStringRessourceId();
	}
	
	abstract static class WatchLaterRecyclerViewItem {
		public static final int TYPE_STORE_VIDEO_GROUP = 0;
		public static final int TYPE_CULTURE_SEARCHANDGO_RESULT = 1;
		
		public abstract int getType();
	}
	
	interface Bindable<T extends WatchLaterRecyclerViewItem> {
		void bind(T item);
	}
	
}