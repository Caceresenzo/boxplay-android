package caceresenzo.libs.boxplay.common.extractor.video.implementations;

import android.os.Handler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderHelper;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.string.StringUtils;

public class AndroidOpenloadVideoExtractor extends OpenloadVideoExtractor {
	
	public static final String FILE_DELETED = "We can't find the file you are looking for. It maybe got deleted by the owner or was removed due a copyright violation.";
	
	private final Handler handler;
	
	private WebView webView;
	
	private String pageContent, resolvedHtml;
	
	public AndroidOpenloadVideoExtractor() {
		this.handler = BoxPlayApplication.getHandler();
		
		lock();
		handler.post(new Runnable() {
			@Override
			public void run() {
				webView = new WebView(BoxPlayApplication.getBoxPlayApplication());
				unlock();
			}
		});
	}
	
	@Override
	public String downloadTargetPage(String url) {
		getLogger().appendln("Downloading page: " + url);
		
		try {
			pageContent = Downloader.getUrlContent(url);
			getLogger().appendln("-- Finished > size= " + pageContent.length()).separator();
			return pageContent;
		} catch (Exception exception) {
			getLogger().appendln("-- Finished > Failed").separator();
			failed(true).notifyException(exception);
			return null;
		}
	}
	
	@Override
	public boolean checkStreamingAvailability(String html) {
		if (html == null) {
			return false;
		}
		
		return !html.contains(FILE_DELETED);
	}
	
	@Override
	public void injectJsCode(final String code) {
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
				webView.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();", new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String html) {
						resolvedHtml = html.replace("\\u003C", "<").replace("\\\"", "\"").replace("\\n", "\n");
						
						getLogger().appendln("WebView > Resolved HTML: \"" + StringUtils.cutIfTooLong(resolvedHtml, 150) + "\" (cut a length 150)");
						
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