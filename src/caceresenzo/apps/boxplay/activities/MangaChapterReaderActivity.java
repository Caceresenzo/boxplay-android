package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.List;

import com.github.chrisbanes.photoview.HackyProblematicViewPager;
import com.github.chrisbanes.photoview.PhotoView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseViewPagerAdapter;
import caceresenzo.apps.boxplay.fragments.utils.ViewFragment;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.libs.boxplay.common.extractor.image.manga.MangaChapterContentExtractor;
import caceresenzo.libs.boxplay.culture.searchngo.content.image.implementations.IMangaContentProvider;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.ChapterItemResultData;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.HelpedThread;

/**
 * Activity to read Manga
 * 
 * The bundle need {@link #BUNDLE_KEY_CHAPTER_ITEM} ({@value #BUNDLE_KEY_CHAPTER_ITEM}) as a {@link ChapterItemResultData} to be started
 * 
 * @author Enzo CACERES
 */
public class MangaChapterReaderActivity extends BaseBoxPlayActivty {
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_CHAPTER_ITEM = "chapter_item";
	public static final String BUNDLE_KEY_ACTUAL_PAGE = "actual_page";
	
	/* Offset values */
	public static final int OFFSET_NEXT_PAGE = 1;
	public static final int OFFSET_PREVIOUS_PAGE = -1;
	
	/* Instance */
	private static MangaChapterReaderActivity INSTANCE;
	
	/* Actual chapter item */
	private ChapterItemResultData chapterItem;
	
	/* Manager(s) */
	private SearchAndGoManager searchAndGoManager;
	
	/* Views */
	private SlidingUpPanelLayout slidingUpPanelLayout;
	
	private ViewPager mangaViewPager;
	private BaseViewPagerAdapter pagerAdapter;
	
	private TextView infoTextView;
	
	private TextView errorTextView;
	private ProgressBar loadingProgressBar;
	
	/* Worker */
	private ExtractionWorker extractionWorker;
	
	/* Local data */
	private String chapterName;
	private List<String> imageUrls;
	private int chapterSize, actualPage = NO_VALUE;
	
	/* Constructor */
	public MangaChapterReaderActivity() {
		super();
		
		this.searchAndGoManager = BoxPlayApplication.getManagers().getSearchAndGoManager();
		
		this.extractionWorker = new ExtractionWorker();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_manga_chapter_reader);
		
		chapterItem = (ChapterItemResultData) getIntent().getSerializableExtra(BUNDLE_KEY_CHAPTER_ITEM);
		boolean validData = false;
		if (chapterItem == null || !(chapterItem.getImageContentProvider() instanceof IMangaContentProvider)) {
			if (boxPlayApplication != null) {
				boxPlayApplication.toast(getString(R.string.boxplay_error_activity_invalid_data)).show();
			}
			finish();
		}
		
		if (savedInstanceState != null) {
			actualPage = savedInstanceState.getInt(BUNDLE_KEY_ACTUAL_PAGE, NO_VALUE);
		}
		
		initializeViews();
		
		initializeManga(validData);
		
		// ready();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_CHAPTER_ITEM, (Serializable) chapterItem);
		outState.putInt(BUNDLE_KEY_ACTUAL_PAGE, actualPage);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
		
		extractionWorker.cancel();
	}
	
	/* Initialization -> Views */
	private void initializeViews() {
		slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.activity_manga_chapter_reader_slidinglayout_container);
		
		mangaViewPager = (HackyProblematicViewPager) findViewById(R.id.activity_manga_chapter_reader_viewpager_container);
		mangaViewPager.setAdapter(pagerAdapter = new BaseViewPagerAdapter(getSupportFragmentManager()));
		
		mangaViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				updateSelectedPage(position + 1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				;
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				;
			}
		});
		
		infoTextView = (TextView) findViewById(R.id.activity_manga_chapter_reader_textview_info);
		
		errorTextView = (TextView) findViewById(R.id.activity_manga_chapter_reader_textview_error);
		loadingProgressBar = (ProgressBar) findViewById(R.id.activity_manga_chapter_reader_progressbar_loading);
	}
	
	/* Initialization -> Manga */
	private void initializeManga(boolean validRestoredData) {
		if (validRestoredData) {
			reloadImages();
		} else {
			this.chapterName = chapterItem.getName();
			
			if (extractionWorker.isRunning()) {
				boxPlayApplication.toast("ExtractionWorker is busy").show();
				return;
			}
			
			setViewerHidden(true);
			
			extractionWorker.applyData(chapterItem).start();
		}
	}
	
	/**
	 * Call this function when image need to be reloaded, like when the activity is being restored
	 */
	public void reloadImages() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				showPages(imageUrls);
			}
		}, 100L);
	}
	
	/**
	 * Fill the {@link ViewPager} with some imageUrls
	 * 
	 * @param imageUrls
	 */
	private void showPages(List<String> imageUrls) {
		this.imageUrls = imageUrls;
		this.chapterSize = imageUrls.size();
		
		mangaViewPager.setAdapter(pagerAdapter = new BaseViewPagerAdapter(getSupportFragmentManager()));
		
		for (String imageUrl : imageUrls) {
			PhotoView imageView = new PhotoView(this);
			
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					setPageByOffset(OFFSET_NEXT_PAGE);
				}
			});
			
			pagerAdapter.addFragment(new ViewFragment(imageView, false), "");
			
			BoxPlayApplication.getViewHelper().downloadToImageView(this, imageView, imageUrl);
			
			pagerAdapter.notifyDataSetChanged(); // Need to be called everytime
		}
		
		mangaViewPager.setOffscreenPageLimit(chapterSize);
		pagerAdapter.notifyDataSetChanged(); // Just to be sure
		
		if (actualPage != NO_VALUE) {
			mangaViewPager.setCurrentItem(actualPage - 1);
		} else {
			updateSelectedPage(1);
		}
		
		setViewerHidden(false);
	}
	
	/**
	 * Hide or show the main viewer
	 * 
	 * If hidden, the loading bar will be visible, if show, loading bar will disapear
	 * 
	 * @param hidden
	 *            True or false
	 */
	private void setViewerHidden(boolean hidden) {
		slidingUpPanelLayout.setVisibility(hidden ? View.GONE : View.VISIBLE);
		
		loadingProgressBar.setVisibility(hidden ? View.VISIBLE : View.GONE);
		errorTextView.setVisibility(View.GONE);
	}
	
	/**
	 * Diaplay an error in the center of the viewer
	 * 
	 * @param exception
	 *            Occured exception
	 */
	private void displayError(final Exception exception) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				setViewerHidden(true);
				loadingProgressBar.setVisibility(View.GONE);
				
				errorTextView.setVisibility(View.VISIBLE);
				errorTextView.setText(getString(R.string.boxplay_manga_chapter_reader_format_error, exception.getLocalizedMessage(), StringUtils.fromException(exception)));
				
			}
		});
	}
	
	/**
	 * Go to a page by current offset (actual page + offset)
	 * 
	 * @param offset
	 *            Target offset
	 */
	private void setPageByOffset(int offset) {
		if (offset < 0) {
			return;
		}
		
		if (mangaViewPager.getCurrentItem() <= pagerAdapter.getCount()) {
			mangaViewPager.setCurrentItem(mangaViewPager.getCurrentItem() + 1);
		} else {
			// TODO: Notify user that chapter has ended
		}
		
		updateSelectedPage(mangaViewPager.getCurrentItem() + 1);
	}
	
	/**
	 * Update the selected page panel
	 * 
	 * @param selectedPage
	 *            Actual position + 1 to remove the offset
	 */
	private void updateSelectedPage(int selectedPage) {
		actualPage = selectedPage;
		infoTextView.setText(getString(R.string.boxplay_manga_chapter_reader_format_info, chapterName, actualPage, chapterSize));
	}
	
	/**
	 * Start a new MangaChapterReaderActivity
	 * 
	 * @param result
	 *            {@link ChapterItemResultData} item that you want to start with
	 */
	public static void start(ChapterItemResultData result) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, MangaChapterReaderActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_CHAPTER_ITEM, (Serializable) result);
		
		application.startActivity(intent);
	}
	
	/**
	 * Extraction thread linked to the UI to fetch data like image urls
	 * 
	 * @author Enzo CACERES
	 */
	class ExtractionWorker extends HelpedThread {
		/* Parent Activity set when creating new Instance */
		private final MangaChapterReaderActivity parentActivity;
		/* Actual result to fetch */
		private ChapterItemResultData result;
		
		/* Result's parent class */
		private IMangaContentProvider mangaContentProvider;
		private MangaChapterContentExtractor chapterContentExtractor;
		
		/* Local list of imageUrls, not initialized */
		private List<String> imageUrls;
		
		/* Constructor */
		public ExtractionWorker() {
			this.parentActivity = INSTANCE;
		}
		
		@Override
		protected void onRun() {
			try {
				imageUrls = chapterContentExtractor.getImageUrls(mangaContentProvider.extractMangaPageUrl(result));
			} catch (Exception exception) {
				displayError(exception);
			}
			
			if (imageUrls == null) {
				cancel();
			}
		}
		
		@Override
		protected void onFinished() {
			if (parentActivity != INSTANCE) {
				return;
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					showPages(imageUrls);
				}
			});
			
		}
		
		@Override
		protected void onCancelled() {
			;
		}
		
		/**
		 * Apply data to the thread, and call {@link #start()} after this function
		 * 
		 * @param result
		 *            Target result
		 * @return Itself
		 */
		public ExtractionWorker applyData(ChapterItemResultData result) {
			if (result == null) {
				return this;
			}
			
			this.result = result;
			
			this.mangaContentProvider = (IMangaContentProvider) result.getImageContentProvider();
			this.chapterContentExtractor = (MangaChapterContentExtractor) searchAndGoManager.getExtractorFromBaseUrl(result.getUrl());
			
			return this;
		}
		
	}
	
}