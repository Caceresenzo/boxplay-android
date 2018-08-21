package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.managers.XManagers.SubManager;
import caceresenzo.libs.boxplay.factory.AdultFactory;
import caceresenzo.libs.boxplay.factory.AdultFactory.AdultFactoryListener;
import caceresenzo.libs.boxplay.factory.AdultFactory.VideoOrigin;
import caceresenzo.libs.boxplay.models.premium.adult.AdultVideo;
import caceresenzo.libs.licencekey.LicenceKey;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.ThreadUtils;

public class PremiumManager extends AbstractManager {
	
	private static final List<Integer> premiumMenusId = new ArrayList<Integer>();
	
	static {
		premiumMenusId.add(R.id.drawer_boxplay_premium);
		premiumMenusId.add(R.id.drawer_boxplay_premium_adult);
	}
	
	public static final String PREF_KEY_ADULT_ANDROID_VERSION_DIALOG = "adult_warning_dialog_showed";
	
	private LicenceKey licenceKey;
	
	private AdultPremiumSubManager adultSubManager;
	
	public PremiumManager() {
		;
	}
	
	@Override
	protected void initialize() {
	}
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		adultSubManager = new AdultPremiumSubManager();
		adultSubManager.initialize();
		
		if (attachedActivity instanceof BoxPlayActivity) {
			updateLicence(LicenceKey.fromString(getManagers().getPreferences().getString(getString(R.string.boxplay_other_settings_premium_pref_premium_key_key), "")));
		}
	}
	
	public void updateLicence(LicenceKey licenceKey) {
		this.licenceKey = licenceKey;
		
		if (licenceKey != null && !licenceKey.isChecked()) {
			licenceKey.verify();
		}
		
		updateDrawer();
	}
	
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
	
	public boolean isPremiumKeyValid() {
		return licenceKey != null && licenceKey.isChecked() && licenceKey.isValid();
	}
	
	public AdultPremiumSubManager getAdultSubManager() {
		return adultSubManager;
	}
	
	public class AdultPremiumSubManager extends SubManager {
		
		private AdultFactory adultFactory = new AdultFactory();
		private boolean working = false;
		
		private AdultSubModuleCallback callback;
		
		private Map<String, Map<VideoOrigin, List<AdultVideo>>> pagedVideosMap;
		private List<AdultVideo> allVideos;
		private int farestLoadedPage = 0;
		
		@Override
		protected void initialize() {
			pagedVideosMap = new HashMap<String, Map<VideoOrigin, List<AdultVideo>>>();
			allVideos = new ArrayList<AdultVideo>();
		}
		
		public void attachCallback(AdultSubModuleCallback callback) {
			this.callback = callback;
			
			if (Build.VERSION.SDK_INT < 23) {
				boolean warningDialogShowed = getManagers().getPreferences().getBoolean(PREF_KEY_ADULT_ANDROID_VERSION_DIALOG, false);
				
				if (!warningDialogShowed) {
					getManagers().getPreferences().edit().putBoolean(PREF_KEY_ADULT_ANDROID_VERSION_DIALOG, true).commit();
					
					AlertDialog.Builder builder = new AlertDialog.Builder(boxPlayApplication); //
					builder.setMessage(R.string.boxplay_premium_adult_warning_dialog_message) //
							.setCancelable(false) //
							.setNegativeButton(R.string.boxplay_premium_adult_warning_dialog_done, new DialogInterface.OnClickListener() { //
								public void onClick(DialogInterface dialog, int id) { //
									dialog.cancel(); //
								} //
							}); //
					AlertDialog alertDialog = builder.create(); //
					alertDialog.show(); //
				}
			}
		}
		
		public void resetFetchData() {
			farestLoadedPage = 0;
			
			pagedVideosMap.clear();
			allVideos.clear();
			
			fetchPage(++farestLoadedPage);
		}
		
		public void fetchNextPage() {
			fetchPage(++farestLoadedPage);
		}
		
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
		
		public void fetchVideoPage(final String targetVideoUrl) {
			if (working) {
				ToastUtils.makeLong(boxPlayApplication, "Manager is budy.");
				return;
			}
			
			working = true;
			
			new Thread(new Runnable() {
				private String videoUrl = String.valueOf(targetVideoUrl);
				private boolean nextStepReady = false;
				private WebView webView;
				private String openloadVideoKey;
				
				@Override
				public void run() {
					if (callback != null) {
						callback.onStatusUpdate(R.string.boxplay_premium_adult_status_downloading_data);
					}
					
					String html = "";
					try {
						html = Downloader.getUrlContent(AdultFactory.formatWebToMobileUrl(videoUrl));
					} catch (Exception exception) {
						notifyFail(exception);
						return;
					}
					
					if (callback != null) {
						callback.onStatusUpdate(R.string.boxplay_premium_adult_status_parsing);
					}
					
					String targetAjaxUrl = adultFactory.parseVideoPageData(html);
					if (targetAjaxUrl == null) {
						notifyFail(new NullPointerException("Not find any valid information in the page"));
						return;
					}
					
					String openloadIframeHtml = "";
					try {
						HashMap<String, String> parameters = new HashMap<String, String>();
						parameters.put("X-Requested-With", "XMLHttpRequest");
						
						openloadIframeHtml = Downloader.webget(targetAjaxUrl, parameters, Charset.defaultCharset());
					} catch (Exception exception) {
						notifyFail(exception);
						return;
					}
					
					String openloadHtml = "";
					try {
						openloadHtml = Downloader.getUrlContent(adultFactory.extractOpenloadLinkFromIframe(openloadIframeHtml));
					} catch (Exception exception) {
						notifyFail(exception);
						return;
					}
					
					if (callback != null) {
						callback.onStatusUpdate(R.string.boxplay_premium_adult_status_computing_url);
					}
					
					File openloadTempFile = new File(getManagers().getBaseDataDirectory(), "openload.html");
					if (openloadTempFile.exists()) {
						openloadTempFile.delete();
					}
					
					String passOpenloadHtml = "";
					try {
						openloadTempFile.createNewFile();
						StringUtils.stringToFile(openloadTempFile, openloadHtml); // Because...................... seens to have "encoding" problem, regex is not working without this.......
						passOpenloadHtml = StringUtils.fromFile(openloadTempFile);
					} catch (IOException exception) {
						exception.printStackTrace();
						passOpenloadHtml = openloadHtml;
					}
					
					final String finalPassOpenloadHtml = passOpenloadHtml;
					
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							webView = new WebView(boxPlayApplication);
							webView.getSettings().setJavaScriptEnabled(true);
							webView.loadDataWithBaseURL("", AdultFactory.formatOpenloadJsCodeExecutor(adultFactory.formatHtmlDomForJsKeyGenerator(finalPassOpenloadHtml), adultFactory.extractOpenloadJSKeyGeneratorFromHtml(finalPassOpenloadHtml)), "text/html", "utf-8", "");
							webView.setWebViewClient(new WebViewClient() {
								public void onPageFinished(WebView view, String url) {
									// webView.evaluateJavascript("(function() { myFunction(); })();", new ValueCallback<String>() {
									// @Override
									// public void onReceiveValue(String html) {
									nextStepReady = true;
									// }
									// });
								}
							});
						}
					});
					
					while (!nextStepReady) {
						ThreadUtils.sleep(100L);
					}
					
					nextStepReady = false;
					
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							webView.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();", new ValueCallback<String>() {
								@Override
								public void onReceiveValue(String html) {
									Log.d("HTML", html);
									String resolvedHtml = html.replace("\\u003C", "<").replace("\\\"", "\"").replace("\\n", "\n");
									// DialogUtils.showDialog(boxPlayActivity, "html", resolvedHtml);
									
									openloadVideoKey = adultFactory.extractOpenloadVideoLinkFromJSExecutedHtmlPage(resolvedHtml);
									
									nextStepReady = true;
								}
							});
						}
					});
					
					while (!nextStepReady) {
						ThreadUtils.sleep(100L);
					}
					
					if (openloadVideoKey == null) {
						notifyFail(new NullPointerException("Page not js-executed correctly."));
						return;
					}
					
					final String finalVideoLink = AdultFactory.formatOpenloadDirectLinkVideoUrl(openloadVideoKey);
					
					working = false;
					
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onUrlReady(finalVideoLink);
							}
						}
					});
				}
				
			}).start();
		}
		
		private void notifyFail(final Exception exception) {
			BoxPlayApplication.getHandler().post(new Runnable() {
				@Override
				public void run() {
					if (callback != null) {
						callback.onLoadFailed(exception);
					}
				}
			});
			
			working = false;
		}
		
		public Map<String, Map<VideoOrigin, List<AdultVideo>>> getPagedVideosMap() {
			return pagedVideosMap;
		}
		
		public List<AdultVideo> getAllVideos() {
			return allVideos;
		}
		
		public boolean isWorking() {
			return working;
		}
		
	}
	
	public static interface AdultSubModuleCallback {
		void onLoadFinish();
		
		void onUrlReady(String url);
		
		void onStatusUpdate(int ressourceId);
		
		void onLoadFailed(Exception exception);
	}
	
}