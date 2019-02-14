package caceresenzo.apps.boxplay.application;

import java.util.Locale;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;
import caceresenzo.android.libs.javascript.AndroidJavaScriptExecutorLibrary;
import caceresenzo.android.libs.uncaughtexceptionhandler.AndroidUncaughtExceptionHandler;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.helper.LocaleHelper;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;

public class BoxPlayApplication extends Application {
	
	/* Set Build as Debug */
	public static final boolean BUILD_DEBUG = true;
	
	/* Constants */
	private static final Version VERSION = new Version("3.1.19", VersionType.BETA);
	
	/* Instance */
	private static BoxPlayApplication APPLICATION;
	
	/* Statics */
	private static BaseBoxPlayActivty ATTACHED_ACTIVITY;
	
	/* Managers */
	private static Handler HANDLER = new Handler();
	private static XManagers MANAGERS = new XManagers();
	private static ViewHelper HELPER = new ViewHelper();
	
	/* Preferences */
	private SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		APPLICATION = this;
		
		HELPER.prepareCache(this);
		
		setLocale();
		
		new AndroidUncaughtExceptionHandler.Builder(getApplicationContext()) //
				.setHandlerEnabled(sharedPreferences.getBoolean(getString(R.string.boxplay_other_settings_application_pref_crash_reporter_key), true)) //
				.setTrackActivitiesEnabled(true) //
				.setBackgroundModeEnabled(true) //
				.addCommaSeparatedEmailAddresses("caceresenzo1502@gmail.com") //
				.build();
		
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		
		MANAGERS.initialize(this);
		
		// BoxPlayForegroundService.startIfNotAlready(this);
		
		AndroidJavaScriptExecutorLibrary.use(this, HANDLER);
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
		
		/* Just to be sure */
		AndroidJavaScriptExecutorLibrary.use(this, HANDLER);
	}
	
	public static void attachActivity(BaseBoxPlayActivty baseBoxPlayActivty) {
		BoxPlayApplication.ATTACHED_ACTIVITY = baseBoxPlayActivty;
		
		MANAGERS.onUiReady(ATTACHED_ACTIVITY);
		
		if (ATTACHED_ACTIVITY instanceof BoxPlayActivity) {
			getViewHelper().setBoxPlayActivity((BoxPlayActivity) ATTACHED_ACTIVITY);
		}
		
		/* Just to be sure */
		AndroidJavaScriptExecutorLibrary.use(getBoxPlayApplication(), HANDLER);
	}
	
	public boolean isUiReady() {
		if (ATTACHED_ACTIVITY == null) {
			return false;
		}
		
		return ATTACHED_ACTIVITY.isReady();
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
	
	public void setLocale() {
		setLocale(false);
	}
	
	@SuppressWarnings("deprecation")
	public void setLocale(boolean autoReCache) {
		final Resources resources = getResources();
		final Configuration configuration = resources.getConfiguration();
		final Locale locale = getLocale();
		if (!configuration.locale.equals(locale)) {
			configuration.setLocale(locale);
			resources.updateConfiguration(configuration, null);
			
			try {
				Configuration config = getBaseContext().getResources().getConfiguration();
				config.setLocale(locale);
				createConfigurationContext(config);
			} catch (Exception exception) {
				; // Unavailable in old API
			}
			
			if (autoReCache) {
				getViewHelper().recache();
			}
		}
	}
	
	public String getLocaleString() {
		return sharedPreferences.getString(getString(R.string.boxplay_other_settings_application_pref_language_key), getString(R.string.boxplay_other_settings_application_pref_language_default_value)).toLowerCase();
	}
	
	public Locale getLocale() {
		return new Locale(getLocaleString());
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
	
	/**
	 * Get the {@link ViewHelper} instance
	 */
	public static ViewHelper getViewHelper() {
		return HELPER;
	}
	
	/**
	 * Get the {@link XManagers} instance
	 */
	public static XManagers getManagers() {
		return MANAGERS;
	}
	
	/**
	 * Get the main {@link Handler} instance
	 */
	public static Handler getHandler() {
		return HANDLER;
	}
	
	/**
	 * Get BoxPlay version
	 */
	public static Version getVersion() {
		return VERSION;
	}
	
	/**
	 * Get main application instance
	 */
	public static BoxPlayApplication getBoxPlayApplication() {
		return APPLICATION;
	}
	
}