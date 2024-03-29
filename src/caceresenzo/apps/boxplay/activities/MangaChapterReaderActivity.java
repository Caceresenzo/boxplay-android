package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.github.chrisbanes.photoview.HackyProblematicViewPager;
import com.github.chrisbanes.photoview.PhotoView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseViewPagerAdapter;
import caceresenzo.apps.boxplay.fragments.utils.ViewFragment;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager.ExtractorType;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.image.manga.MangaChapterContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.text.TextContentExtractor.TextFormat;
import caceresenzo.libs.boxplay.common.extractor.text.novel.NovelChapterContentExtractor;
import caceresenzo.libs.boxplay.culture.searchngo.content.image.implementations.IMangaContentProvider;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.SimpleData;
import caceresenzo.libs.boxplay.culture.searchngo.data.models.content.ChapterItemResultData;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

/**
 * Activity to read Manga.<br>
 * The bundle need {@link #BUNDLE_KEY_CHAPTER_ITEM} ({@value #BUNDLE_KEY_CHAPTER_ITEM}) as a {@link ChapterItemResultData} to be started.
 * 
 * @author Enzo CACERES
 */
public class MangaChapterReaderActivity extends BaseBoxPlayActivty {
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_CONTENT_TYPE = "content_type";
	public static final String BUNDLE_KEY_CHAPTER_ITEM = "chapter_item";
	public static final String BUNDLE_KEY_ACTUAL_PAGE = "actual_page";
	
	/* Offset values */
	public static final int OFFSET_NEXT_PAGE = +1;
	public static final int OFFSET_PREVIOUS_PAGE = -1;
	
	/* Content Type */
	public static final int CONTENT_TYPE_UNKNOWN = -1;
	public static final int CONTENT_TYPE_IMAGE_ARRAY = 0;
	public static final int CONTENT_TYPE_TEXT = 1;
	
	/* Instance */
	private static MangaChapterReaderActivity INSTANCE;
	
	/* Actual chapter item */
	private ChapterItemResultData chapterItem;
	
	/* Views */
	private SlidingUpPanelLayout slidingUpPanelLayout;
	
	private ViewPager mangaViewPager;
	private BaseViewPagerAdapter pagerAdapter;
	
	private ImageButton previousControlImageButton, nextControlImageButton;
	private TextView infoTextView;
	private Button comingSoonControlButton, reloadActualPageControlButton;
	
	private TextView errorTextView;
	private ProgressBar loadingProgressBar;
	
	/* Worker */
	private MangaExtractionWorker mangaExtractionWorker;
	private NovelExtractionWorker novelExtractionWorker;
	
	/* Local data */
	private String chapterName;
	private List<String> imageUrls;
	private int contentType, chapterSize, actualPage = NO_VALUE;
	
	/* Constructor */
	public MangaChapterReaderActivity() {
		super();
		
		this.mangaExtractionWorker = new MangaExtractionWorker();
		this.novelExtractionWorker = new NovelExtractionWorker();
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
		initializeControls();
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
		
		mangaExtractionWorker.shouldStop();
		novelExtractionWorker.shouldStop();
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
		
		previousControlImageButton = (ImageButton) findViewById(R.id.activity_manga_chapter_reader_imagebutton_control_previous);
		infoTextView = (TextView) findViewById(R.id.activity_manga_chapter_reader_textview_info);
		nextControlImageButton = (ImageButton) findViewById(R.id.activity_manga_chapter_reader_button_control_next);
		
		comingSoonControlButton = (Button) findViewById(R.id.activity_manga_chapter_reader_button_control_coming_soon);
		reloadActualPageControlButton = (Button) findViewById(R.id.activity_manga_chapter_reader_button_control_reload_actual_page);
		
		previousControlImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setPageByOffset(OFFSET_PREVIOUS_PAGE);
			}
		});
		
		nextControlImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setPageByOffset(OFFSET_NEXT_PAGE);
			}
		});
		
		reloadActualPageControlButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				reloadActualPage();
			}
		});
		
		errorTextView = (TextView) findViewById(R.id.activity_manga_chapter_reader_textview_error);
		loadingProgressBar = (ProgressBar) findViewById(R.id.activity_manga_chapter_reader_progressbar_loading);
	}
	
	/* Initialization -> Manga */
	private void initializeManga(boolean validRestoredData) {
		this.chapterName = chapterItem.convertToDisplayableString();
		
		switch (chapterItem.getChapterType()) {
			case IMAGE_ARRAY: {
				contentType = CONTENT_TYPE_IMAGE_ARRAY;
				
				if (validRestoredData) {
					reloadImages();
				} else {
					if (mangaExtractionWorker.isRunning()) {
						boxPlayApplication.toast("ExtractionWorker is busy").show();
						return;
					}
					
					setViewerHidden(true);
					
					mangaExtractionWorker.applyData(chapterItem).start();
				}
				break;
			}
			
			case TEXT: {
				contentType = CONTENT_TYPE_TEXT;
				
				if (novelExtractionWorker.isRunning()) {
					boxPlayApplication.toast("ExtractionWorker is busy").show();
					return;
				}
				
				setViewerHidden(true);
				
				novelExtractionWorker.applyData(chapterItem).start();
				break;
			}
			
			default: {
				contentType = CONTENT_TYPE_UNKNOWN;
				
				displayError(new IllegalStateException("Unhandled chapter type: " + chapterItem.getChapterType()));
				break;
			}
		}
	}
	
	/* Initialization -> Controls */
	private void initializeControls() {
		switch (contentType) {
			case CONTENT_TYPE_IMAGE_ARRAY: {
				comingSoonControlButton.setVisibility(View.GONE);
				reloadActualPageControlButton.setVisibility(View.VISIBLE);
				break;
			}
			
			case CONTENT_TYPE_UNKNOWN:
			case CONTENT_TYPE_TEXT:
			default: {
				comingSoonControlButton.setVisibility(View.VISIBLE);
				reloadActualPageControlButton.setVisibility(View.GONE);
				break;
			}
		}
	}
	
	/**
	 * Call this function when image need to be reloaded, like when the activity is being restored.
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
	 * Fill the {@link ViewPager} with some image urls.
	 * 
	 * @param imageUrls
	 *            Target image urls.
	 */
	private void showPages(List<String> imageUrls) {
		this.imageUrls = imageUrls;
		this.chapterSize = imageUrls.size();
		
		mangaViewPager.setAdapter(pagerAdapter = new BaseViewPagerAdapter(getSupportFragmentManager()));
		
		Map<String, Object> requireHttpHeaders = getRequireHttpHeaders();
		
		for (String imageUrl : imageUrls) {
			PhotoView imageView = createImageView();
			
			pagerAdapter.addFragment(new ViewFragment(imageView, false), "");
			
			imageHelper.download(imageView, imageUrl).headers(requireHttpHeaders).validate();
			
			pagerAdapter.notifyDataSetChanged(); /* Need to be called everytime */
		}
		
		mangaViewPager.setOffscreenPageLimit(chapterSize);
		pagerAdapter.notifyDataSetChanged(); /* Just to be sure */
		
		if (actualPage != NO_VALUE) {
			mangaViewPager.setCurrentItem(actualPage - 1);
		} else {
			updateSelectedPage(1);
		}
		
		setViewerHidden(false);
	}
	
	/**
	 * @return An {@link ImageView} with a {@link View.OnClickListener} attached to it.
	 */
	private PhotoView createImageView() {
		PhotoView imageView = new PhotoView(this);
		
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setPageByOffset(OFFSET_NEXT_PAGE);
			}
		});
		
		return imageView;
	}
	
	/**
	 * Fill the {@link ViewPager} with a text.
	 * 
	 * @param spannedText
	 *            {@link Spanned} version of the text for the novel.
	 */
	private void showNovelPage(Spanned spannedText) {
		this.imageUrls = null;
		this.chapterSize = 1;
		
		for (ImageButton imageButton : new ImageButton[] { previousControlImageButton, nextControlImageButton }) {
			imageButton.setVisibility(View.GONE);
		}
		
		mangaViewPager.setAdapter(pagerAdapter = new BaseViewPagerAdapter(getSupportFragmentManager()));
		
		TextView textView = new TextView(this);
		textView.setText(spannedText);
		
		pagerAdapter.addFragment(new ViewFragment(textView), "");
		
		pagerAdapter.notifyDataSetChanged();
		updateSelectedPage(1);
		
		setViewerHidden(false);
	}
	
	/**
	 * Reload the actually selected page.<br>
	 * It means re-call the download function on the {@link ImageView}.
	 */
	public void reloadActualPage() {
		switch (contentType) {
			case CONTENT_TYPE_IMAGE_ARRAY: {
				int actualPageIndex = actualPage - 1;
				
				ViewFragment pageFragment = (ViewFragment) pagerAdapter.getItem(actualPageIndex);
				PhotoView imageView = (PhotoView) pageFragment.getTargetView();
				
				Map<String, Object> requireHttpHeaders = getRequireHttpHeaders();
				
				imageHelper.download(imageView, imageUrls.get(actualPageIndex)).headers(requireHttpHeaders).validate();
				break;
			}
			
			default: {
				throw new IllegalStateException("Page reloading not supported for this content type.");
			}
		}
	}
	
	/**
	 * Get the http headers needed to download the image correctly, return false if it dosen't.
	 * 
	 * @return <code>chapterItem.getComplement(SimpleData.REQUIRE_HTTP_HEADERS_COMPLEMENT);</code>
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getRequireHttpHeaders() {
		if (chapterItem.hasInitializedComplements()) {
			return (Map<String, Object>) chapterItem.getComplement(SimpleData.REQUIRE_HTTP_HEADERS_COMPLEMENT);
		}
		
		return null;
	}
	
	/**
	 * Hide or show the main viewer.<br>
	 * If hidden, the loading bar will be visible, if show, loading bar will disapear.
	 * 
	 * @param hidden
	 *            New hidden state.
	 */
	private void setViewerHidden(boolean hidden) {
		slidingUpPanelLayout.setVisibility(hidden ? View.GONE : View.VISIBLE);
		
		loadingProgressBar.setVisibility(hidden ? View.VISIBLE : View.GONE);
		errorTextView.setVisibility(View.GONE);
	}
	
	/**
	 * Diaplay an error in the center of the viewer.
	 * 
	 * @param exception
	 *            Occured exception.
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
	 * Go to a page by current offset (actual page + offset).
	 * 
	 * @param offset
	 *            Target offset.
	 */
	private void setPageByOffset(int offset) {
		if (mangaViewPager.getCurrentItem() <= pagerAdapter.getCount()) {
			mangaViewPager.setCurrentItem(mangaViewPager.getCurrentItem() + offset);
		} else {
			// TODO: Notify user that chapter has ended
		}
		
		updateSelectedPage(mangaViewPager.getCurrentItem() + 1);
	}
	
	/**
	 * Update the selected page panel.
	 * 
	 * @param selectedPage
	 *            Actual position + 1 to remove the offset.
	 */
	private void updateSelectedPage(int selectedPage) {
		actualPage = selectedPage;
		infoTextView.setText(getString(R.string.boxplay_manga_chapter_reader_format_info, chapterName, actualPage, chapterSize));
		
		previousControlImageButton.setVisibility(selectedPage > 1 ? View.VISIBLE : View.INVISIBLE);
		nextControlImageButton.setVisibility(selectedPage < chapterSize ? View.VISIBLE : View.INVISIBLE);
	}
	
	/**
	 * Start a new {@link MangaChapterReaderActivity}.
	 * 
	 * @param result
	 *            {@link ChapterItemResultData} item that you want to start with.
	 */
	public static void start(ChapterItemResultData result) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, MangaChapterReaderActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_CHAPTER_ITEM, (Serializable) result);
		
		application.startActivity(intent);
	}
	
	/**
	 * Extraction thread linked to the UI to fetch data like image urls.
	 * 
	 * @author Enzo CACERES
	 */
	class MangaExtractionWorker extends ExtractionWorker<MangaChapterContentExtractor> {
		
		/* Local list of imageUrls, not initialized */
		private List<String> imageUrls;
		
		@Override
		protected void work() {
			imageUrls = chapterContentExtractor.getImageUrls(mangaContentProvider.extractMangaPageUrl(localChapterItem));
			
			if (imageUrls == null) {
				terminate();
			}
		}
		
		@Override
		protected void finish() {
			showPages(imageUrls);
		}
		
		@Override
		public ExtractorType getExtractionType() {
			return ExtractorType.MANGA;
		}
	}
	
	/**
	 * Extraction thread linked to the UI to fetch data like novels texts
	 * 
	 * @author Enzo CACERES
	 */
	class NovelExtractionWorker extends ExtractionWorker<NovelChapterContentExtractor> {
		
		/* Text Format */
		private TextFormat extractedNovelTextFormat;
		
		/* Content */
		private String novel;
		
		@Override
		protected void work() {
			novel = chapterContentExtractor.extractNovel(localChapterItem);
			
			if (!StringUtils.validate(novel)) {
				terminate();
			}
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected void finish() {
			switch (extractedNovelTextFormat) {
				case HTML: {
					showNovelPage(Html.fromHtml(novel));
					break;
				}
				
				default: {
					displayError(new IllegalStateException("Unhandled text format: " + extractedNovelTextFormat));
					break;
				}
			}
		}
		
		@Override
		public void onAppliedData(ChapterItemResultData result) {
			this.extractedNovelTextFormat = chapterContentExtractor.getSupposedExtractedTextFormat();
		}
		
		@Override
		public ExtractorType getExtractionType() {
			return ExtractorType.NOVEL;
		}
	}
	
	abstract class ExtractionWorker<E extends ContentExtractor> extends WorkerThread {
		
		/* Parent Activity set when creating new Instance */
		protected final MangaChapterReaderActivity parentActivity;
		/* Actual result to fetch */
		protected ChapterItemResultData localChapterItem;
		
		/* Result's parent class */
		protected IMangaContentProvider mangaContentProvider;
		protected E chapterContentExtractor;
		
		/* Constructor */
		public ExtractionWorker() {
			this.parentActivity = INSTANCE;
		}
		
		/**
		 * Abstract function, called when the thread just start.
		 */
		protected abstract void work();
		
		@Override
		protected void execute() {
			try {
				work();
			} catch (Exception exception) {
				displayError(exception);
				terminate();
			}
		}
		
		/**
		 * Abstract function, called when the thread has finished working.<br>
		 * He will be call on the main UI thread.
		 */
		protected abstract void finish();
		
		@Override
		protected void done() {
			if (parentActivity != INSTANCE) {
				return;
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					finish();
				}
			});
		}
		
		/**
		 * Apply data to the thread, and call {@link #start()} after this function.
		 * 
		 * @param result
		 *            Target result.
		 * @return Itself.
		 */
		@SuppressWarnings("unchecked")
		public ExtractionWorker<E> applyData(ChapterItemResultData result) {
			if (result == null) {
				return this;
			}
			
			this.localChapterItem = result;
			
			this.mangaContentProvider = (IMangaContentProvider) result.getImageContentProvider();
			this.chapterContentExtractor = (E) ContentExtractionManager.getExtractorFromBaseUrl(getExtractionType(), result.getUrl());
			
			onAppliedData(result);
			
			return this;
		}
		
		/**
		 * Function to override if necessary, called when {@link #applyData(ChapterItemResultData)} has been called.
		 */
		public void onAppliedData(ChapterItemResultData result) {
			;
		}
		
		public abstract ExtractorType getExtractionType();
	}
	
}