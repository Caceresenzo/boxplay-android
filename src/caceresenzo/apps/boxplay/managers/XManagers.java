package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ViewHelper;

public class XManagers {
	
	/* Tag */
	public static final String TAG = XManagers.class.getSimpleName();
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	
	protected PermissionManager permissionManager;
	protected DataManager dataManager;
	protected VideoManager videoManager;
	protected UpdateManager updateManager;
	protected PremiumManager premiumManager;
	protected SearchAndGoManager searchAndGoManager;
	protected SubscriptionManager subscriptionManager;
	protected MyListManager myListManager;
	protected DebugManager debugManager;
	protected BackgroundServiceManager backgroundServiceManager;
	
	/* Files */
	private File baseApplicationDirectory, baseDataDirectory;
	
	/* Preferences */
	protected SharedPreferences preferences;
	
	/* List */
	private List<AbstractManager> managers;
	
	/* Constructor */
	public XManagers(BoxPlayApplication boxPlayApplication) {
		this.boxPlayApplication = boxPlayApplication;
	}
	
	public XManagers initialize() {
		if (Build.VERSION.SDK_INT <= 24) {
			baseApplicationDirectory = boxPlayApplication.getFilesDir();
		} else {
			baseApplicationDirectory = boxPlayApplication.getDataDir();
		}
		Log.d(TAG, "BASE APPLICATION DIRECTORY : " + baseApplicationDirectory.getAbsolutePath());
		baseDataDirectory = new File(baseApplicationDirectory, "data" + File.separator);
		
		managers = new ArrayList<AbstractManager>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean add(AbstractManager manager) {
				Log.d(TAG, "Registering manager: " + manager.getClass().getSimpleName() + " (size=" + (size() + 1) + ")");
				return super.add(manager);
			}
		};
		
		// Config
		preferences = PreferenceManager.getDefaultSharedPreferences(BoxPlayApplication.getBoxPlayApplication());
		
		// managers.add(identificationManager = new IdentificationManager());
		// managers.add(userManager = new UserManager());
		
		managers.add(permissionManager = new PermissionManager());
		
		managers.add(dataManager = new DataManager());
		managers.add(videoManager = new VideoManager());
		
		managers.add(updateManager = new UpdateManager());
		
		managers.add(premiumManager = new PremiumManager());

		managers.add(searchAndGoManager = new SearchAndGoManager());
		managers.add(subscriptionManager = new SubscriptionManager());
		
		managers.add(myListManager = new MyListManager());
		
		managers.add(debugManager = new DebugManager());
		
		managers.add(backgroundServiceManager = new BackgroundServiceManager());
		
		for (AbstractManager manager : managers) {
			manager.initialize();
		}
		
		return this;
	}
	
	public void onUiReady(BaseBoxPlayActivty attachedActivity) {
		for (AbstractManager manager : managers) {
			manager.initializeWhenUiReady(attachedActivity);
		}
	}
	
	public void destroy() {
		for (AbstractManager manager : managers) {
			manager.destroy();
		}
	}
	
	public File getBaseApplicationDirectory() {
		return baseApplicationDirectory;
	}
	
	public File getBaseDataDirectory() {
		return baseDataDirectory;
	}
	
	public SharedPreferences getPreferences() {
		return preferences;
	}
	
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}
	
	public DataManager getDataManager() {
		return dataManager;
	}
	
	public VideoManager getVideoManager() {
		return videoManager;
	}
	
	public UpdateManager getUpdateManager() {
		return updateManager;
	}
	
	public PremiumManager getPremiumManager() {
		return premiumManager;
	}
	
	public SearchAndGoManager getSearchAndGoManager() {
		return searchAndGoManager;
	}
	
	public SubscriptionManager getSubscriptionManager() {
		return subscriptionManager;
	}
	
	public MyListManager getMyListManager() {
		return myListManager;
	}
	
	public DebugManager getDebugManager() {
		return debugManager;
	}
	
	public BackgroundServiceManager getBackgroundServiceManager() {
		return backgroundServiceManager;
	}
	
	protected abstract static class AbstractManager {
		protected BoxPlayApplication boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		protected Handler handler = BoxPlayApplication.getHandler();

		protected CacheHelper cacheHelper = boxPlayApplication.getCacheHelper();
		protected ViewHelper viewHelper = boxPlayApplication.getViewHelper();
		
		protected void initialize() {
			;
		}
		
		protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
			;
		}
		
		protected void destroy() {
			;
		}
		
		protected XManagers getManagers() {
			return BoxPlayApplication.getBoxPlayApplication().getManagers();
		}
		
		protected String getString(int ressourceId) {
			return BoxPlayApplication.getBoxPlayApplication().getString(ressourceId);
		}
		
		protected String getString(int ressourceId, Object... args) {
			return BoxPlayApplication.getBoxPlayApplication().getString(ressourceId, args);
		}
	}
	
	protected abstract static class SubManager extends AbstractManager {
		;
	}
	
	protected String getString(int ressourceId) {
		return boxPlayApplication.getString(ressourceId);
	}
	
	protected String getString(int ressourceId, Object... args) {
		return boxPlayApplication.getString(ressourceId, args);
	}
	
	public void writeLocalFile(File file, String string) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(string.getBytes());
		outputStream.close();
	}
	
}