package caceresenzo.apps.boxplay.fragments.store;

import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.ANIMES;
import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.MOVIES;
import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.RANDOM;
import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.RECOMMENDED;
import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.SERIES;
import static caceresenzo.apps.boxplay.fragments.store.PageVideoStoreFragment.VideoStoreSubCategory.YOURLIST;

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
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.VideoActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.models.element.implementations.VideoElement;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.models.store.video.VideoSeason;
import caceresenzo.libs.boxplay.models.store.video.enums.VideoFileType;
import caceresenzo.libs.random.Randomizer;

public class PageVideoStoreFragment extends StorePageFragment {
	
	private VideoStorePopulator videoStorePopulator = new VideoStorePopulator();
	private static View tutorialSlidableView;
	
	@Override
	protected void initializeViews(View view) {
		;
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
	public StoreSearchHandler<VideoElement> createSearchHandler() {
		return new StoreSearchHandler<VideoElement>() {
			@Override
			public boolean onQueryTextChange(String newText) {
				if (recyclerView != null) {
					List<VideoElement> filteredModelList = filter(newText);
					
					recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
					recyclerView.setAdapter(new VideoListRowViewAdapter(filteredModelList));
				}
				return true;
			}
			
			public List<VideoElement> filter(String query) {
				query = query.toLowerCase();
				
				final List<VideoElement> filteredGroupList = new ArrayList<>();
				for (VideoGroup group : BoxPlayApplication.getManagers().getVideoManager().getGroups()) {
					boolean stringContains = false;
					
					if (group.getTitle().toLowerCase().contains(query)) {
						stringContains = true;
					} else {
						for (VideoSeason season : group.getSeasons()) {
							if (season.getTitle().toLowerCase().contains(query)) {
								stringContains = true;
								break;
							}
						}
					}
					
					if (stringContains) {
						filteredGroupList.add(group);
					}
				}
				return filteredGroupList;
			}
		};
	}
	
	class VideoStorePopulator extends StorePopulator {
		private Map<VideoStoreSubCategory, List<VideoElement>> population;
		
		public void populate() {
			population = new HashMap<VideoStoreSubCategory, List<VideoElement>>();
			List<VideoElement> yourWatchingList = new ArrayList<VideoElement>();
			
			for (VideoStoreSubCategory category : VideoStoreSubCategory.values()) {
				if (category.equals(YOURLIST)) {
					continue;
				}
				population.put(category, new ArrayList<VideoElement>());
			}
			
			for (VideoGroup videoGroup : BoxPlayApplication.getManagers().getVideoManager().getGroups()) {
				if (videoGroup == null) {
					continue;
				}
				if (videoGroup.isWatching()) {
					yourWatchingList.add(videoGroup);
				}
				if (videoGroup.isRecommended()) {
					populate(RECOMMENDED, videoGroup);
				}
				if (videoGroup.getVideoFileType().equals(VideoFileType.ANIME) || videoGroup.getVideoFileType().equals(VideoFileType.ANIMEMOVIE)) {
					populate(ANIMES, videoGroup);
				}
				if (videoGroup.getVideoFileType().equals(VideoFileType.ANIMEMOVIE) || videoGroup.getVideoFileType().equals(VideoFileType.MOVIE)) {
					populate(MOVIES, videoGroup);
				}
				if (videoGroup.getVideoFileType().equals(VideoFileType.SERIE)) {
					populate(SERIES, videoGroup);
				}
				if (Randomizer.nextRangeInt(0, 10) == 5) {
					populate(RANDOM, videoGroup);
				}
			}
			
			if (!yourWatchingList.isEmpty()) {
				// rowListItems.add(new TitleRowItem(YOURLIST));
				
				String headingTitle = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(YOURLIST);
				RowListItemConfig rowListItemConfig = new RowListItemConfig().title(headingTitle);
				
				if (yourWatchingList.size() == 1) {
					rowListItems.add(new VideoElementRowItem(yourWatchingList.get(0)).configurate(rowListItemConfig));
				} else {
					rowListItems.add(new VideoListRowItem(yourWatchingList).configurate(rowListItemConfig));
				}
			}
			
			List<VideoStoreSubCategory> keys = new ArrayList<VideoStoreSubCategory>(population.keySet());
			Collections.shuffle(keys);
			for (VideoStoreSubCategory category : keys) {
				if (!population.get(category).isEmpty()) {
					// rowListItems.add(new TitleRowItem(category));
					
					String headingTitle = BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(category);
					RowListItemConfig rowListItemConfig = new RowListItemConfig().title(headingTitle);
					
					if (category.equals(RANDOM) || population.get(category).size() < 2) {
						rowListItems.add(new VideoElementRowItem(population.get(category).get(0)).configurate(rowListItemConfig));
					} else {
						rowListItems.add(new VideoListRowItem(population.get(category)).configurate(rowListItemConfig));
					}
				}
			}
		}
		
		private void populate(VideoStoreSubCategory category, VideoElement element) {
			population.get(category).add(element);
		}
	}
	
	public static enum VideoStoreSubCategory {
		YOURLIST, RECOMMENDED, ANIMES, MOVIES, SERIES, RANDOM, RELEASE;
	}
	
	/*
	 * Element
	 */
	protected static class VideoElementRowViewAdapter extends RecyclerView.Adapter<VideoElementRowViewHolder> {
		private VideoElement element;
		
		public VideoElementRowViewAdapter(VideoElement element) {
			this.element = element;
		}
		
		@Override
		public VideoElementRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_store_page_video_element_cardview, viewGroup, false);
			return new VideoElementRowViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(VideoElementRowViewHolder viewHolder, int position) {
			viewHolder.bind(element);
		}
		
		@Override
		public int getItemCount() {
			return 1;
		}
	}
	
	protected static class VideoElementRowViewHolder extends RecyclerView.ViewHolder {
		private TextView titleTextView, subtitleTextView;
		private ImageView thumbnailImageView;
		private View view;
		
		public VideoElementRowViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			titleTextView = (TextView) itemView.findViewById(R.id.item_store_page_video_element_layout_textview_title);
			subtitleTextView = (TextView) itemView.findViewById(R.id.item_store_page_video_element_layout_textview_subtitle);
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_store_page_video_element_layout_imageview_thumbnail);
		}
		
		public void bind(VideoElement item) {
			if (item instanceof VideoGroup) {
				final VideoGroup group = (VideoGroup) item;
				
				titleTextView.setText(group.getTitle());
				
				if (group.hasSeason()) {
					subtitleTextView.setText(BoxPlayApplication.getBoxPlayApplication().getString(R.string.boxplay_store_video_season_view, group.getSeasons().size(), group.getSeasons().size() > 1 ? "s" : ""));
				} else {
					subtitleTextView.setText(BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(group.getVideoFileType()));
				}
				
				if (group.getGroupImageUrl() != null) {
					BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, group.getGroupImageUrl());
				}
				
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						// BoxPlayApplication.getViewHelper().startVideoActivity(view, group); TODO
						// BoxPlayApplication.getViewHelper().startVideoActivity(view, group);
						VideoActivity.start(group);
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
	}
	
	protected static class VideoElementRowItem extends RowListItem {
		private VideoElement element;
		
		public VideoElementRowItem(VideoElement element) {
			this.element = element;
		}
		
		public VideoElement getVideoFile() {
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
		private List<VideoElement> elements;
		
		public VideoListRowViewAdapter(List<VideoElement> elements) {
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
			VideoElement item = elements.get(position);
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
		
		public void bind(VideoElement item) {
			if (item instanceof VideoGroup) {
				final VideoGroup group = (VideoGroup) item;
				
				titleTextView.setText(group.getTitle());
				
				if (group.getGroupImageUrl() != null) {
					BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, group.getGroupImageUrl());
				}
				
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						// BoxPlayApplication.getViewHelper().startVideoActivity(view, group); TODO
						VideoActivity.start(group);
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
	}
	
	protected static class VideoListRowItem extends RowListItem {
		private List<VideoElement> elements;
		
		public VideoListRowItem(List<VideoElement> elements) {
			this.elements = elements;
		}
		
		public List<VideoElement> getVideoElements() {
			return elements;
		}
		
		@Override
		public int getType() {
			return TYPE_VIDEO_LIST;
		}
	}
	
	public static View getTutorialSlidableView() {
		return tutorialSlidableView;
	}
	
	public static PageVideoStoreFragment getVideoFragment() {
		return (PageVideoStoreFragment) INSTANCE;
	}
	
}