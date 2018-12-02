package caceresenzo.libs.boxplay.common.extractor.video.implementations;

import java.io.File;
import java.io.IOException;

import android.os.Handler;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.libs.boxplay.common.extractor.html.HtmlCommonExtractor;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderHelper;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.string.StringUtils;

public class AndroidOpenloadVideoExtractor extends OldAbstractOpenloadVideoExtractor {
	
	public static final String TAG = AndroidOpenloadVideoExtractor.class.getSimpleName();
	
	private final XManagers managers;
	private final Handler handler;
	
	private WebView webView;
	
	private String pageContent, resolvedHtml;
	
	public AndroidOpenloadVideoExtractor() {
		this.managers = BoxPlayApplication.getManagers();
		this.handler = BoxPlayApplication.getHandler();
	}
	
	@Override
	public String downloadTargetPage(String url) {
		getLogger().appendln("Downloading page: " + url);
		
		try {
			pageContent = Downloader.getUrlContent(url);
			getLogger().appendln("-- Finished > size= " + pageContent.length());
			
			File cacheFile = new File(managers.getBaseDataDirectory(), "/openload.html");
			try {
				getLogger().appendln("-- Caching STARTING");
				
				getLogger().appendln("-- -- Old file exist? " + cacheFile.exists());
				if (cacheFile.exists()) {
					getLogger().appendln("-- -- Deleting old file. RESULT: " + cacheFile.delete());
				}
				
				getLogger().appendln("-- -- Creating new file. RESULT: " + cacheFile.createNewFile());
				
				StringUtils.stringToFile(cacheFile, pageContent); // Seems to have "encoding" problem, regex is sometimes not working without this
				pageContent = StringUtils.fromFile(cacheFile);
				
				getLogger().appendln("-- Caching OK");
			} catch (IOException exception) {
				Log.e(TAG, "Failed to restore openload file from cache", exception);
				getLogger().appendln("-- Caching ERROR");
				getLogger().appendln(StringUtils.fromException(exception));
				getLogger().appendln("-- Caching FAIL");
			}
			
			getLogger().separator();
			
			try {
				cacheFile.delete();
			} catch (Exception exception) {
				;
			}
			
			return pageContent;
		} catch (Exception exception) {
			getLogger().appendln("-- Finished > Failed").separator();
			failed(true).notifyException(exception);
			return null;
		}
	}
	
	@Override
	public void injectJsCode(final String code, String openloadHtml) {
		lock();
		handler.post(new Runnable() {
			@Override
			public void run() {
				getLogger().appendln("Creating WebView instance...");
				
				webView = new WebView(BoxPlayApplication.getBoxPlayApplication());
				unlock();
			}
		});
		
		waitUntilUnlock();
		
		lock();
		
		getLogger().appendln("WebView > Starting code injection...");
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				webView.getSettings().setJavaScriptEnabled(true);
				webView.loadDataWithBaseURL("", code, "text/html", "utf-8", "");
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						getLogger().appendln("WebView > Page finished loaded");
						unlock();
					}
				});
			}
		});
	}
	
	@Override
	public String getJsResult() {
		lock();
		
		getLogger().appendln("WebView > Starting code extraction...");
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				webView.evaluateJavascript(HtmlCommonExtractor.COMMON_JS_FUNCTION_EXTRACT_HTML, new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String html) {
						resolvedHtml = html.replace("\\u003C", "<").replace("\\\"", "\"").replace("\\n", "\n");
						
						getLogger().appendln("WebView > Resolved HTML: \"" + StringUtils.cutIfTooLong(resolvedHtml, 150) + "\" (cut a length 150)");
						
						getLogger().appendln("Destroying WebView instance...");
						webView.destroy();
						webView = null;
						
						unlock();
					}
				});
			}
		});
		
		waitUntilUnlock();
		
		return resolvedHtml;
	}
	
	@Override
	public String getOpenloadKey(String jsCodeResult) {
		String key = ProviderHelper.getStaticHelper().extract(REGEX_DOM_DATA_EXTRACTOR, jsCodeResult, 3);
		
		getLogger().separator().appendln("Openload > Key: " + key);
		
		return key;
	}
	
}