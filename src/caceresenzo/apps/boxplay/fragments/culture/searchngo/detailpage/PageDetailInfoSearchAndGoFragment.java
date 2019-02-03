package caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import android.os.Bundle;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.ImageViewerActivity;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.apps.boxplay.managers.MyListManager.MyList;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalDataType;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.additional.CategoryResultData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.additional.RatingResultData;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.Subscribable;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.string.StringUtils;

/**
 * Info page for {@link SearchAndGoDetailActivity}
 * 
 * @author Enzo CACERES
 */
public class PageDetailInfoSearchAndGoFragment extends BaseBoxPlayFragment {
	
	/* Manager */
	private MyListManager myListManager;
	
	/* Views */
	private LinearLayout listLinearLayout;
	private ProgressBar progressBar;
	
	/* Variables */
	private List<DetailListItem> items;
	private SearchAndGoResult result;
	
	/* Constructor */
	public PageDetailInfoSearchAndGoFragment() {
		super();
		
		this.myListManager = managers.getMyListManager();
		
		this.items = new ArrayList<>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_culture_searchngo_activitypage_details, container, false);
		
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_progressbar_loading);
		
		listLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_culture_searchngo_activitypage_details_linearlayout_list);
		
		progressBar.setVisibility(View.VISIBLE);
		listLinearLayout.setVisibility(View.GONE);
		
		ready();
		
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
		this.result = result;
		this.items.clear();
		
		List<DetailListItem> processedItems = new ArrayList<>();
		
		if (StringUtils.validate(result.getBestImageUrl())) {
			this.items.add(new ImageDetailItem(result.getBestImageUrl()).dataType(AdditionalDataType.THUMBNAIL));
		}
		
		for (AdditionalResultData additionalResultData : additionals) {
			Object data = additionalResultData.getData();
			AdditionalDataType type = additionalResultData.getType();
			DetailListItem item = null;
			
			switch (type) {
				case THUMBNAIL: {
					this.items.add(new ImageDetailItem((String) data));
					break;
				}
				
				case SIMPLE_HTML: {
					item = new HtmlDetailItem(additionalResultData.convert());
					break;
				}
				
				default: {
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
							// item = new StringDetailItem("#LIST/" + additionalResultData.convert());
							item = new StringDetailItem(additionalResultData.convert());
						}
					} else if (data instanceof RatingResultData) {
						item = new RatingDetailItem((RatingResultData) data);
					} else {
						item = new StringDetailItem(additionalResultData.convert());
					}
					break;
				}
			}
			
			if (item != null) {
				processedItems.add(item.dataType(additionalResultData.getType()));
			}
		}
		
		this.items.add(new AddToWatchLaterListDetailItem(result));
		
		if (result.getParentProvider() instanceof Subscribable) {
			this.items.add(new AddToSubscriptionsListDetailItem(result));
		}
		
		this.items.addAll(processedItems);
		
		listLinearLayout.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		createNextDetailListItemView(new AsyncLayoutInflater(context), items.iterator());
	}
	
	private void createNextDetailListItemView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<DetailListItem> itemsIterator) {
		if (itemsIterator.hasNext() && isContextValid()) {
			final DetailListItem item = itemsIterator.next();
			
			int layout = R.layout.container_item_culture_searchngo_activitypage_detail_info_holder;
			if (item.needListToDisplay()) {
				layout = R.layout.container_item_culture_searchngo_activitypage_detail_info_listholder;
			}
			
			asyncLayoutInflater.inflate(layout, listLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (!destroyed) {
						TextView typeTextView = view.findViewById(R.id.container_item_culture_searchandgo_detail_info_holder_textview_type);
						
						if (item.getDataType() == null) {
							typeTextView.setVisibility(View.GONE);
						} else {
							typeTextView.setText(viewHelper.enumToStringCacheTranslation(item.getDataType()));
						}
						
						final View itemContainerView = view;
						
						if (item.getType() == DetailListItem.TYPE_CATEGORY) {
							RecyclerView recyclerView = view.findViewById(R.id.container_item_culture_searchandgo_detail_info_holder_recyclerview_container);
							FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
							flowLayoutManager.setAutoMeasureEnabled(true);
							
							recyclerView.setLayoutManager(flowLayoutManager);
							recyclerView.setAdapter(new CategoryItemViewAdapter(((CategoryDetailItem) item)));
							recyclerView.setHasFixedSize(true);
							recyclerView.setNestedScrollingEnabled(false);
							
							parent.addView(view);
							
							createNextDetailListItemView(asyncLayoutInflater, itemsIterator);
						} else {
							FrameLayout containerFrameLayout = view.findViewById(R.id.container_item_culture_searchandgo_detail_info_holder_framelayout_container);
							
							asyncLayoutInflater.inflate(item.getLayout(), containerFrameLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
								@Override
								public void onInflateFinished(View view, int resid, ViewGroup parent) {
									if (!destroyed) {
										switch (item.getType()) {
											case DetailListItem.TYPE_IMAGE: {
												new ImageItemViewBinder(view).bind((ImageDetailItem) item);
												break;
											}
											
											case DetailListItem.TYPE_BUTTON_ADD_TO_WATCH_LATER_LIST: {
												new AddToWatchLaterListItemViewBinder(view).bind((AddToWatchLaterListDetailItem) item);
												break;
											}
											
											case DetailListItem.TYPE_BUTTON_ADD_TO_SUBSCRIPTIONS_LIST: {
												new AddToSubscriptionsListItemViewBinder(view).bind((AddToSubscriptionsListDetailItem) item);
												break;
											}
											
											case DetailListItem.TYPE_STRING: {
												new StringItemViewBinder(view).bind((StringDetailItem) item);
												break;
											}
											
											case DetailListItem.TYPE_HTML: {
												new HtmlItemViewBinder(view).bind((HtmlDetailItem) item);
												break;
											}
											
											case DetailListItem.TYPE_CATEGORY: {
												throw new IllegalStateException("The item type for category should have already been processed before.");
											}
											
											case DetailListItem.TYPE_RATING: {
												new RatingItemViewBinder(view).bind((RatingDetailItem) item);
												break;
											}
											
											default: {
												throw new IllegalStateException("Unhandled item type: " + item.getType());
											}
										}
										
										parent.addView(view);
										listLinearLayout.addView(itemContainerView);
										
										createNextDetailListItemView(asyncLayoutInflater, itemsIterator);
									}
								}
							});
						}
					}
				}
			});
		}
	}
	
	class ImageItemViewBinder {
		private ImageView contentImageView;
		
		public ImageItemViewBinder(View view) {
			contentImageView = (ImageView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_image_imageview_container);
		}
		
		public void bind(final ImageDetailItem imageItem) {
			viewHelper.downloadToImageView(contentImageView, imageItem.getUrl(), result.getRequireHeaders2());
			
			contentImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ImageViewerActivity.start(imageItem.getUrl());
				}
			});
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
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_image;
		}
	}
	
	class AddToListItemViewBinder<T extends AddToListDetailItem> {
		
		/* Variables */
		private String addString, removeString;
		private Button addToListButton;
		
		/* Constructor */
		public AddToListItemViewBinder(View view, int addStringId, int removeStringId) {
			addString = getString(addStringId);
			removeString = getString(removeStringId);
			
			addToListButton = (Button) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_add_to_list_button_add);
		}
		
		public void bind(final T item) {
			updateButtonText(item);
			
			addToListButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					item.updateState();
					updateButtonText(item);
				}
			});
		}
		
		public void updateButtonText(T item) {
			String text = addString;
			
			if (item.isInList()) {
				text = removeString;
			}
			
			addToListButton.setText(text);
		}
	}
	
	abstract class AddToListDetailItem extends DetailListItem {
		
		/* Variables */
		private MyList myList;
		private MyListable myListable;
		
		/* Constructor */
		public AddToListDetailItem(MyList myList, MyListable myListable) {
			this.myList = myList;
			this.myListable = myListable;
		}
		
		public boolean isInList() {
			return myList.contains(myListable);
		}
		
		public void updateState() {
			if (myList.contains(myListable)) {
				myList.remove(myListable);
			} else {
				myList.add(myListable);
			}
		}
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_add_to_list;
		}
		
		public MyListable getMyListable() {
			return myListable;
		}
		
	}
	
	class AddToWatchLaterListItemViewBinder extends AddToListItemViewBinder<AddToWatchLaterListDetailItem> {

		/* Constructor */
		public AddToWatchLaterListItemViewBinder(View view) {
			super(view, R.string.boxplay_culture_searchngo_detail_info_button_add_to_list_watch_later, R.string.boxplay_culture_searchngo_detail_info_button_remove_to_list_watch_later);
		}
		
	}
	
	class AddToWatchLaterListDetailItem extends AddToListDetailItem {

		/* Constructor */
		public AddToWatchLaterListDetailItem(MyListable myListable) {
			super(myListManager.getWatchLaterMyList(), myListable);
		}
		
		@Override
		public int getType() {
			return TYPE_BUTTON_ADD_TO_WATCH_LATER_LIST;
		}
		
	}
	
	class AddToSubscriptionsListItemViewBinder extends AddToListItemViewBinder<AddToSubscriptionsListDetailItem> {

		/* Constructor */
		public AddToSubscriptionsListItemViewBinder(View view) {
			super(view, R.string.boxplay_culture_searchngo_detail_info_button_add_to_list_subscriptions, R.string.boxplay_culture_searchngo_detail_info_button_remove_to_list_subscriptions);
		}
		
	}
	
	class AddToSubscriptionsListDetailItem extends AddToListDetailItem {

		/* Constructor */
		public AddToSubscriptionsListDetailItem(MyListable myListable) {
			super(myListManager.getSubscriptionsMyList(), myListable);
		}
		
		@Override
		public int getType() {
			return TYPE_BUTTON_ADD_TO_SUBSCRIPTIONS_LIST;
		}
		
	}
	
	class StringItemViewBinder {
		private TextView contentTextView;
		
		public StringItemViewBinder(View view) {
			contentTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_string_textview_container);
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
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_string;
		}
	}
	
	class HtmlItemViewBinder {
		private TextView contentTextView;
		
		public HtmlItemViewBinder(View view) {
			contentTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_string_textview_container);
		}
		
		public void bind(HtmlDetailItem stringItem) {
			contentTextView.setText(stringItem.getSpanned());
		}
	}
	
	class HtmlDetailItem extends DetailListItem {
		private Spanned spanned;
		
		@SuppressWarnings("deprecation")
		public HtmlDetailItem(String html) {
			this(Html.fromHtml(html));
		}
		
		public HtmlDetailItem(Spanned spanned) {
			this.spanned = spanned;
		}
		
		public Spanned getSpanned() {
			return spanned;
		}
		
		@Override
		public int getType() {
			return TYPE_HTML;
		}
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_string;
		}
	}
	
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
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_category;
		}
		
		@Override
		public boolean needListToDisplay() {
			return true;
		}
	}
	
	class RatingItemViewBinder {
		private RatingBar starRatingBar;
		private TextView stringTextView;
		
		public RatingItemViewBinder(View view) {
			starRatingBar = (RatingBar) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_rating_ratingbar_bar);
			stringTextView = (TextView) view.findViewById(R.id.item_culture_searchandgo_activitypage_detail_info_rating_textview_string);
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
		
		@Override
		public int getLayout() {
			return R.layout.item_culture_searchandgo_activitypage_detail_info_rating;
		}
	}
	
	abstract class DetailListItem {
		private AdditionalDataType dataType;
		
		public static final int TYPE_IMAGE = 0;
		public static final int TYPE_BUTTON_ADD_TO_WATCH_LATER_LIST = 1;
		public static final int TYPE_BUTTON_ADD_TO_SUBSCRIPTIONS_LIST = 2;
		public static final int TYPE_STRING = 3;
		public static final int TYPE_HTML = 4;
		public static final int TYPE_CATEGORY = 5;
		public static final int TYPE_RATING = 6;
		
		public abstract int getType();
		
		public abstract int getLayout();
		
		public boolean needListToDisplay() {
			return false;
		}
		
		public AdditionalDataType getDataType() {
			return dataType;
		}
		
		public DetailListItem dataType(AdditionalDataType dataType) {
			this.dataType = dataType;
			return this;
		}
	}
	
}