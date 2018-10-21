package caceresenzo.apps.boxplay.managers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import android.util.Log;
import android.view.MenuItem;
import caceresenzo.android.libs.dialog.DialogUtils;
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.R.string;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.managers.XManagers.SubManager;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager;
import caceresenzo.libs.boxplay.common.extractor.ContentExtractionManager.ExtractorType;
import caceresenzo.libs.boxplay.common.extractor.video.VideoContentExtractor;
import caceresenzo.libs.boxplay.common.extractor.video.VideoContentExtractor.VideoContentExtractorProgressCallback;
import caceresenzo.libs.boxplay.factory.AdultFactory;
import caceresenzo.libs.boxplay.factory.AdultFactory.AdultFactoryListener;
import caceresenzo.libs.boxplay.factory.AdultFactory.VideoOrigin;
import caceresenzo.libs.boxplay.models.premium.adult.AdultVideo;
import caceresenzo.libs.licencekey.LicenceKey;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.AbstractWorkerThread;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class PremiumManager extends AbstractManager {
	
	/* Tag */
	private static final String TAG = PremiumManager.class.getSimpleName();
	
	/* Menu Ids List */
	private static final List<Integer> premiumMenusId = new ArrayList<Integer>();
	
	static {
		premiumMenusId.add(R.id.drawer_boxplay_premium);
		premiumMenusId.add(R.id.drawer_boxplay_premium_adult);
	}
	
	/* Validation */
	private LicenceKey licenceKey;
	
	/* Sub Managers */
	private AdultPremiumSubManager adultSubManager;
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		adultSubManager = new AdultPremiumSubManager();
		adultSubManager.initialize();
		
		if (attachedActivity instanceof BoxPlayActivity) {
			updateLicence(LicenceKey.fromString(getManagers().getPreferences().getString(getString(R.string.boxplay_other_settings_premium_pref_premium_key_key), "")));
		}
	}
	
	/**
	 * Call to check the {@link LicenceKey} and call {@link #updateDrawer()} just to be sure that the ui is sync
	 * 
	 * @param licenceKey
	 *            Your new {@link LicenceKey}
	 */
	public void updateLicence(LicenceKey licenceKey) {
		this.licenceKey = licenceKey;
		
		if (licenceKey != null && !licenceKey.isChecked()) {
			licenceKey.verify();
		}
		
		updateDrawer();
	}
	
	/**
	 * Update the drawer, hide the premium menu ids if the {@link LicenceKey} key is not valid
	 */
	private void updateDrawer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!BoxPlayApplication.getBoxPlayApplication().isUiReady()) {
					ThreadUtils.sleep(100L);
				}
				
				boolean keyIsValid = isPremiumKeyValid();
				
				try {
					for (int menuId : premiumMenusId) {
						MenuItem menuItem = BoxPlayActivity.getBoxPlayActivity().getNavigationView().getMenu().findItem(menuId);
						
						if (menuItem != null) {
							menuItem.setVisible(keyIsValid);
							menuItem.setChecked(false);
						}
					}
				} catch (Exception exception) {
					;
				}
			}
		}).start();
	}
	
	/**
	 * Check if the actually registered {@link LicenceKey} is not null and valid
	 * 
	 * @return {@link LicenceKey} validation
	 */
	public boolean isPremiumKeyValid() {
		return licenceKey != null && licenceKey.isChecked() && licenceKey.isValid();
	}
	
	/**
	 * Get the {@link AdultPremiumSubManager} instance
	 * 
	 * @return The actual instance
	 */
	public AdultPremiumSubManager getAdultSubManager() {
		return adultSubManager;
	}
	
	/**
	 * Sub-Manager for Adult
	 * 
	 * @author Enzo CACERES
	 */
	public static class AdultPremiumSubManager extends SubManager {
		/* Managers */
		private DebugManager debugManager;
		
		/* Factory */
		private AdultFactory adultFactory = new AdultFactory();
		
		/* Callbacks */
		private AdultSubModuleCallback callback;
		
		/* Video page */
		private Map<String, Map<VideoOrigin, List<AdultVideo>>> pagedVideosMap;
		private List<AdultVideo> allVideos;
		private int farestLoadedPage = 0;
		
		/* Worker */
		private VideoExtractorWorker videoExtractorWorker;
		
		/* Old Variable; TODO: Update code */
		private boolean working = false;
		
		@Override
		protected void initialize() {
			this.debugManager = getManagers().getDebugManager();
			
			this.pagedVideosMap = new HashMap<>();
			this.allVideos = new ArrayList<>();
			
			this.videoExtractorWorker = new VideoExtractorWorker();
		}
		
		/**
		 * Attach a callback used to return eta of workers
		 * 
		 * @param callback
		 *            Target callback
		 */
		public void attachCallback(AdultSubModuleCallback callback) {
			this.callback = callback;
		}
		
		/**
		 * Reset all page loading, and come back to the first one with all cached value clear
		 */
		public void resetFetchData() {
			farestLoadedPage = 0;
			
			pagedVideosMap.clear();
			allVideos.clear();
			
			fetchPage(++farestLoadedPage);
		}
		
		/**
		 * Fetch the next page
		 */
		public void fetchNextPage() {
			fetchPage(++farestLoadedPage);
		}
		
		/**
		 * Fetch a page by its number
		 * 
		 * TODO: Update code to a {@link AbstractWorkerThread} with a Worker
		 * 
		 * @param targetPage
		 *            Targetted page number
		 */
		public void fetchPage(final int targetPage) {
			if (working) {
				ToastUtils.makeLong(boxPlayApplication, "Manager is budy.");
				return;
			}
			working = true;
			
			new Thread(new Runnable() {
				private String page = String.valueOf(targetPage);
				private Map<VideoOrigin, List<AdultVideo>> actualPageVideosMap = pagedVideosMap.get(page);
				
				@Override
				public void run() {
					String html = "";
					try {
						html = Downloader.getUrlContent(AdultFactory.formatHomepageUrl(targetPage));
					} catch (Exception exception) {
						notifyFail(exception);
						return;
					}
					
					if (actualPageVideosMap == null) {
						actualPageVideosMap = new HashMap<VideoOrigin, List<AdultVideo>>();
						pagedVideosMap.put(page, actualPageVideosMap);
					}
					
					adultFactory.parseHomepageHtml(new AdultFactoryListener() {
						@Override
						public void onHtmlNull() {
							notifyFail(new NullPointerException("Page is empty."));
						}
						
						@Override
						public void onAdultVideoCreated(AdultVideo adultVideo, VideoOrigin origin) {
							List<AdultVideo> videos = actualPageVideosMap.get(origin);
							if (videos == null) {
								videos = new ArrayList<AdultVideo>();
								actualPageVideosMap.put(origin, videos);
							}
							
							videos.add(adultVideo);
							
							if (!allVideos.contains(adultVideo)) {
								allVideos.add(adultVideo);
							}
						}
					}, html, (farestLoadedPage == 1));
					farestLoadedPage = targetPage;
					
					working = false;
					
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onLoadFinish();
							}
						}
					});
				}
				
			}).start();
		}
		
		/**
		 * Start the worker to extract a video from a target page
		 * 
		 * @param targetVideoPageUrl
		 *            Target video page
		 */
		public void fetchVideoPage(final String targetVideoPageUrl) {
			if (videoExtractorWorker.isRunning()) {
				ToastUtils.makeLong(boxPlayApplication, "VideoExtractorWorker is busy.");
				return;
			}
			
			videoExtractorWorker = new VideoExtractorWorker();
			videoExtractorWorker.applyData(targetVideoPageUrl, callback).start();
		}
		
		/**
		 * Use the callback to tell the app that a exception has occured
		 * 
		 * @param exception
		 *            Target exception
		 */
		private void notifyFail(final Exception exception) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callback != null) {
						callback.onLoadFailed(exception);
					}
				}
			});
			
			working = false; // TODO: Remove
		}
		
		/**
		 * Get the cached video map
		 * 
		 * @return Paged Videos Map
		 */
		public Map<String, Map<VideoOrigin, List<AdultVideo>>> getPagedVideosMap() {
			return pagedVideosMap;
		}
		
		/**
		 * Get all video fetched so far
		 * 
		 * @return A list containing all videos
		 */
		public List<AdultVideo> getAllVideos() {
			return allVideos;
		}
		
		/**
		 * Tell if the SubManager is actually working
		 * 
		 * @return If any of the worker is running
		 */
		public boolean isWorking() {
			return videoExtractorWorker.isRunning() || working; // TODO: Remove working
		}
		
		/**
		 * Video Extractor Worker, extract a video from its page url
		 * 
		 * @author Enzo CACERES
		 */
		class VideoExtractorWorker extends WorkerThread {
			private String videoPageUrl;
			private AdultSubModuleCallback callback;
			
			private VideoContentExtractor extractor;
			
			@Override
			protected void execute() {
				try {
					/* Downloading video page url */
					callback.onStatusUpdate(R.string.boxplay_premium_adult_status_downloading_data);
					
					String html = Downloader.getUrlContent(AdultFactory.formatWebToMobileUrl(videoPageUrl));
					if (!StringUtils.validate(html)) {
						throw new NullPointerException("Downloaded html is not valid");
					}
					
					/* Parsing information */
					callback.onStatusUpdate(R.string.boxplay_premium_adult_status_parsing);
					
					String targetAjaxUrl = adultFactory.parseVideoPageData(html);
					if (targetAjaxUrl == null) {
						throw new NullPointerException("Not find any valid information in the page");
					}
					
					/* Get openload iframe */
					Map<String, String> parameters = new HashMap<>();
					parameters.put("X-Requested-With", "XMLHttpRequest");
					
					String openloadIframeHtml = Downloader.webget(targetAjaxUrl, parameters, Charset.defaultCharset());
					
					/* Getting a valid video extractor */
					callback.onStatusUpdate(R.string.boxplay_premium_adult_status_computing_url);
					
					String playerUrl = adultFactory.extractOpenloadLinkFromIframe(openloadIframeHtml);
					extractor = (VideoContentExtractor) ContentExtractionManager.getExtractorFromBaseUrl(ExtractorType.VIDEO, playerUrl);
					
					if (extractor == null) {
						throw new NullPointerException(String.format("VideoContentExtractor is null, site not supported? (page url: %s)", playerUrl));
					}
					
					/* Starting video extraction */
					final String directUrl = extractor.extractDirectVideoUrl(playerUrl, new VideoContentExtractorProgressCallback() {
						@Override
						public void onDownloadingUrl(final String targetUrl) {
							if (callback != null) {
								callback.onStatusUpdate(R.string.boxplay_premium_adult_status_downloading_video_page);
							}
						}
						
						@Override
						public void onFileNotAvailable() {
							terminate();
							
							if (callback != null) {
								callback.onError(R.string.boxplay_premium_adult_status_error_file_not_found);
							}
						}
						
						@Override
						public void onExtractingLink() {
							if (callback != null) {
								callback.onStatusUpdate(R.string.boxplay_premium_adult_status_extracting_link);
							}
						}
						
						@Override
						public void onFormattingResult() {
							if (callback != null) {
								callback.onStatusUpdate(R.string.boxplay_premium_adult_status_formatting_result);
							}
						}
					});
					
					callback.returnDialog().hide();
					
					if (directUrl == null) {
						if (!isCancelled()) {
							callback.onError(R.string.boxplay_premium_adult_status_error_extraction_failed);
						}
					} else {
						callback.onUrlReady(directUrl);
					}
					
				} catch (Exception exception) {
					if (extractor != null) {
						extractor.notifyException(exception);
					} else {
						notifyFail(exception);
						Log.e(TAG, "Can't print in the extractor's logger (null)", exception);
					}
					
					if (!debugManager.openLogsAtExtractorEnd()) {
						callback.onError(R.string.boxplay_premium_adult_status_error_error_occured, StringUtils.fromException(exception));
					}
				}
				
				if (debugManager.openLogsAtExtractorEnd() && extractor != null) {
					DialogUtils.showDialog(handler, boxPlayApplication.getAttachedActivity(), "Extraction logs", extractor.getLogger().getContent());
				}
			}
			
			@Override
			protected void cancel() {
				callback.returnDialog().hide();
			}
			
			/**
			 * Apply local data to the worker
			 * 
			 * @param videoPageUrl
			 *            Target video page url
			 * @param callback
			 *            Callback to communicate with the ui
			 * @return Itself
			 */
			public VideoExtractorWorker applyData(String videoPageUrl, AdultSubModuleCallback callback) {
				this.videoPageUrl = videoPageUrl;
				this.callback = callback;
				
				if (callback == null) {
					throw new IllegalArgumentException("Callback can't be null");
				}
				
				return this;
			}
		}
		
	}
	
	/**
	 * Sub-Module callback for the Adult module
	 * 
	 * @author Enzo CACERES
	 */
	public static interface AdultSubModuleCallback {
		/**
		 * Called when a page has finished loaded
		 */
		void onLoadFinish();
		
		/**
		 * Called when an extractor has just finished extracting a video url
		 * 
		 * @param url
		 *            Freshly extracted video url
		 */
		void onUrlReady(String url);
		
		/**
		 * Called when the progress dialog must update his message
		 * 
		 * @param ressourceId
		 *            Target {@link string} eta ressource
		 */
		void onStatusUpdate(int ressourceId);
		
		/**
		 * Called when the extractor has returned an error and can't continue anymore, goal is to display this message to a {@link StyleableToast}
		 * 
		 * @param ressourceId
		 *            Target {@link string} error message ressource
		 * @param arguments
		 *            Falcutative, used to allow more formatting of the returned error
		 */
		void onError(int ressourceId, Object... arguments);
		
		/**
		 * Called when a page has failed to load
		 * 
		 * @param exception
		 *            Target exception
		 */
		void onLoadFailed(Exception exception);
		
		/**
		 * Called when the worker need to directly access the
		 * 
		 * @return Active {@link WorkingProgressDialog} used to display any progress of the module
		 */
		WorkingProgressDialog returnDialog();
	}
	
}