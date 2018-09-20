package caceresenzo.apps.boxplay.fragments.store;

//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.ANIMES;
//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.MOVIES;
//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.RANDOM;
//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.RECOMMENDED;
//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.SERIES;
//import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.YOURLIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import caceresenzo.android.libs.dialog.DialogUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.VideoActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.store.video.BaseVideoStoreElement;
import caceresenzo.libs.boxplay.store.video.TagsCorresponder;
import caceresenzo.libs.boxplay.store.video.implementations.SimpleVideoStoreElement;
import caceresenzo.libs.string.SimpleLineStringBuilder;
import caceresenzo.libs.string.StringUtils;

public class PageVideoStoreFragment extends StorePageFragment {
	
	/* Constants */
	public static int IMAGE_SIZE_WIDTH = 320;
	public static int IMAGE_SIZE_HEIGHT = 450;
	
	private static PageVideoStoreFragment INSTANCE;
	
	private VideoStorePopulator videoStorePopulator = new VideoStorePopulator();
	private static View tutorialSlidableView;
	
	@Override
	protected void initializeViews(View view) {
		INSTANCE = this;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		onUserRefresh();
	}
	
	@Override
	public void onUserRefresh() {
		BoxPlayApplication.getManagers().getDataManager().fetchData(true);
	}
	
	@Override
	public void callDataUpdater(boolean newContent) {
		rowListItems.clear();
		
		videoStorePopulator.populate();
		
		finishUpdate(newContent);
	}
	
	@Override
	public StoreSearchHandler<BaseVideoStoreElement> createSearchHandler() {
		return new StoreSearchHandler<BaseVideoStoreElement>() {
			@Override
			public boolean onQueryTextChange(String newText) {
				if (recyclerView != null) {
					List<BaseVideoStoreElement> filteredModelList = filter(newText);
					
					recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
					recyclerView.setAdapter(new VideoListRowViewAdapter(filteredModelList));
				}
				return true;
			}
			
			public List<BaseVideoStoreElement> filter(String query) {
				query = query.toLowerCase();
				
				final List<BaseVideoStoreElement> filteredGroupList = new ArrayList<>();
				for (SimpleVideoStoreElement videoElement : BoxPlayApplication.getManagers().getVideoManager().getGroups()) {
					boolean stringContains = false;
					
					if (videoElement.getTitle().toLowerCase().contains(query)) {
						stringContains = true;
					} else {
						// for (VideoSeason season : videoElement.getSeasons()) {
						// if (season.getTitle().toLowerCase().contains(query)) {
						// stringContains = true;
						// break;
						// }
						// }
					}
					
					if (stringContains) {
						filteredGroupList.add(videoElement);
					}
				}
				return filteredGroupList;
			}
		};
	}
	
	class VideoStorePopulator extends StorePopulator {
		// private Map<VideoStoreSubCategory, List<BaseVideoStoreElement>> population;
		private Map<String, List<BaseVideoStoreElement>> population;
		
		public void populate() {
			population = new HashMap<>();
			// List<BaseVideoStoreElement> yourWatchingList = new ArrayList<BaseVideoStoreElement>();
			
			// for (VideoStoreSubCategory category : VideoStoreSubCategory.values()) {
			// if (category.equals(YOURLIST)) {
			// continue;
			// }
			// population.put(category, new ArrayList<BaseVideoStoreElement>());
			// }
			
			SimpleLineStringBuilder builder = new SimpleLineStringBuilder();
			
			TagsCorresponder tagsCorresponder = videoManager.getTagsCorresponder();
			
			if (tagsCorresponder == null) {
				return;
			}
			
			builder.appendln("FETCHED TAGS\n").appendln(tagsCorresponder == null ? "NULL INSTANCE" : StringUtils.join(tagsCorresponder.getTagsCorrespondances(), "\n"));
			
			for (SimpleVideoStoreElement videoElement : videoManager.getGroups()) {
				if (videoElement == null) {
					continue;
				}
				
				builder.appendln("Item: " + videoElement.getTitle()).appendln("Tags (long): " + videoElement.getTagsBitset().getValue());
				builder.appendln("Tags (string): ");
				for (String tag : tagsCorresponder.findCorrespondances(videoElement)) {
					builder.append(tag).append(", ");
					populate(tag, videoElement);
				}
				builder.appendln();
				builder.appendln();
				
				// if (videoGroup.isWatching()) {
				// yourWatchingList.add(videoGroup);
				// }
				// if (videoGroup.isRecommended()) {
				// populate(RECOMMENDED, videoGroup);
				// }
				// if (videoGroup.getVideoFileType().equals(VideoFileType.ANIME) || videoGroup.getVideoFileType().equals(VideoFileType.ANIMEMOVIE)) {
				// populate(ANIMES, videoGroup);
				// }
				// if (videoGroup.getVideoFileType().equals(VideoFileType.ANIMEMOVIE) || videoGroup.getVideoFileType().equals(VideoFileType.MOVIE)) {
				// populate(MOVIES, videoGroup);
				// }
				// if (videoGroup.getVideoFileType().equals(VideoFileType.SERIE)) {
				// populate(SERIES, videoGroup);
				// }
				// if (Randomizer.nextRangeInt(0, 10) == 5) {
				// populate(RANDOM, videoGroup);
				// }
			}
			DialogUtils.showDialog(BoxPlayActivity.getBoxPlayActivity(), "tags output", builder.toString());
			
			// if (!yourWatchingList.isEmpty()) {
			// // rowListItems.add(new TitleRowItem(YOURLIST));
			//
			// String headingTitle = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(YOURLIST);
			// RowListItemConfig rowListItemConfig = new RowListItemConfig().title(headingTitle);
			//
			// if (yourWatchingList.size() == 1) {
			// rowListItems.add(new BaseVideoStoreElementRowItem(yourWatchingList.get(0)).configurate(rowListItemConfig));
			// } else {
			// rowListItems.add(new VideoListRowItem(yourWatchingList).configurate(rowListItemConfig));
			// }
			// }
			
			List<String> keys = new ArrayList<>(population.keySet());
			Collections.shuffle(keys);
			for (String tag : keys) {
				if (!population.get(tag).isEmpty()) {
					// rowListItems.add(new TitleRowItem(category));
					
					String headingTitle = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(tag);
					RowListItemConfig rowListItemConfig = new RowListItemConfig().title(headingTitle);
					
					// if (tag.equals(RANDOM) || population.get(tag).size() < 2) {
					// rowListItems.add(new BaseVideoStoreElementRowItem(population.get(tag).get(0)).configurate(rowListItemConfig));
					// } else {
					rowListItems.add(new VideoListRowItem(population.get(tag)).configurate(rowListItemConfig));
					// }
				}
			}
		}
		
		// private void populate(VideoStoreSubCategory category, BaseVideoStoreElement element) {
		// population.get(category).add(element);
		// }
		
		private void populate(String tag, BaseVideoStoreElement element) {
			if (!population.containsKey(tag)) {
				population.put(tag, new ArrayList<BaseVideoStoreElement>());
			}
			
			population.get(tag).add(element);
		}
	}
	
	public static enum VideoStoreSubCategory {
		YOURLIST, RECOMMENDED, ANIMES, MOVIES, SERIES, RANDOM, RELEASE;
	}
	
	/*
	 * Element
	 */
	protected static class BaseVideoStoreElementRowViewAdapter extends RecyclerView.Adapter<BaseVideoStoreElementRowViewHolder> {
		private BaseVideoStoreElement element;
		
		public BaseVideoStoreElementRowViewAdapter(BaseVideoStoreElement element) {
			this.element = element;
		}
		
		@Override
		public BaseVideoStoreElementRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_video_element_cardview, viewGroup, false);
			return new BaseVideoStoreElementRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(BaseVideoStoreElementRowViewHolder viewHolder, int position) {
			viewHolder.bind(element);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	protected static class BaseVideoStoreElementRowViewHolder extends RecyclerView.ViewHolder {
		private TextView titleTextView, subtitleTextView;
		private ImageView thumbnailImageView;
		private View view;
		
		public BaseVideoStoreElementRowViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_video_element_layout_textview_title);
			subtitleTextView = (TextView) itemView.findViewById(R.id.item_store_page_video_element_layout_textview_subtitle);
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_store_page_video_element_layout_imageview_thumbnail);
		}
		
		public void bind(BaseVideoStoreElement item) {
			
			titleTextView.setText(item.getTitle());
			
			// if (group.hasSeason()) { TODO
			// subtitleTextView.setText(BoxPlayApplication.getBoxPlayApplication().getString(R.string.boxplay_store_video_season_view, group.getSeasons().size(), group.getSeasons().size() > 1 ? "s" : ""));
			// } else {
			// subtitleTextView.setText(BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(group.getVideoFileType()));
			// }
			
			downloadToImageView(thumbnailImageView, item.getImageUrl());
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// BoxPlayApplication.getViewHelper().startVideoActivity(view, group); TODO
					// BoxPlayApplication.getViewHelper().startVideoActivity(view, group);
					// VideoActivity.start(group); TODO
				}
			});
			
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					return true;
				}
			});
		}
	}
	
	protected static class BaseVideoStoreElementRowItem extends RowListItem {
		private BaseVideoStoreElement element;
		
		public BaseVideoStoreElementRowItem(BaseVideoStoreElement element) {
			this.element = element;
		}
		
		public BaseVideoStoreElement getVideoFile() {
			return element;
		}
		
		@Override
		public int getType() {
			return TYPE_VIDEO_ELEMENT;
		}
	}
	
	/*
	 * List
	 */
	protected static class VideoListRowViewAdapter extends RecyclerView.Adapter<VideoListRowViewHolder> {
		private List<BaseVideoStoreElement> elements;
		
		public VideoListRowViewAdapter(List<BaseVideoStoreElement> elements) {
			this.elements = elements;
		}
		
		@Override
		public VideoListRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_video_list_cardview, viewGroup, false);
			if (tutorialSlidableView == null) {
				tutorialSlidableView = view;
			}
			return new VideoListRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(VideoListRowViewHolder viewHolder, int position) {
			BaseVideoStoreElement item = elements.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return elements.size();
		}
	}
	
	protected static class VideoListRowViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private TextView titleTextView;
		private ImageView thumbnailImageView;
		
		public VideoListRowViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_video_list_layout_textview_title);
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_store_page_video_list_layout_imageview_thumbnail);
		}
		
		public void bind(final BaseVideoStoreElement item) {
			titleTextView.setText(item.getTitle());
			
			downloadToImageView(thumbnailImageView, item.getImageUrl());
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					VideoActivity.start(item);
				}
			});
			
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					return true;
				}
			});
		}
	}
	
	protected static class VideoListRowItem extends RowListItem {
		private List<BaseVideoStoreElement> elements;
		
		public VideoListRowItem(List<BaseVideoStoreElement> elements) {
			this.elements = elements;
		}
		
		public List<BaseVideoStoreElement> getBaseVideoStoreElements() {
			return elements;
		}
		
		@Override
		public int getType() {
			return TYPE_VIDEO_LIST;
		}
	}
	
	private static void downloadToImageView(ImageView imageView, String url) {
		BoxPlayApplication.getViewHelper().downloadToImageView(imageView, url, IMAGE_SIZE_HEIGHT, IMAGE_SIZE_WIDTH);
	}
	
	public static View getTutorialSlidableView() {
		return tutorialSlidableView;
	}
	
	public static PageVideoStoreFragment getVideoFragment() {
		return INSTANCE;
	}
	
}