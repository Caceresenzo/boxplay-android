package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.kyo.expandablelayout.ExpandableLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
import caceresenzo.android.libs.intent.CommonIntentUtils;
import caceresenzo.android.libs.internet.AdmAndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.managers.VideoManager;
import caceresenzo.libs.boxplay.models.element.BoxPlayElement;
import caceresenzo.libs.boxplay.models.store.video.VideoFile;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.models.store.video.VideoSeason;

@SuppressWarnings("deprecation")
public class VideoActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = VideoActivity.class.getSimpleName();
	
	/* Constants */
	public static final int SNACKBAR_DURATION_DONE_WATCHING_PROMPT = 15000;
	public static final SimpleDateFormat DATEFORMAT_VIDEO_DURATION = new SimpleDateFormat("HH:mm:ss");
	
	/* Static */
	static {
		DATEFORMAT_VIDEO_DURATION.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_VIDEO_GROUP_ITEM = "video_group_item";
	
	public static final String BUNDLE_KEY_VLC_EXTRA_POSITION = "extra_position";
	public static final String BUNDLE_KEY_VLC_EXTRA_DURATION = "extra_duration";
	
	/* Instance */
	private static VideoActivity INSTANCE;
	
	/* Managers */
	private VideoManager videoManager;
	
	/* Element */
	private VideoGroup videoGroup;
	private VideoSeason selectedVideoSeason;
	
	/* Views */
	private AppBarLayout appBarLayout;
	private Toolbar toolbar;
	private ActionBar actionBar;
	private CollapsingToolbarLayout collapsingToolbarLayout;
	private NestedScrollView nestedScrollView;
	private FloatingActionButton floatingActionButton;
	private LinearLayout videosLinearLayout;
	
	private ImageView videoImageView;
	private TextView seasonTextView;
	private Spinner seasonSpinner;
	private CheckBox seasonCheckBox;
	
	/* Variables */
	private List<VideoItem> videoItems;
	private List<VideoItemViewBinder> videoItemViews;
	
	private VideoItemViewBinder lastHolder;
	
	/* Constructor */
	public VideoActivity() {
		super();
		
		this.videoManager = managers.getVideoManager();
		
		this.videoItems = new ArrayList<>();
		this.videoItemViews = new ArrayList<>();
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
		
		changeSeason(videoGroup.getSeasons().get(0));
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
			case Constants.REQUEST_ID.REQUEST_ID_VLC_VIDEO: {
				if (data == null || data.getExtras() == null) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_activity_vlc_error, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				long position = data.getExtras().getLong(BUNDLE_KEY_VLC_EXTRA_POSITION);
				long duration = data.getExtras().getLong(BUNDLE_KEY_VLC_EXTRA_DURATION);
				
				boolean extraPositionValid = !DATEFORMAT_VIDEO_DURATION.format(new Date(position)).equals("23:59:59");
				boolean extraDurationValid = !DATEFORMAT_VIDEO_DURATION.format(new Date(duration)).equals("00:00:00");
				
				if (!extraPositionValid || !extraDurationValid) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_activity_invalid_time, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				VideoFile video = videoManager.getLastVideoFileOpen();
				
				if (video == null) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_error_video_file_forget, Snackbar.LENGTH_LONG).show();
					return;
				}
				
				video.newSavedTime(position);
				video.newDuration(duration);
				
				if (lastHolder != null && lastHolder.bindVideoItem.videoFile == video) {
					lastHolder.updateVideoFileItemInformations(video, false);
					
					lastHolder.getBindVideoItem().setExpanded(true);
					lastHolder.getExpandableLayout().setExpanded(true);
				}
				
				if (videoGroup.isMovie()) {
					lastHolder = videoItemViews.get(0);
					lastHolder.updateVideoFileItemInformations(video, false);
				}
				
				videoManager.callConfigurator(video);
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
	}
	
	/**
	 * Initialize views
	 */
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
		
		videosLinearLayout = (LinearLayout) findViewById(R.id.activity_video_linearlayout_videos);
		
		/* Code */
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
				
				videoManager.callConfigurator(videoGroup);
			}
		});
		floatingActionButton.setImageResource(videoGroup.isWatching() ? R.drawable.icon_eye_close_96px : R.drawable.icon_eye_open_96px);
		
		imageHelper.download(videoImageView, videoGroup.getGroupImageUrl()).validate();
		
		seasonCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean checked = seasonCheckBox.isChecked();
				
				selectedVideoSeason.asWatched(checked);
				
				videoManager.callConfigurator(selectedVideoSeason);
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
						changeSeason(videoGroup.getSeasons().get(position));
					}
				}
			});
		} else {
			seasonTextView.setText(videoGroup.getTitle());
		}
		
		SeasonSpinnerAdapter seasonSpinnerAdapter = new SeasonSpinnerAdapter(this, videoGroup.getSeasons());
		seasonSpinner.setAdapter(seasonSpinnerAdapter);
	}
	
	/**
	 * Only change season with this function, if you change the {@link #selectedVideoSeason} by yourself, the list update will not be done.
	 */
	private void changeSeason(VideoSeason videoSeason) {
		if (videoSeason != this.selectedVideoSeason) {
			this.selectedVideoSeason = videoSeason;
			
			seasonTextView.setText(getString(R.string.boxplay_store_video_season_selector_result, selectedVideoSeason.getTitle()));
			seasonCheckBox.setChecked(videoSeason.isWatched());
			
			videosLinearLayout.removeAllViews();
			videoItems.clear();
			for (VideoFile video : videoSeason.getVideos()) {
				videoItems.add(new VideoItem(video));
			}
			
			createNextVideoView(new AsyncLayoutInflater(this), new ArrayList<>(videoItems).iterator(), videoSeason, 0);
			
			String imageUrl = videoSeason.getImageHdUrl();
			if (imageUrl == null) {
				imageUrl = videoSeason.getImageUrl();
			}
			
			if (imageUrl != null) {
				imageHelper.download(videoImageView, imageUrl).validate();
			}
			final String passImageUrl = imageUrl;
			
			videoImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ImageViewerActivity.start(passImageUrl);
				}
			});
			
			appBarLayout.setExpanded(true, true);
			nestedScrollView.getParent().requestChildFocus(nestedScrollView, nestedScrollView);
		}
	}
	
	/**
	 * "Recursive" function to create as much view as needed one by one added to the main {@link #videosLinearLayout}.<br>
	 * This function will not continue if the season has been changed while views are populatings.<br>
	 * All of the information update is done by the {@link VideoItemViewBinder}.<br>
	 * 
	 * @param asyncLayoutInflater
	 *            An {@link AsyncLayoutInflater} layout instance that will be constantly used
	 * @param videoIterator
	 *            VideoItem's list iterator to go over all instances
	 * @param sourceSeason
	 *            Season selected when the populating begin, will stop if this instance if not the same as {@link #selectedVideoSeason}
	 * @param position
	 *            Incremented position (+1 every "recursive" call)
	 */
	private void createNextVideoView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<VideoItem> videoIterator, final VideoSeason sourceSeason, final int position) {
		if (videoIterator.hasNext()) {
			asyncLayoutInflater.inflate(R.layout.item_video_layout, videosLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (sourceSeason == selectedVideoSeason) {
						new VideoItemViewBinder(view, position).bind(videoIterator.next());
						
						parent.addView(view);
						
						createNextVideoView(asyncLayoutInflater, videoIterator, sourceSeason, position + 1);
					}
				}
			});
		}
	}
	
	/**
	 * Simple {@link ArrayAdapter} to be used with a {@link Spinner}.<br>
	 * This implementations will handle all work do display season and ask a change if the user want it.
	 * 
	 * @author Enzo CACERES
	 */
	class SeasonSpinnerAdapter extends ArrayAdapter<VideoSeason> {
		
		/* Constructor */
		public SeasonSpinnerAdapter(Context context, List<VideoSeason> objects) {
			super(context, android.R.layout.simple_spinner_item, objects);
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			return createTextView(position, true, true);
		};
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return createTextView(position, true, false);
		}
		
		/**
		 * Create a {@link TextView} that can display a {@link VideoSeason} by its position
		 * 
		 * @param position
		 *            Season position
		 * @param useColor
		 *            If you want to use text color
		 * @param addDropdownArrow
		 *            If you want to add a dropdown arrow near the text
		 * @return A {@link TextView} in a {@link LinearLayout} for better padding uses
		 */
		private LinearLayout createTextView(int position, boolean useColor, boolean addDropdownArrow) {
			TextView textView = new TextView(getContext());
			textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
			TypedArray typedArray = getContext().obtainStyledAttributes(new int[] { R.attr.selectableItemBackground });
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
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_arrow_drop_down_black_24dp, 0, 0, 0);
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
			
			textView.setTextColor(getResources().getColor(targetColorId));
			
			if (Build.VERSION.SDK_INT >= 23) {
				textView.setCompoundDrawableTintList(getResources().getColorStateList(targetColorId));
			}
			
			LinearLayout linearLayout = new LinearLayout(getContext());
			linearLayout.addView(textView);
			linearLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
			return linearLayout;
		}
	}
	
	/**
	 * {@link VideoItem} handler to update information in a generated {@link View}
	 * 
	 * @author Enzo CACERES
	 */
	class VideoItemViewBinder {
		
		/* Views */
		public ExpandableLayout expandableLayout;
		public View relativeLayout;
		public TextView episodeTextView, timeTextView, languageTextView;
		public SeekBar progressSeekBar;
		public Button playButton, downloadButton, watchButton, shareButton, shareUrlButton;
		
		/* Information holder */
		public VideoItem bindVideoItem;
		
		/* Variables */
		public int position;
		
		/* Listeners */
		private View.OnClickListener viewOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				VideoItemViewBinder holder = (VideoItemViewBinder) view.getTag();
				
				if (videoGroup.isMovie()) {
					holder.getExpandableLayout().setExpanded(true, false);
					holder.getBindVideoItem().setExpanded(true);
				} else {
					resetExpendableLayout();
					
					holder.getExpandableLayout().toggleExpansion();
				}
				
				lastHolder = holder;
			}
		};
		
		/* Constructor */
		public VideoItemViewBinder(View view, int position) {
			videoItemViews.add(this);
			
			this.position = position;
			
			/* Parent */
			this.expandableLayout = (ExpandableLayout) view.findViewById(R.id.item_video_layout_expandablelayout_item_container);
			this.relativeLayout = (View) view.findViewById(R.id.item_video_layout_relativelayout_parent_container);
			this.episodeTextView = (TextView) view.findViewById(R.id.item_video_layout_textview_episode_title);
			this.progressSeekBar = (SeekBar) view.findViewById(R.id.item_video_layout_seekbar_saved_progress);
			this.timeTextView = (TextView) view.findViewById(R.id.item_video_layout_textview_saved_time);
			this.languageTextView = (TextView) view.findViewById(R.id.item_video_layout_textview_language);
			
			/* Child */
			this.playButton = (Button) view.findViewById(R.id.item_video_layout_item_button_play);
			this.downloadButton = (Button) view.findViewById(R.id.item_video_layout_item_button_download);
			this.watchButton = (Button) view.findViewById(R.id.item_video_layout_item_button_watch);
			this.shareButton = (Button) view.findViewById(R.id.item_video_layout_item_button_share);
			this.shareUrlButton = (Button) view.findViewById(R.id.item_video_layout_item_button_share_url);
		}
		
		/**
		 * Bind a {@link VideoItem} to this {@link View}
		 * 
		 * @param item
		 *            Target item
		 */
		public void bind(VideoItem item) {
			bindVideoItem = item;
			final VideoFile video = item.videoFile;
			
			/* Parent */
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
			
			expandableLayout.setExpanded(item.isExpanded(), false);
			episodeTextView.setText(getString(R.string.boxplay_store_video_activity_episode_title, cacheHelper.translate(item.videoFile.getVideoType()), item.videoFile.getRawEpisodeValue()));
			languageTextView.setText(getString(R.string.boxplay_store_video_activity_episode_language, cacheHelper.translate(item.videoFile.getLanguage())));
			progressSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
			
			updateVideoFileItemInformations(video);
			
			/* Child */
			playButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					managers.getVideoManager().openVLC(video);
				}
			});
			
			downloadButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// AndroidDownloader.askDownload(BoxPlayApplication.getBoxPlayApplication(), Uri.parse(video.getUrl()));
					AdmAndroidDownloader.askDownload(boxPlayApplication, video.getUrl(), applicationHelper.isAdmEnabled());
				}
			});
			
			watchButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					boolean isNotWatched = !video.isWatched();
					
					video.asWatched(isNotWatched);
					
					updateVideoFileItemInformations(video);
					
					videoManager.callConfigurator(video);
				}
			});
			
			shareButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// IntentUtils.shareText(BoxPlayApplication.getBoxPlayApplication(), parsedTitle + "\n\nLink: " + parsedUrl, parsedTitle); TODO
					boxPlayApplication.toast(getString(R.string.boxplay_error_not_implemented_yet)).show();
				}
			});
			
			shareUrlButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					CommonIntentUtils.shareText(BoxPlayApplication.getBoxPlayApplication(), video.getUrl(), video.getUrl());
				}
			});
			
			/* Movie */
			if (videoGroup.isMovie()) {
				episodeTextView.setText(getString(R.string.boxplay_store_video_activity_movie_title, cacheHelper.translate(item.videoFile.getFileType()), cacheHelper.translate(item.videoFile.getLanguage())));
				episodeTextView.setVisibility(View.GONE);
				languageTextView.setText(getString(R.string.boxplay_store_video_activity_episode_language, cacheHelper.translate(item.videoFile.getLanguage())));
				
				bindVideoItem.setExpanded(true);
				expandableLayout.setExpanded(true, false);
			}
		}
		
		/**
		 * Update the information, and don't show the {@link Snackbar} for auto-set-as-view if you watch more than 80%<br>
		 * See {@link #updateVideoFileItemInformations(VideoFile, boolean)} for more information.
		 * 
		 * @param video
		 *            Target {@link VideoFile} to update
		 */
		private void updateVideoFileItemInformations(VideoFile video) {
			updateVideoFileItemInformations(video, true);
		}
		
		/**
		 * Update this group of {@link View}s<br>
		 * This will update the language, actual time, and progress bar.<br>
		 * 
		 * @param video
		 *            Target {@link VideoFile} to update information from
		 * @param disableSnackbarConfirm
		 *            If you want to disable or not the creation and display of a {@link Snackbar} that will prompt the user if he want to set this episode as "watched".<br>
		 *            This bar will only show up if the actual watch time is more than 80% of the total time of the video.
		 */
		private void updateVideoFileItemInformations(final VideoFile video, boolean disableSnackbarConfirm) {
			/* Default action */
			progressSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
			
			/* Video unavailable */
			if (!video.isAvailable()) {
				progressSeekBar.setVisibility(View.INVISIBLE);
				languageTextView.setVisibility(View.INVISIBLE);
				timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_unavailable);
				
				setGlobalButtonEnabled(false);
				return;
			}
			
			/* Video available */
			progressSeekBar.setVisibility(View.VISIBLE);
			languageTextView.setVisibility(View.VISIBLE);
			timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_available);
			setGlobalButtonEnabled(true);
			
			/* Already watched */
			if (video.isWatched()) {
				progressSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
				progressSeekBar.setProgress(100);
				timeTextView.setText(R.string.boxplay_store_video_activity_episode_time_watched);
				watchButton.setText(R.string.boxplay_store_video_button_unwatch);
				
				return;
			}
			
			watchButton.setText(R.string.boxplay_store_video_button_watch);
			
			/* Not finished, and not saved progress (-1 or 0) */
			if (video.getSavedTime() < 1) {
				progressSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.MULTIPLY);
				
				return;
			}
			
			/* Not finished, but saved progress */
			progressSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
			progressSeekBar.setProgress((int) ((video.getSavedTime() * 100) / video.getDuration()));
			timeTextView.setText(getString(R.string.boxplay_store_video_activity_episode_time, DATEFORMAT_VIDEO_DURATION.format(new Date(video.getSavedTime())), DATEFORMAT_VIDEO_DURATION.format(new Date(video.getDuration()))));
			
			if (!disableSnackbarConfirm) {
				if (video.getSavedTime() > video.getDuration() * 0.80) {
					Snackbar.make(coordinatorLayout, R.string.boxplay_store_video_action_mark_as_watched, SNACKBAR_DURATION_DONE_WATCHING_PROMPT).setAction(R.string.boxplay_store_video_action_mark_as_watched_ok, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							video.asWatched(true);
							updateVideoFileItemInformations(video);
							videoManager.callConfigurator(video);
						}
					}).show();
				}
			}
		}
		
		/**
		 * Update every {@link Button} their enabled state.<br>
		 * The {@link #playButton} will only be able to change his state if VLC is installed.
		 * 
		 * @param enabled
		 *            New state.
		 */
		private void setGlobalButtonEnabled(boolean enabled) {
			playButton.setEnabled(applicationHelper.isVlcInstalled() ? enabled : false);
			downloadButton.setEnabled(enabled);
			watchButton.setEnabled(enabled);
			shareButton.setEnabled(enabled);
			shareUrlButton.setEnabled(enabled);
		}
		
		/**
		 * Call {@link ExpandableLayout#setExpanded(boolean, boolean)} with a false argument and the annimation enabled, to every {@link ExpandableLayout} in the list.
		 */
		public void resetExpendableLayout() {
			for (VideoItemViewBinder holder : videoItemViews) {
				if (holder.getExpandableLayout().isExpanded()) {
					holder.getExpandableLayout().setExpanded(false, true);
				}
				
				holder.getBindVideoItem().setExpanded(false);
			}
		}
		
		public VideoItem getBindVideoItem() {
			return bindVideoItem;
		}
		
		public ExpandableLayout getExpandableLayout() {
			return expandableLayout;
		}
	}
	
	/**
	 * Information holder class
	 * 
	 * @author Enzo CACERES
	 */
	class VideoItem {
		
		/* Variables */
		private final VideoFile videoFile;
		private boolean expanded;
		
		/* Constructor */
		public VideoItem(VideoFile videoFile) {
			this.videoFile = videoFile;
			this.expanded = false;
		}
		
		/**
		 * @return Attached {@link VideoFile}
		 */
		public VideoFile getVideoFile() {
			return videoFile;
		}
		
		/**
		 * @return The expanded state, not sync with an {@link ExpandableLayout}
		 */
		public boolean isExpanded() {
			return expanded;
		}
		
		/**
		 * Set expanded state, not sync with an {@link ExpandableLayout}
		 * 
		 * @param expanded
		 *            New state
		 */
		public void setExpanded(boolean expanded) {
			this.expanded = expanded;
		}
	}
	
	/**
	 * Quick start a {@link VideoActivity} with a source {@link VideoGroup}
	 * 
	 * @param videoGroup
	 *            Target {@link VideoGroup} to start with
	 */
	public static void start(VideoGroup videoGroup) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, VideoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_VIDEO_GROUP_ITEM, (Serializable) videoGroup);
		
		application.startActivity(intent);
	}
	
	/**
	 * @return {@link VideoActivity} instance if available
	 */
	public static VideoActivity getVideoActivity() {
		return (VideoActivity) INSTANCE;
	}
	
}