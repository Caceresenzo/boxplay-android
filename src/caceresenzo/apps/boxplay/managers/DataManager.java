package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import android.support.design.widget.Snackbar;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.other.about.PageAboutHostingFragment;
import caceresenzo.apps.boxplay.fragments.store.StorePageFragment;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.cryptography.MD5;
import caceresenzo.libs.filesystem.FileUtils;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.json.parser.JsonException;
import caceresenzo.libs.json.parser.JsonParser;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.string.StringUtils;
import caceresenzo.libs.thread.HelpedThread;
import caceresenzo.libs.thread.ThreadUtils;

public class DataManager extends AbstractManager {
	
	public static final String TAG = DataManager.class.getSimpleName();
	
	private final String downloadUrl;
	private final File cacheFolder, cachedUrlFile;
	
	private JsonObject serverJsonData;
	private int serverJsonRevision = 0;
	
	private boolean workableDataReady = false, runningOnCacheVersion;
	
	private Snackbar snackbar;
	
	private Worker worker;
	
	public DataManager() {
		super();
		
		this.downloadUrl = getString(R.string.boxplay_data_download_url);
		
		this.cacheFolder = new File(getManagers().getBaseApplicationDirectory() + "/cache/");
		
		String cachedUrlFileName;
		try {
			cachedUrlFileName = MD5.generateStringMD5(downloadUrl);
		} catch (NoSuchAlgorithmException exception) {
			cachedUrlFileName = FileUtils.remplaceIllegalChar(downloadUrl);
		}
		
		this.cachedUrlFile = new File(cacheFolder, cachedUrlFileName + ".cache");
		
		this.worker = new Worker();
	}
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		fetchData(false);
	}
	
	public void fetchData(final boolean forceFetch) {
		if (worker.isRunning()) {
			return;
		}
		
		worker = new Worker();
		worker.force(forceFetch).start();
	}
	
	public JsonObject getJsonData() {
		return serverJsonData;
	}
	
	public boolean isWorkableDataReady() {
		return workableDataReady;
	}
	
	public boolean isRunningOnCacheVersion() {
		return runningOnCacheVersion;
	}
	
	class Worker extends HelpedThread {
		private boolean forceFetch;
		
		@Override
		protected void onRun() {
			while (!BoxPlayApplication.getBoxPlayApplication().isUiReady()) {
				ThreadUtils.sleep(100L);
			}
			
			BoxPlayApplication.getHandler().post(new Runnable() {
				@Override
				public void run() {
					if (!workableDataReady) {
						snackbar = BoxPlayApplication.getBoxPlayApplication().snackbar(getString(R.string.boxplay_store_video_data_downloading), Snackbar.LENGTH_INDEFINITE);
						snackbar.show();
					}
				}
			});
			
			final int oldServerJsonRevision = serverJsonRevision;
			String content = null;
			try {
				content = Downloader.getUrlContent(downloadUrl);
				serverJsonData = new JsonObject((Map<?, ?>) new JsonParser().parse(new StringReader(content)));
				
				serverJsonRevision = ParseUtils.parseInt(serverJsonData.get("json_revision"), 0);
			} catch (IOException | JsonException exception) {
				Log.e(TAG, "Failed to download and parse json from the server, trying cached version.", exception);
				
				try {
					content = StringUtils.fromFile(cachedUrlFile);
					serverJsonData = new JsonObject((Map<?, ?>) new JsonParser().parse(new StringReader(content)));
				} catch (Exception exception2) {
					Log.e(TAG, "Failed to read cached version.", exception2);
				}
				
				runningOnCacheVersion = content != null && serverJsonData != null;
			} finally {
				try {
					if (content != null) {
						getManagers().writeLocalFile(cachedUrlFile, content);
					}
				} catch (IOException exception2) {
					Log.e(TAG, "Failed to write cache version.", exception2);
				}
			}
			
			workableDataReady = true;
			
			BoxPlayApplication.getHandler().post(new Runnable() {
				@Override
				public void run() {
					if (snackbar != null) {
						snackbar.dismiss();
					}
					
					boolean newContent = oldServerJsonRevision != serverJsonRevision;
					boolean force = getManagers().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_boxplay_pref_force_factory_key), false);
					
					if (forceFetch) {
						force = true;
					}
					
					if ((newContent || force) || isRunningOnCacheVersion()) {
						getManagers().getVideoManager().callFactory();
						getManagers().getMusicManager().callFactory();
						getManagers().getServerManager().callFactory();
						getManagers().getUpdateManager().prepareDialogBuild();
					}
					
					for (StorePageFragment storePageFragment : StorePageFragment.getStorePageFragmentRegisteredInstances()) {
						if (storePageFragment != null) {
							storePageFragment.callDataUpdater(newContent);
						}
					}
					
					if (PageAboutHostingFragment.getPageAboutPageFragment() != null) {
						PageAboutHostingFragment.getPageAboutPageFragment().updateServers();
					}
				}
			});
			
		}
		
		public HelpedThread force(boolean force) {
			forceFetch = force;
			return this;
		}
		
		@Override
		protected void onFinished() {
			;
		}
		
		@Override
		protected void onCancelled() {
			;
		}
		
	}
	
}