package caceresenzo.apps.boxplay.activities;

import com.github.chrisbanes.photoview.PhotoView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.string.StringUtils;

public class ImageViewerActivity extends BaseBoxPlayActivty {
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_IMAGE_URL = "image_url";
	
	/* Views */
	private PhotoView viewerPhotoView;
	
	/* Variables */
	private String imageUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_image_viewer);
		
		if (getIntent() == null) {
			finish();
			return;
		}
		
		imageUrl = getIntent().getStringExtra(BUNDLE_KEY_IMAGE_URL);
		
		if (!StringUtils.validate(imageUrl)) {
			finish();
			return;
		}
		
		initializeViews();
	}
	
	/* Initialization -> Views */
	private void initializeViews() {
		viewerPhotoView = (PhotoView) findViewById(R.id.activity_image_viewer_photoview_viewer);
		
		viewHelper.downloadToImageView(viewerPhotoView, imageUrl);
	}
	
	/**
	 * Start a new ImageViewerActivity
	 * 
	 * @param imageUrl
	 *            Image's url you want to view
	 */
	public static void start(String imageUrl) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, ImageViewerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_IMAGE_URL, imageUrl);
		
		application.startActivity(intent);
	}
	
}