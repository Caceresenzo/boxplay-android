package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.kyo.expandablelayout.ExpandableLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import caceresenzo.android.libs.intent.IntentUtils;
import caceresenzo.android.libs.internet.AndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.LocaleHelper;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.TutorialManager.Tutorialable;
import caceresenzo.apps.boxplay.managers.VideoManager;
import caceresenzo.libs.boxplay.models.element.BoxPlayElement;
import caceresenzo.libs.boxplay.models.server.ServerHosting;
import caceresenzo.libs.boxplay.models.store.video.VideoFile;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.models.store.video.VideoSeason;

public class VideoActivity extends BaseBoxPlayActivty implements Tutorialable {
	
	public static final String BUNDLE_KEY_VIDEO_GROUP_ITEM = "video_group_item";
	
	public static final String BUNDLE_KEY_VLC_EXTRA_POSITION = "extra_position";
	public static final String BUNDLE_KEY_VLC_EXTRA_DURATION = "extra_duration";
	
	private static VideoActivity INSTANCE;
	
	private VideoGroup videoGroup;
	private VideoSeason videoSeason;
	
	private AppBarLayout appBarLayout;
	private Toolbar toolbar;
	private ActionBar actionBar;
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private NestedScrollView nestedScrollView;
	private FloatingActionButton floatingActionButton;
	private RecyclerView videoRecyclerView;
	
	private ImageView videoImageView;
	private TextView seasonTextView;
	private Spinner seasonSpinner;
	private CheckBox seasonCheckBox;
	
	private VideoListViewAdapter videoListViewAdapter;
	
	private List<VideoItem> videoItems;
	
	private VideoItemViewHolder lastHolder;
	
	public VideoActivity() {
		super();
		
		videoItems = new ArrayList<>();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		INSTANCE = this;
		
		videoGroup = (VideoGroup) getIntent().getSerializableExtra(BUNDLE_KEY_VIDEO_GROUP_ITEM);
		if (videoGroup == null) {
			BoxPlayApplication.getBoxPlayApplication().toast(getString(R.string.boxplay_error_activity_invalid_data)).show();
			finish();
		}
		
		/* Finding back original */
		for (Object object : BoxPlayElement.getInstances().values()) {
			if (object.toString().equals(videoGroup.toString())) {
				videoGroup = (VideoGroup) object; // Un-serialization, finding original one
				break;
			}
		}
		
		initializeViews();
		
		changeSeason(videoSeason = videoGroup.getSeasons().get(0));
		
		BoxPlayApplication.getManagers().getTutorialManager().executeActivityTutorial(this);
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_VIDEO_GROUP_ITEM, (Serializable) videoGroup);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case BoxPlayApplication.REQUEST_ID_VLC_VIDEO:
				if (data == null || data.getExtras() == null) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_activity_vlc_error, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				long position = data.getExtras().getLong(BUNDLE_KEY_VLC_EXTRA_POSITION);
				long duration = data.getExtras().getLong(BUNDLE_KEY_VLC_EXTRA_DURATION);
				
				boolean extraPositionValid = !ViewHelper.DATEFORMAT_VIDEO_DURATION.format(new Date(position)).equals("23:59:59");
				boolean extraDurationValid = !ViewHelper.DATEFORMAT_VIDEO_DURATION.format(new Date(duration)).equals("00:00:00");
				
				if (!extraPositionValid || !extraDurationValid) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_activity_invalid_time, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				VideoManager videoManager = BoxPlayApplication.getManagers().getVideoManager();
				VideoFile video = videoManager.getLastVideoFileOpen();
				
				if (video == null) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_file_forget, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				video.newSavedTime(position);
				video.newDuration(duration);
				
				if (lastHolder != null && lastHolder.bindVideoItem.videoFile == video) {
					lastHolder.updateVideoFileItemInformations(video, false);
					lastHolder.bindVideoItem.expanded = true;
					lastHolder.expandableLayout.setExpanded(true);
				}
				
				if (!videoGroup.hasSeason()) { // Is movie
					lastHolder = videoItemViewHolderInstances.get(0);
					lastHolder.updateVideoFileItemInformations(video, false);
					videoListViewAdapter.notifyItemChanged(0);
				}
				
				videoManager.callConfigurator(video);
				break;
		}
	}
	
	private void initializeViews() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_video_coordinatorlayout_container);
		
		toolbar = (Toolbar) findViewById(R.id.activity_video_toolbar_bar);
		appBarLayout = (AppBarLayout) findViewById(R.id.activity_video_appbarlayout_container);
		
		collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.activity_video_collapsingtoolbarlayout_container);
		nestedScrollView = (NestedScrollView) findViewById(R.id.activity_video_nestedscrollview_container);
		
		floatingActionButton = (FloatingActionButton) findViewById(R.id.activity_video_floatingactionbutton_watch);
		videoImageView = (ImageView) findViewById(R.id.activity_video_imageview_header);
		
		seasonTextView = (TextView) findViewById(R.id.activity_video_textview_season_and_name);
		seasonSpinner = (Spinner) findViewById(R.id.activity_video_spinner_season_selector);
		seasonCheckBox = (CheckBox) findViewById(R.id.activity_video_checkbox_season_watched);
		
		videoRecyclerView = (RecyclerView) this.findViewById(R.id.activity_video_recyclerview_videos);
		
		/*
		 * Code
		 */
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		collapsingToolbarLayout.setTitle(videoGroup.getTitle());
		floatingActionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (videoGroup.isWatching()) {
					floatingActionButton.setImageResource(R.drawable.icon_eye_open_96px);
					Snackbar.make(coordinatorLayout, R.string.boxplay_store_video_data_unwatching, Snackbar.LENGTH_LONG).show();
				} else {
					floatingActionButton.setImageResource(R.drawable.icon_eye_close_96px);
					Snackbar.make(coordinatorLayout, R.string.boxplay_store_video_data_watching, Snackbar.LENGTH_LONG).show();
				}
				
				videoGroup.setAsWatching(!videoGroup.isWatching());
				
				BoxPlayApplication.getManagers().getVideoManager().callConfigurator(videoGroup);
			}
		});
		floatingActionButton.setImageResource(videoGroup.isWatching() ? R.drawable.icon_eye_close_96px : R.drawable.icon_eye_open_96px);
		
		BoxPlayApplication.getViewHelper().downloadToImageView(videoImageView, videoGroup.getGroupImageUrl());
		
		seasonCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean checked = seasonCheckBox.isChecked();
				
				videoSeason.asWatched(checked);
				
				BoxPlayApplication.getManagers().getVideoManager().callConfigurator(videoSeason);
			}
		});
		
		if (videoGroup.hasSeason()) {
			seasonTextView.setText(getString(R.string.boxplay_store_video_season_selector_result, videoGroup.getSeasons().get(0).getTitle(), videoGroup.getSeasons().get(0).getSeasonValue()));
			
			seasonSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					;
				}
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (videoGroup.hasSeason()) {
						videoSeason = videoGroup.getSeasons().get(position);
						
						seasonTextView.setText(getString(R.string.boxplay_store_video_season_selector_result, videoSeason.getTitle()));
						changeSeason(videoSeason);
					}
				}
			});
		} else {
			seasonTextView.setText(videoGroup.getTitle());
		}
		ArrayAdapter<VideoSeason> dataAdapter = new SeasonSpinnerAdapter(this, videoGroup.getSeasons());
		seasonSpinner.setAdapter(dataAdapter);
		
		videoRecyclerView.setAdapter(videoListViewAdapter = new VideoListViewAdapter(videoItems));
		videoRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		videoRecyclerView.setHasFixedSize(false);
		videoRecyclerView.setNestedScrollingEnabled(false);
	}
	
	private void changeSeason(VideoSeason videoSeason) {
		this.videoSeason = videoSeason;
		
		seasonCheckBox.setChecked(videoSeason.isWatched());
		
		videoItems.clear();
		for (VideoFile video : videoSeason.getVideos()) {
			videoItems.add(new VideoItem(video));
		}
		videoListViewAdapter.notifyDataSetChanged();
		
		String imageUrl = videoSeason.getImageHdUrl();
		if (imageUrl == null) {
			imageUrl = videoSeason.getImageUrl();
		}
		
		if (imageUrl != null) {
			BoxPlayApplication.getViewHelper().downloadToImageView(videoImageView, imageUrl);
		}
		
		appBarLayout.setExpanded(true, true);
		nestedScrollView.getParent().requestChildFocus(nestedScrollView, nestedScrollView);
	}
	
	class SeasonSpinnerAdapter extends ArrayAdapter<VideoSeason> {
		public SeasonSpinnerAdapter(Context context, List<VideoSeason> objects) {
			super(context, android.R.layout.simple_spinner_item, objects);
		}
		
		@SuppressWarnings("deprecation")
		private LinearLayout createTextView(int position, boolean useColor, boolean addDropdownArrow) {
			TextView textView = new TextView(getContext());
			textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			int[] attrs = new int[] { R.attr.selectableItemBackground };
			TypedArray typedArray = getContext().obtainStyledAttributes(attrs);
			int backgroundResource = typedArray.getResourceId(0, 0);
			textView.setBackgroundResource(backgroundResource);
			typedArray.recycle();
			
			if (videoGroup.hasSeason()) {
				textView.setText(getString(R.string.boxplay_store_video_season_selector_item, videoGroup.getSeasons().get(position).getSeasonValue()));
			} else {
				textView.setText(getString(R.string.boxplay_store_video_season_selector_item_no_season));
			}
			
			int targetColorId = R.color.white;
			
			if (addDropdownArrow) {
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_action_arrow_drop_down, 0, 0, 0);
				textView.setPadding(16, 16, 0, 16);
			} else {
				textView.setPadding(32, 16, 0, 16);
			}
			
			if (videoGroup.getSeasons().get(position).isWatched()) {
				targetColorId = R.color.colorAccent;
			}
			
			if (useColor && position == seasonSpinner.getSelectedItemPosition()) {
				targetColorId = R.color.colorAccent;
			}
			
			textView.setTextColor(VideoActivity.this.getResources().getColor(targetColorId));
			
			if (Build.VERSION.SDK_INT >= 23) {
				textView.setCompoundDrawableTintList(VideoActivity.this.getResources().getColorStateList(targetColorId));
			}
			
			LinearLayout linearLayout = new LinearLayout(getContext());
			linearLayout.addView(textView);
			linearLayout.setBackgroundColor(VideoActivity.this.getResources().getColor(R.color.colorPrimary));
			return linearLayout;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			return createTextView(position, true, true);
		};
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return createTextView(position, true, false);
		}
	}
	
	class VideoListViewAdapter extends RecyclerView.Adapter<VideoItemViewHolder> {
		private List<VideoItem> list;
		
		public VideoListViewAdapter(List<VideoItem> list) {
			this.list = list;
		}
		
		@Override
		public VideoItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_layout, viewGroup, false);
			return new VideoItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(VideoItemViewHolder viewHolder, int position) {
			VideoItem item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	public static List<VideoItemViewHolder> videoItemViewHolderInstances = new ArrayList<VideoItemViewHolder>();
	
	class VideoItemViewHolder extends RecyclerView.ViewHolder {
		
		private OnClickListener viewOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				VideoItemViewHolder holder = (VideoItemViewHolder) view.getTag();
				
				/**
				 * @Movie
				 */
				if (!videoGroup.hasSeason()) {
					holder.expandableLayout.setExpanded(true, false);
					holder.bindVideoItem.expanded = true;
					return;
				}
				
				/**
				 * @Multiple
				 */
				resetExpensableLayout();
				boolean result = holder.expandableLayout.toggleExpansion();
				VideoItem info = videoItems.get(holder.position);
				info.expanded = result ? !info.expanded : info.expanded;
				
				lastHolder = holder;
			}
		};
		
		public void resetExpensableLayout() {
			for (VideoItemViewHolder holder : videoItemViewHolderInstances) {
				holder.expandableLayout.setExpanded(false, true);
				holder.bindVideoItem.expanded = false;
			}
		}
		
		private ExpandableLayout.OnExpandListener expandableLayoutItemOnExpandListener = new ExpandableLayout.OnExpandListener() {
			@Override
			public void onToggle(ExpandableLayout view, View child, boolean isExpanded) {
				;
			}
			
			@Override
			public void onExpandOffset(ExpandableLayout view, View child, float offset, boolean isExpanding) {
				;
			}
		};
		
		public int position;
		public ExpandableLayout expandableLayout;
		public View relativeLayout;
		public TextView episodeTextView, timeTextView, languageTextView;
		public ImageView hostIconImageView;
		public SeekBar progressSeekBar;
		public Button playButton, downloadButton, watchButton, shareButton, shareUrlButton;
		public VideoItem bindVideoItem;
		
		public VideoItemViewHolder(View itemView) {
			super(itemView);
			videoItemViewHolderInstances.add(this);
			
			// Parent
			expandableLayout = (ExpandableLayout) itemView.findViewById(R.id.item_video_layout_expandablelayout_item_container);
			relativeLayout = (View) itemView.findViewById(R.id.item_video_layout_relativelayout_parent_container);
			episodeTextView = (TextView) itemView.findViewById(R.id.item_video_layout_textview_episode_title);
			progressSeekBar = (SeekBar) itemView.findViewById(R.id.item_video_layout_seekbar_saved_progress);
			timeTextView = (TextView) itemView.findViewById(R.id.item_video_layout_textview_saved_time);
			languageTextView = (TextView) itemView.findViewById(R.id.item_video_layout_textview_language);
			hostIconImageView = (ImageView) itemView.findViewById(R.id.item_video_layout_imageview_host_icon);
			
			// Child
			playButton = (Button) itemView.findViewById(R.id.item_video_layout_item_button_play);
			downloadButton = (Button) itemView.findViewById(R.id.item_video_layout_item_button_download);
			watchButton = (Button) itemView.findViewById(R.id.item_video_layout_item_button_watch);
			shareButton = (Button) itemView.findViewById(R.id.item_video_layout_item_button_share);
			shareUrlButton = (Button) itemView.findViewById(R.id.item_video_layout_item_button_share_url);
		}
		
		@SuppressWarnings("deprecation")
		public void bind(VideoItem item) {
			bindVideoItem = item;
			final VideoFile video = item.videoFile;
			
			position = getAdapterPosition();
			
			/**
			 * @Parent
			 */
			
			expandableLayout.setOnExpandListener(expandableLayoutItemOnExpandListener);
			relativeLayout.setOnClickListener(viewOnClickListener);
			progressSeekBar.setOnClickListener(viewOnClickListener);
			progressSeekBar.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					viewOnClickListener.onClick(view);
					return true;
				}
			});
			
			expandableLayout.setTag(this);
			relativeLayout.setTag(this);
			progressSeekBar.setTag(this);
			timeTextView.setTag(this);
			languageTextView.setTag(this);
			
			expandableLayout.setExpanded(item.expanded, false);
			episodeTextView.setText(getString(R.string.boxplay_store_video_activity_episode_title, BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(item.videoFile.getVideoType()), item.videoFile.getRawEpisodeValue()));
			languageTextView.setText(getString(R.string.boxplay_store_video_activity_episode_language, BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(item.videoFile.getLanguage())));
			progressSeekBar.getProgressDrawable().setColorFilter(VideoActivity.this.getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
			
			boolean iconImageViewAvailable = false;
			if (BoxPlayApplication.getManagers().getServerManager() != null && BoxPlayApplication.getManagers().getServerManager().getServerHostings().size() > 0) {
				for (ServerHosting hosting : BoxPlayApplication.getManagers().getServerManager().getServerHostings()) {
					if (video.getUrl() != null && video.getUrl().startsWith(hosting.getStartingStringUrl()) && hosting.getIconUrl() != null && video.isAvailable()) {
						iconImageViewAvailable = true;
						BoxPlayApplication.getViewHelper().downloadToImageView(hostIconImageView, hosting.getIconUrl());
						break;
					}
				}
			}
			
			if (!iconImageViewAvailable) {
				hostIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_cloud_off_light));
			}
			
			updateVideoFileItemInformations(video);
			
			/**
			 * @Child
			 */
			
			playButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					BoxPlayApplication.getManagers().getVideoManager().openVLC(video);
				}
			});
			
			downloadButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					AndroidDownloader.askDownload(BoxPlayApplication.getBoxPlayApplication(), Uri.parse(video.getUrl()));
				}
			});
			
			watchButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					boolean isNotWatched = !video.isWatched();
					
					video.asWatched(isNotWatched);
					
					updateVideoFileItemInformations(video);
					
					BoxPlayApplication.getManagers().getVideoManager().callConfigurator(video);
				}
			});
			
			shareButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// IntentUtils.shareText(BoxPlayApplication.getBoxPlayApplication(), parsedTitle + "\n\nLink: " + parsedUrl, parsedTitle); TODO
					BoxPlayApplication.getBoxPlayApplication().toast(getString(R.string.boxplay_error_not_implemented_yet)).show();
				}
			});
			
			shareUrlButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					IntentUtils.shareText(BoxPlayApplication.getBoxPlayApplication(), video.getUrl(), video.getUrl());
				}
			});
			
			/**
			 * @Movie
			 */
			if (!videoGroup.hasSeason()) {
				// episodeTextView.setText(getString(R.string.boxplay_store_video_activity_movie_title, BoxPlayApplication.getViewHelper().videoEnumTypeToStringTranslation(item.videoFile.getFileType()), BoxPlayApplication.getViewHelper().videoEnumTypeToStringTranslation(item.videoFile.getLanguage())));
				episodeTextView.setVisibility(View.GONE);
				languageTextView.setText(getString(R.string.boxplay_store_video_activity_episode_language, BoxPlayApplication.getViewHelper().enumToStringCacheTranslation(item.videoFile.getLanguage())));
				
				bindVideoItem.expanded = true;
				expandableLayout.setExpanded(true, false);
			}
		}
		
		private void updateVideoFileItemInformations(final VideoFile video) {
			updateVideoFileItemInformations(video, true);
		}
		
		@SuppressWarnings("deprecation")
		private void updateVideoFileItemInformations(final VideoFile video, boolean disableSnackbarConfirm) {
			// Default action
			progressSeekBar.getProgressDrawable().setColorFilter(VideoActivity.this.getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
			
			// Video unavailable
			if (!video.isAvailable()) {
				progressSeekBar.setVisibility(View.INVISIBLE);
				languageTextView.setVisibility(View.INVISIBLE);
				timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_unavailable);
				
				setGlobalButtonEnabled(false);
				return;
			}
			
			// Video available
			progressSeekBar.setVisibility(View.VISIBLE);
			languageTextView.setVisibility(View.VISIBLE);
			timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_available);
			setGlobalButtonEnabled(true);
			
			// Already watched
			if (video.isWatched()) {
				progressSeekBar.getProgressDrawable().setColorFilter(VideoActivity.this.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
				progressSeekBar.setProgress(100);
				timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_watched);
				watchButton.setText(R.string.boxplay_store_video_button_unwatch);
				
				return;
			}
			
			watchButton.setText(R.string.boxplay_store_video_button_watch);
			
			// Not finished, and not saved progress (-1 or 0)
			if (video.getSavedTime() < 1) {
				progressSeekBar.getProgressDrawable().setColorFilter(VideoActivity.this.getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
				
				return;
			}
			
			// Not finished, but saved progress
			progressSeekBar.getProgressDrawable().setColorFilter(VideoActivity.this.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
			progressSeekBar.setProgress((int) ((video.getSavedTime() * 100) / video.getDuration()));
			timeTextView.setText(getString(R.string.boxplay_store_video_activity_episode_time, ViewHelper.DATEFORMAT_VIDEO_DURATION.format(new Date(video.getSavedTime())), ViewHelper.DATEFORMAT_VIDEO_DURATION.format(new Date(video.getDuration()))));
			
			if (!disableSnackbarConfirm) {
				// if (video.getSavedTime() > video.getDuration() * 0.80) {
				// Snackbar.make(coordinatorLayout, R.string.boxplay_store_video_action_mark_as_watched, Snackbar.LENGTH_LONG).setAction(R.string.boxplay_store_video_action_mark_as_watched_ok, new OnClickListener() {
				// @Override
				// public void onClick(View view) {
				// video.asWatched(true);
				// updateVideoFileItemInformations(video);
				// }
				// }).show();
				// }
				
				if (lastHolder != null) {
					videoListViewAdapter.notifyItemChanged(lastHolder.position);
				}
			}
			return;
		}
		
		private void setGlobalButtonEnabled(boolean enabled) {
			playButton.setEnabled(BoxPlayApplication.getViewHelper().isVlcInstalled() ? enabled : false);
			downloadButton.setEnabled(enabled);
			watchButton.setEnabled(enabled);
			shareButton.setEnabled(enabled);
			shareUrlButton.setEnabled(enabled);
			// TODO: castButton.setEnabled(enabled);
		}
	}
	
	static class VideoItem {
		boolean expanded;
		VideoFile videoFile;
		
		public VideoItem(VideoFile videoFile) {
			this.videoFile = videoFile;
		}
	}
	
	public static final int TUTORIAL_PROGRESS_ARROW = 0, //
			TUTORIAL_PROGRESS_WATCHING_LIST = 1, //
			TUTORIAL_PROGRESS_SEASON_SELECTOR = 2, //
			TUTORIAL_PROGRESS_WATCHED_SEASON = 3, //
			TUTORIAL_PROGRESS_EPISODES = 4; //
	
	@SuppressWarnings("deprecation")
	@Override
	public TapTargetSequence getTapTargetSequence() {
		List<TapTarget> sequences = new ArrayList<TapTarget>();
		Display display = getWindowManager().getDefaultDisplay();
		
		Rect rectangle = new Rect(24, 24, 24, 24);
		rectangle.offsetTo(display.getWidth() / 2, display.getHeight() / 2);
		
		sequences.add( //
				TapTarget.forToolbarNavigationIcon(toolbar, getString(R.string.boxplay_tutorial_video_back_title), getString(R.string.boxplay_tutorial_video_back_description)) //
						.id(TUTORIAL_PROGRESS_ARROW) //
						.dimColor(android.R.color.black) // Background
						.outerCircleColor(R.color.colorAccent) // Big circle
						.targetCircleColor(R.color.colorPrimary) // Moving circle color (animation)
						.textColor(android.R.color.black) //
						.transparentTarget(true) //
						.cancelable(false) //
		); //
		
		sequences.add( //
				TapTarget.forView(floatingActionButton, getString(R.string.boxplay_tutorial_video_watch_list_title), getString(R.string.boxplay_tutorial_video_watch_list_description)) //
						.id(TUTORIAL_PROGRESS_WATCHING_LIST) //
						.dimColor(android.R.color.black) //
						.outerCircleColor(R.color.colorAccent) //
						.targetCircleColor(R.color.colorPrimary) //
						.textColor(android.R.color.black) //
						.transparentTarget(true) //
						.cancelable(false) //
		); //
		
		sequences.add( //
				TapTarget.forView(seasonSpinner, getString(R.string.boxplay_tutorial_video_season_selector_title), getString(R.string.boxplay_tutorial_video_season_selector_description)) //
						.id(TUTORIAL_PROGRESS_SEASON_SELECTOR) //
						.dimColor(android.R.color.black) //
						.outerCircleColor(R.color.colorAccent) //
						.targetCircleColor(R.color.colorPrimary) //
						.textColor(android.R.color.black) //
						.transparentTarget(true) //
						.cancelable(false) //
		); //
		
		sequences.add( //
				TapTarget.forView(seasonCheckBox, getString(R.string.boxplay_tutorial_video_season_watched_title), getString(R.string.boxplay_tutorial_video_season_watched_description)) //
						.id(TUTORIAL_PROGRESS_WATCHED_SEASON) //
						.dimColor(android.R.color.black) //
						.outerCircleColor(R.color.colorAccent) //
						.targetCircleColor(R.color.colorPrimary) //
						.textColor(android.R.color.black) //
						.transparentTarget(true) //
						.cancelable(false) //
		); //
		
		sequences.add( //
				TapTarget.forBounds(rectangle, getString(R.string.boxplay_tutorial_video_episodes_title), getString(R.string.boxplay_tutorial_video_episodes_description)) //
						.id(TUTORIAL_PROGRESS_EPISODES) //
						.dimColor(android.R.color.black) //
						.outerCircleColor(R.color.colorAccent) //
						.targetCircleColor(R.color.colorPrimary) //
						.textColor(android.R.color.black) //
						.transparentTarget(true) //
						.cancelable(false) //
		); //
		
		return new TapTargetSequence(this) //
				.targets(sequences).listener(new TapTargetSequence.Listener() {
					@Override
					public void onSequenceFinish() {
						BoxPlayApplication.getManagers().getTutorialManager().saveTutorialFinished(VideoActivity.this);
					}
					
					@Override
					public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
						;
					}
					
					@Override
					public void onSequenceCanceled(TapTarget lastTarget) {
						; // Impossible
					}
					
				});
	}
	
	public static void start(VideoGroup videoGroup) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, VideoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_VIDEO_GROUP_ITEM, (Serializable) videoGroup);
		
		application.startActivity(intent);
	}
	
	public static VideoActivity getVideoActivity() {
		return (VideoActivity) INSTANCE;
	}
	
}