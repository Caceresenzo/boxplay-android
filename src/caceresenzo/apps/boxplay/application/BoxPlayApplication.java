package caceresenzo.apps.boxplay.application;

import com.google.android.gms.ads.MobileAds;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;
import caceresenzo.android.libs.javascript.AndroidJavaScriptExecutorLibrary;
import caceresenzo.android.libs.uncaughtexceptionhandler.AndroidUncaughtExceptionHandler;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.helper.HelperManager;
import caceresenzo.apps.boxplay.helper.implementations.ApplicationHelper;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ImageHelper;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.helper.implementations.MenuHelper;
import caceresenzo.apps.boxplay.helper.implementations.ViewHelper;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;

public class BoxPlayApplication extends Application {
	
	/* Tag */
	public static final String TAG = BoxPlayApplication.class.getSimpleName();
	
	/* Set Build as Debug */
	public static final boolean BUILD_DEBUG = true;
	
	/* Version */
	private static final Version VERSION = new Version("3.1.23.1", VersionType.BETA);
	
	/* Instance */
	private static BoxPlayApplication APPLICATION;
	
	/* Statics */
	private static BaseBoxPlayActivty ATTACHED_ACTIVITY;
	
	/* Managers */
	private static Handler HANDLER = new Handler();
	
	private HelperManager helperManager;
	private XManagers managers;
	
	/* Preferences */
	private SharedPreferences sharedPreferences;
	
	public BoxPlayApplication() {
		super();
		
		this.helperManager = new HelperManager(this);
		this.managers = new XManagers(this);
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		APPLICATION = this;
		
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		new AndroidUncaughtExceptionHandler.Builder(getApplicationContext()) //
				.setHandlerEnabled(sharedPreferences.getBoolean(getString(R.string.boxplay_other_settings_application_pref_crash_reporter_key), true)) //
				.setTrackActivitiesEnabled(true) //
				.setBackgroundModeEnabled(true) //
				.addCommaSeparatedEmailAddresses("caceresenzo1502@gmail.com") //
				.build();
		
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		
		createNotificationChannels();
		
		helperManager.initialize();
		managers.initialize();
		
		AndroidJavaScriptExecutorLibrary.use(this, HANDLER);
		
		MobileAds.initialize(this, getString(R.string.admob_app_id));
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	public static void attachActivity(BaseBoxPlayActivty baseBoxPlayActivty) {
		BoxPlayApplication.ATTACHED_ACTIVITY = baseBoxPlayActivty;
		
		getBoxPlayApplication().getManagers().onUiReady(baseBoxPlayActivty);
	}
	
	public boolean isUiReady() {
		if (ATTACHED_ACTIVITY == null) {
			return false;
		}
		
		return ATTACHED_ACTIVITY.isReady();
	}
	
	/**
	 * Create the notification channels that are required after Android O if necessary.<br>
	 * If in {@link #BUILD_DEBUG}, the notification channels will alaways be destroyed then recreated
	 */
	private void createNotificationChannels() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			
			if (BoxPlayApplication.BUILD_DEBUG) {
				for (String channel : new String[] { Constants.NOTIFICATION_CHANNEL.MAIN, Constants.NOTIFICATION_CHANNEL.SEARCH_AND_GO_UPDATE }) {
					if (notificationManager.getNotificationChannel(channel) != null) {
						notificationManager.deleteNotificationChannel(channel);
					}
				}
			}
			
			if (notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL.MAIN) == null) {
				NotificationChannel mainChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL.MAIN, getString(R.string.boxplay_notification_channel_main_title), NotificationManager.IMPORTANCE_DEFAULT);
				mainChannel.setDescription(getString(R.string.boxplay_notification_channel_main_description));
				mainChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
				
				notificationManager.createNotificationChannel(mainChannel);
			}
			
			if (notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL.SEARCH_AND_GO_UPDATE) == null) {
				NotificationChannel searchAndGoUpdateChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL.SEARCH_AND_GO_UPDATE, getString(R.string.boxplay_notification_channel_search_and_go_update_title), NotificationManager.IMPORTANCE_DEFAULT);
				searchAndGoUpdateChannel.setDescription(getString(R.string.boxplay_notification_channel_search_and_go_update_description));
				searchAndGoUpdateChannel.setImportance(NotificationManager.IMPORTANCE_MAX);
				
				notificationManager.createNotificationChannel(searchAndGoUpdateChannel);
			}
		}
	}
	
	public Snackbar snackbar(String text, int duration) {
		return Snackbar.make(ATTACHED_ACTIVITY.getCoordinatorLayout(), text, duration);
	}
	
	public Snackbar snackbar(int ressourceId, int duration, Object... args) {
		return snackbar(getString(ressourceId, args), duration);
	}
	
	public StyleableToast toast(String string) {
		return StyleableToast.makeText(this, string, R.style.customStylableToastStyle);
	}
	
	public StyleableToast toast(int ressourceId, Object... args) {
		return StyleableToast.makeText(this, getString(ressourceId, args), Toast.LENGTH_LONG, R.style.customStylableToastStyle);
	}
	
	public SharedPreferences getPreferences() {
		return sharedPreferences;
	}
	
	public BaseBoxPlayActivty getAttachedActivity() {
		return ATTACHED_ACTIVITY;
	}
	
	public FragmentManager getSupportFragmentManager() {
		return getAttachedActivity().getSupportFragmentManager();
	}
	
	/** @return The {@link ApplicationHelper} instance. */
	public ApplicationHelper getApplicationHelper() {
		return helperManager.getApplicationHelper();
	}
	
	/** @return The {@link CacheHelper} instance. */
	public CacheHelper getCacheHelper() {
		return helperManager.getCacheHelper();
	}
	
	/** @return The {@link ImageHelper} instance. */
	public ImageHelper getImageHelper() {
		return helperManager.getImageHelper();
	}
	
	/** @return The {@link LocaleHelper} instance. */
	public LocaleHelper getLocaleHelper() {
		return helperManager.getLocaleHelper();
	}
	
	/** @return The {@link MenuHelper} instance. */
	public MenuHelper getMenuHelper() {
		return helperManager.getMenuHelper();
	}
	
	/** @return The {@link ViewHelper} instance. */
	public ViewHelper getViewHelper() {
		return helperManager.getViewHelper();
	}
	
	/** @return The {@link XManagers} instance. */
	public XManagers getManagers() {
		return managers;
	}
	
	/** @return The main {@link Handler} instance. */
	public static Handler getHandler() {
		return HANDLER;
	}
	
	/** @return BoxPlay {@link Version}. */
	public static Version getVersion() {
		return VERSION;
	}
	
	/** @return Main {@link Application} instance. */
	public static BoxPlayApplication getBoxPlayApplication() {
		return APPLICATION;
	}
	
}