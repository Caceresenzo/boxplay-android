package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.store.video.BaseVideoStoreElement;
import caceresenzo.libs.boxplay.store.video.implementations.SeriesVideoStoreElement;
import caceresenzo.libs.boxplay.store.video.implementations.series.SeriesSeasonVideoStoreElement;

public class VideoActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = VideoActivity.class.getSimpleName();
	
	/* Constants */
	public static final String BUNDLE_KEY_VIDEO_ELEMENT_ITEM = "video_element";
	
	public static final String BUNDLE_KEY_VLC_EXTRA_POSITION = "extra_position";
	public static final String BUNDLE_KEY_VLC_EXTRA_DURATION = "extra_duration";
	
	/* Static Instance */
	private static VideoActivity INSTANCE;
	
	/* Views */
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
	
	/* Variables */
	private BaseVideoStoreElement videoElement;
	private SeriesSeasonVideoStoreElement selectedSeasonElement;
	
	/* Constructor */
	public VideoActivity() {
		super();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		INSTANCE = this;
		
		videoElement = (BaseVideoStoreElement) getIntent().getSerializableExtra(BUNDLE_KEY_VIDEO_ELEMENT_ITEM);
		if (videoElement == null) {
			boxPlayApplication.toast(getString(R.string.boxplay_error_activity_invalid_data)).show();
			finish();
		}
		
		initializeViews();
		
		if (videoElement instanceof SeriesVideoStoreElement) {
			// changeSeason(selectedSeasonElement = ((SeriesVideoStoreElement) videoElement).getSeasons().get(0));
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_VIDEO_ELEMENT_ITEM, (Serializable) videoElement);
	}
	
	private void initializeViews() {
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public static void start(BaseVideoStoreElement videoElement) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, VideoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_VIDEO_ELEMENT_ITEM, (Serializable) videoElement);
		
		application.startActivity(intent);
	}
	
	public static VideoActivity getVideoActivity() {
		return (VideoActivity) INSTANCE;
	}
	
}