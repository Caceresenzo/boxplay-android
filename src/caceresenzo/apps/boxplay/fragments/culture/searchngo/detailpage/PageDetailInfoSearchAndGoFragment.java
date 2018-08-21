package caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage;

import java.util.ArrayList;
import java.util.List;

import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalDataType;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.additional.CategoryResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.additional.RatingResultData;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.mylist.MyListable;

/**
 * Info page for {@link SearchAndGoDetailActivity}
 * 
 * @author Enzo CACERES
 */
public class PageDetailInfoSearchAndGoFragment extends Fragment {
	private boolean uiReady = false;
	
	private List<DetailListItem> items = new ArrayList<>();
	
	private RecyclerView recyclerView;
	private ProgressBar progressBar;
	
	private DetailRecyclerViewAdapter adapter;
	
	public PageDetailInfoSearchAndGoFragment() {
		;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo_activitypage_details, container, false);
		
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_progressbar_loading);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_recyclerview_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(adapter = new DetailRecyclerViewAdapter(items));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		
		progressBar.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);
		
		uiReady = true;
		
		return view;
	}
	
	/**
	 * Apply data from the {#link SearchAndGoDetailActivit}'s worker
	 * 
	 * @param result
	 *            Original clicked result
	 * @param additionals
	 *            New additional data
	 */
	public void applyResult(SearchAndGoResult result, List<AdditionalResultData> additionals) {
		this.items.clear();

		this.items.add(new ImageDetailItem(result.getBestImageUrl()).dataType(AdditionalDataType.THUMBNAIL));

		this.items.add(new AddToWatchListDetailItem(result));
		
		for (AdditionalResultData additionalResultData : additionals) {
			Object data = additionalResultData.getData();
			DetailListItem item;
			
			if (data instanceof List && !((List<?>) data).isEmpty()) {
				List<?> unknownList = (List<?>) data;
				Object firstObject = unknownList.get(0);
				
				if (firstObject instanceof CategoryResultData) {
					List<CategoryResultData> categories = new ArrayList<>();
					
					for (Object categoryResultData : unknownList) {
						categories.add((CategoryResultData) categoryResultData);
					}
					
					item = new CategoryDetailItem(categories);
				} else {
					item = new StringDetailItem("#LIST/" + additionalResultData.convert());
				}
			} else if (data instanceof RatingResultData) {
				item = new RatingDetailItem((RatingResultData) data);
			} else {
				item = new StringDetailItem(additionalResultData.convert());
			}
			
			this.items.add(item.dataType(additionalResultData.getType()));
		}
		
		if (adapter != null) {
			adapter.notifyDataSetChanged();
			
			recyclerView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Tell if the ui has been {@link View#findViewById(int)} and all view are ready to use
	 * 
	 * @return If the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished
	 */
	public boolean isUiReady() {
		return uiReady;
	}
	
	class DetailRecyclerViewAdapter extends RecyclerView.Adapter<DetailRowViewHolder> {
		private List<DetailListItem> list;
		
		public DetailRecyclerViewAdapter(List<DetailListItem> list) {
			this.list = list;
		}
		
		@Override
		public DetailRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.container_item_culture_searchngo_activitypage_detail_info_holder, viewGroup, false);
			return new DetailRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(DetailRowViewHolder viewHolder, int position) {
			DetailListItem item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class DetailRowViewHolder extends RecyclerView.ViewHolder {
		private ViewHelper viewHelper;
		
		private TextView typeTextView;
		private RecyclerView rowRecyclerView;
		
		public DetailRowViewHolder(View itemView) {
			super(itemView);
			
			viewHelper = BoxPlayApplication.getViewHelper();
			
			typeTextView = (TextView) itemView.findViewById(R.id.container_item_culture_searchandgo_detail_info_holder_textview_type);
			rowRecyclerView = (RecyclerView) itemView.findViewById(R.id.container_item_culture_searchandgo_detail_info_holder_recyclerview_list);
			rowRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
		}
		
		public void bind(DetailListItem item) {
			if (item.getDataType() == null) {
				typeTextView.setVisibility(View.GONE);
			} else {
				typeTextView.setText(viewHelper.enumToStringCacheTranslation(item.getDataType()));
			}
			
			switch (item.getType()) {
				case DetailListItem.TYPE_IMAGE: { // 0
					rowRecyclerView.setAdapter(new ImageItemViewAdapter(((ImageDetailItem) item)));
					break;
				}
				
				case DetailListItem.TYPE_BUTTON_ADD_TO_WATCHLIST: { // 1
					rowRecyclerView.setAdapter(new AddToWatchListItemViewAdapter((AddToWatchListDetailItem) item));
					break;
				}
				
				case DetailListItem.TYPE_STRING: { // 2
					rowRecyclerView.setAdapter(new StringItemViewAdapter(((StringDetailItem) item)));
					break;
				}
				
				case DetailListItem.TYPE_CATEGORY: { // 3
					FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
					flowLayoutManager.setAutoMeasureEnabled(true);
					rowRecyclerView.setLayoutManager(flowLayoutManager);
					
					rowRecyclerView.setAdapter(new CategoryItemViewAdapter(((CategoryDetailItem) item)));
					break;
				}
				
				case DetailListItem.TYPE_RATING: { // 4
					// LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
					// linearLayoutManager.setAutoMeasureEnabled(true);
					// rowRecyclerView.setLayoutManager(linearLayoutManager);
					
					rowRecyclerView.setAdapter(new RatingItemViewAdapter(((RatingDetailItem) item)));
					break;
				}
				
				default: {
					BoxPlayApplication.getBoxPlayApplication().snackbar(getString(R.string.boxplay_error_fragment_type_unbind, item.getType()), Snackbar.LENGTH_LONG).show();
					break;
				}
			}
		}
		
	}
	
	/*
	 * ************************************** IMAGE
	 */
	class ImageItemViewAdapter extends RecyclerView.Adapter<ImageItemViewHolder> {
		private ImageDetailItem imageItem;
		
		public ImageItemViewAdapter(ImageDetailItem imageItem) {
			this.imageItem = imageItem;
		}
		
		@Override
		public ImageItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_info_image, viewGroup, false);
			return new ImageItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(ImageItemViewHolder viewHolder, int position) {
			viewHolder.bind(imageItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	class ImageItemViewHolder extends RecyclerView.ViewHolder {
		private ImageView contentImageView;
		
		public ImageItemViewHolder(View itemView) {
			super(itemView);
			
			contentImageView = (ImageView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_image_imageview_container);
		}
		
		public void bind(ImageDetailItem imageItem) {
			BoxPlayApplication.getViewHelper().downloadToImageView(contentImageView, imageItem.getUrl());
		}
	}
	
	class ImageDetailItem extends DetailListItem {
		private String url;
		
		public ImageDetailItem(String url) {
			this.url = url;
		}
		
		public String getUrl() {
			return url;
		}
		
		@Override
		public int getType() {
			return TYPE_IMAGE;
		}
	}
	
	/*
	 * ************************************** ADD TO WATCH LIST
	 */
	class AddToWatchListItemViewAdapter extends RecyclerView.Adapter<AddToWatchListItemViewHolder> {
		private AddToWatchListDetailItem addToWatchListItem;
		
		public AddToWatchListItemViewAdapter(AddToWatchListDetailItem addToWatchListItem) {
			this.addToWatchListItem = addToWatchListItem;
		}
		
		@Override
		public AddToWatchListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_info_add_to_wachlist, viewGroup, false);
			return new AddToWatchListItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(AddToWatchListItemViewHolder viewHolder, int position) {
			viewHolder.bind(addToWatchListItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	class AddToWatchListItemViewHolder extends RecyclerView.ViewHolder {
		private String addString, removeString;
		private Button addToListButton;
		
		public AddToWatchListItemViewHolder(View itemView) {
			super(itemView);
			
			addString = getString(R.string.boxplay_culture_searchngo_detail_info_button_add_to_watchlist);
			removeString = getString(R.string.boxplay_culture_searchngo_detail_info_button_remove_to_watchlist);
			
			addToListButton = (Button) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_add_to_watchlist_button_add_to_list);
		}
		
		public void bind(final AddToWatchListDetailItem item) {
			updateButtonText(item);
			
			addToListButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					item.updateState();
					updateButtonText(item);
				}
			});
		}
		
		public void updateButtonText(AddToWatchListDetailItem item) {
			String text = addString;
			
			if (item.isInList()) {
				text = removeString;
			}
			
			addToListButton.setText(text);
		}
	}
	
	class AddToWatchListDetailItem extends DetailListItem {
		private MyListManager myListManager = BoxPlayApplication.getManagers().getMyListManager();
		private MyListable myListable;
		
		public AddToWatchListDetailItem(MyListable myListable) {
			this.myListable = myListable;
		}
		
		public MyListable getMyListable() {
			return myListable;
		}
		
		public boolean isInList() {
			return myListManager.containsInWatchList(myListable);
		}
		
		public void updateState() {
			if (myListManager.containsInWatchList(myListable)) {
				myListManager.removeFromWatchList(myListable);
			} else {
				myListManager.addToWatchList(myListable);
			}
		}
		
		@Override
		public int getType() {
			return TYPE_BUTTON_ADD_TO_WATCHLIST;
		}
	}
	
	/*
	 * ************************************** STRING
	 */
	class StringItemViewAdapter extends RecyclerView.Adapter<StringItemViewHolder> {
		private StringDetailItem stringItem;
		
		public StringItemViewAdapter(StringDetailItem stringItem) {
			this.stringItem = stringItem;
		}
		
		@Override
		public StringItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_info_string, viewGroup, false);
			return new StringItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(StringItemViewHolder viewHolder, int position) {
			viewHolder.bind(stringItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	class StringItemViewHolder extends RecyclerView.ViewHolder {
		private TextView contentTextView;
		
		public StringItemViewHolder(View itemView) {
			super(itemView);
			
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_string_textview_container);
		}
		
		public void bind(StringDetailItem stringItem) {
			contentTextView.setText(stringItem.getString());
		}
	}
	
	class StringDetailItem extends DetailListItem {
		private String string;
		
		public StringDetailItem(String string) {
			this.string = string;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public int getType() {
			return TYPE_STRING;
		}
	}
	
	/*
	 * ************************************** CATEGORY (GENDER)
	 */
	class CategoryItemViewAdapter extends RecyclerView.Adapter<CategoryItemViewHolder> {
		private List<CategoryResultData> categories;
		
		public CategoryItemViewAdapter(CategoryDetailItem categoryItem) {
			this.categories = categoryItem.getCategoryResultData();
		}
		
		@Override
		public CategoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_info_category, viewGroup, false);
			return new CategoryItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(CategoryItemViewHolder viewHolder, int position) {
			viewHolder.bind(categories.get(position));
		}
		
		@Override
		public int getItemCount() {
			return categories.size();
		}
	}
	
	class CategoryItemViewHolder extends RecyclerView.ViewHolder {
		private TextView contentTextView;
		
		public CategoryItemViewHolder(View itemView) {
			super(itemView);
			
			contentTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_category_textview_container);
		}
		
		public void bind(CategoryResultData categoryData) {
			contentTextView.setText(categoryData.getName());
		}
	}
	
	class CategoryDetailItem extends DetailListItem {
		private List<CategoryResultData> categoryResultData;
		
		public CategoryDetailItem(List<CategoryResultData> categoryResultData) {
			this.categoryResultData = categoryResultData;
		}
		
		public List<CategoryResultData> getCategoryResultData() {
			return categoryResultData;
		}
		
		@Override
		public int getType() {
			return TYPE_CATEGORY;
		}
	}
	
	/*
	 * ************************************** RATING
	 */
	class RatingItemViewAdapter extends RecyclerView.Adapter<RatingItemViewHolder> {
		private RatingDetailItem ratingItem;
		
		public RatingItemViewAdapter(RatingDetailItem ratingItem) {
			this.ratingItem = ratingItem;
		}
		
		@Override
		public RatingItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_culture_searchandgo_activitypage_detail_info_rating, viewGroup, false);
			return new RatingItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(RatingItemViewHolder viewHolder, int position) {
			viewHolder.bind(ratingItem);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	class RatingItemViewHolder extends RecyclerView.ViewHolder {
		private RatingBar starRatingBar;
		private TextView stringTextView;
		
		public RatingItemViewHolder(View itemView) {
			super(itemView);
			
			starRatingBar = (RatingBar) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_rating_ratingbar_bar);
			stringTextView = (TextView) itemView.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_rating_textview_string);
		}
		
		public void bind(RatingDetailItem ratingItem) {
			RatingResultData ratingData = ratingItem.getRatingResultData();

			starRatingBar.setNumStars(5);
			starRatingBar.setMax(5);
			
			starRatingBar.setRating(((ratingData.getAverage()) / ratingData.getBest()) * 5);
			
			stringTextView.setText(ratingData.convertToDisplayableString());
		}
	}
	
	class RatingDetailItem extends DetailListItem {
		private RatingResultData ratingResultData;
		
		public RatingDetailItem(RatingResultData ratingResultData) {
			this.ratingResultData = ratingResultData;
		}
		
		public RatingResultData getRatingResultData() {
			return ratingResultData;
		}
		
		@Override
		public int getType() {
			return TYPE_RATING;
		}
	}
	
	abstract static class DetailListItem {
		private AdditionalDataType dataType;
		
		public static final int TYPE_IMAGE = 0;
		public static final int TYPE_BUTTON_ADD_TO_WATCHLIST = 1;
		public static final int TYPE_STRING = 2;
		public static final int TYPE_CATEGORY = 3;
		public static final int TYPE_RATING = 4;
		
		public abstract int getType();
		
		public AdditionalDataType getDataType() {
			return dataType;
		}
		
		public DetailListItem dataType(AdditionalDataType dataType) {
			this.dataType = dataType;
			return this;
		}
	}
	
}