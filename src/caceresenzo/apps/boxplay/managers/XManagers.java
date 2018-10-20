package caceresenzo.apps.boxplay.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.libs.boxplay.api.BoxPlayApi;
import caceresenzo.libs.boxplay.users.User;

public class XManagers {
	
	/* Tag */
	public static final String TAG = XManagers.class.getSimpleName();
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	
	protected IdentificationManager identificationManager;
	protected UserManager userManager;
	protected PermissionManager permissionManager;
	protected DataManager dataManager;
	protected VideoManager videoManager;
	protected MusicManager musicManager;
	protected ServerManager serverManager;
	protected UpdateManager updateManager;
	protected TutorialManager tutorialManager;
	protected PremiumManager premiumManager;
	protected SearchAndGoManager searchAndGoManager;
	protected MyListManager myListManager;
	protected DebugManager debugManager;
	protected BackgroundServiceManager backgroundServiceManager;
	
	/* Files */
	protected final File baseApplicationDirectory;
	protected final File baseDataDirectory;
	
	/* Preferences */
	protected SharedPreferences preferences;
	
	/* List */
	private List<AbstractManager> managers;
	
	public XManagers() {
		baseApplicationDirectory = new File("/sdcard" + "/" + "BoxPlay" + "/"); // TODO: getString(R.string.application_name) and Environment.getExternalStorageDirectory()
		baseDataDirectory = new File(baseApplicationDirectory, "data/");
	}
	
	public XManagers initialize(final BoxPlayApplication boxPlayApplication) {
		this.boxPlayApplication = boxPlayApplication;
		
		managers = new ArrayList<AbstractManager>() {
			@Override
			public boolean add(AbstractManager manager) {
				Log.d(TAG, "Registering manager: " + manager.getClass().getSimpleName() + " (size=" + (size() + 1) + ")");
				return super.add(manager);
			}
		};
		
		// Config
		preferences = PreferenceManager.getDefaultSharedPreferences(BoxPlayApplication.getBoxPlayApplication());
		
		// if (identificationManager == null) {
		// managers.add(identificationManager = new IdentificationManager());
		// }
		
		// if (userManager == null) {
		// managers.add(userManager = new UserManager());
		// }
		
		if (permissionManager == null) {
			managers.add(permissionManager = new PermissionManager());
		}
		
		if (dataManager == null) {
			managers.add(dataManager = new DataManager());
		}
		
		if (videoManager == null) {
			managers.add(videoManager = new VideoManager());
		}
		
		if (musicManager == null) {
			managers.add(musicManager = new MusicManager());
		}
		
		if (serverManager == null) {
			managers.add(serverManager = new ServerManager());
		}
		
		if (updateManager == null) {
			managers.add(updateManager = new UpdateManager());
		}
		
		if (tutorialManager == null) {
			managers.add(tutorialManager = new TutorialManager());
		}
		
		if (premiumManager == null) {
			managers.add(premiumManager = new PremiumManager());
		}
		
		if (searchAndGoManager == null) {
			managers.add(searchAndGoManager = new SearchAndGoManager());
		}
		
		if (myListManager == null) {
			managers.add(myListManager = new MyListManager());
		}
		
		if (debugManager == null) {
			managers.add(debugManager = new DebugManager());
		}
		
		if (backgroundServiceManager == null) {
			managers.add(backgroundServiceManager = new BackgroundServiceManager());
		}
		
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
	
	public void onUserLogged(final User user, final BoxPlayApi boxPlayApi) {
		// for (AbstractManager manager : managers) {
		// manager.initializeWhenUserLogged(user, boxPlayApi);
		// }
		throw new IllegalStateException("Disabled.");
	}
	
	public void destroy() {
		for (AbstractManager manager : managers) {
			manager.destroy();
		}
	}
	
	public void checkAndRecreate() {
		if (managers == null) {
			if (BoxPlayApplication.getBoxPlayApplication() != null) {
				initialize(BoxPlayApplication.getBoxPlayApplication());
			}
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
	
	public IdentificationManager getIdentificationManager() {
		// checkAndRecreate();
		// return identificationManager;
		throw new IllegalStateException("Disabled.");
	}
	
	public UserManager getUserManager() {
		// checkAndRecreate();
		// return userManager;
		throw new IllegalStateException("Disabled.");
	}
	
	public PermissionManager getPermissionManager() {
		checkAndRecreate();
		return permissionManager;
	}
	
	public DataManager getDataManager() {
		checkAndRecreate();
		return dataManager;
	}
	
	public VideoManager getVideoManager() {
		checkAndRecreate();
		return videoManager;
	}
	
	public MusicManager getMusicManager() {
		checkAndRecreate();
		return musicManager;
	}
	
	public ServerManager getServerManager() {
		checkAndRecreate();
		return serverManager;
	}
	
	public UpdateManager getUpdateManager() {
		checkAndRecreate();
		return updateManager;
	}
	
	public TutorialManager getTutorialManager() {
		checkAndRecreate();
		return tutorialManager;
	}
	
	public PremiumManager getPremiumManager() {
		checkAndRecreate();
		return premiumManager;
	}
	
	public SearchAndGoManager getSearchAndGoManager() {
		checkAndRecreate();
		return searchAndGoManager;
	}
	
	public MyListManager getMyListManager() {
		checkAndRecreate();
		return myListManager;
	}
	
	public DebugManager getDebugManager() {
		checkAndRecreate();
		return debugManager;
	}
	
	public BackgroundServiceManager getBackgroundServiceManager() {
		checkAndRecreate();
		return backgroundServiceManager;
	}
	
	protected abstract static class AbstractManager {
		protected BoxPlayApplication boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		protected Handler handler = BoxPlayApplication.getHandler();
		protected ViewHelper viewHelper = BoxPlayApplication.getViewHelper();
		
		protected void initialize() {
			;
		}
		
		protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
			;
		}
		
		protected void initializeWhenUserLogged(User user, BoxPlayApi boxPlayApi) {
			;
		}
		
		protected void destroy() {
			;
		}
		
		protected XManagers getManagers() {
			return BoxPlayApplication.getManagers();
		}
		
		protected String getString(int ressourceId, Object... args) {
			return BoxPlayApplication.getBoxPlayApplication().getString(ressourceId, args);
		}
	}
	
	protected abstract static class SubManager extends AbstractManager {
		;
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