package caceresenzo.apps.boxplay.helper.implementations;

import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;
import caceresenzo.apps.boxplay.helper.HelperManager;

/**
 * Manages setting of the app's locale.
 */
public class LocaleHelper extends AbstractHelper {
	
	/* Constructor */
	public LocaleHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	public void initialize(HelperManager helperManager) {
		super.initialize(helperManager);
		
		setLocale();
	}

	public static Context onAttach(Context context) {
		String locale = getPersistedLocale(context);
		return setLocale(context, locale);
	}
	
	public static String getPersistedLocale(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(context.getString(R.string.boxplay_other_settings_application_pref_language_key), "");
	}
	
	/**
	 * Set the app's locale to the one specified by the given String.
	 *
	 * @param context
	 * @param localeSpec
	 *            a locale specification as used for Android resources (NOTE: does not support country and variant codes so far); the special string "system" sets the locale to the locale specified in system settings
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Context setLocale(Context context, String localeSpec) {
		Locale locale;
		if (localeSpec.equals("system")) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				locale = Resources.getSystem().getConfiguration().getLocales().get(0);
			} else {
				// noinspection deprecation
				locale = Resources.getSystem().getConfiguration().locale;
			}
		} else {
			locale = new Locale(localeSpec);
		}
		Locale.setDefault(locale);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return updateResources(context, locale);
		} else {
			return updateResourcesLegacy(context, locale);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.N)
	private static Context updateResources(Context context, Locale locale) {
		Configuration configuration = context.getResources().getConfiguration();
		configuration.setLocale(locale);
		configuration.setLayoutDirection(locale);
		
		return context.createConfigurationContext(configuration);
	}
	
	@SuppressWarnings("deprecation")
	private static Context updateResourcesLegacy(Context context, Locale locale) {
		Resources resources = context.getResources();
		
		Configuration configuration = resources.getConfiguration();
		configuration.locale = locale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			configuration.setLayoutDirection(locale);
		}
		
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
		
		return context;
	}
	
	public void setLocale() {
		setLocale(false);
	}
	
	@SuppressWarnings("deprecation")
	public void setLocale(boolean autoReCache) {
		final Resources resources = boxPlayApplication.getResources();
		final Configuration configuration = resources.getConfiguration();
		final Locale locale = getLocale();
		if (!configuration.locale.equals(locale)) {
			configuration.setLocale(locale);
			resources.updateConfiguration(configuration, null);
			
			try {
				Configuration config = boxPlayApplication.getBaseContext().getResources().getConfiguration();
				config.setLocale(locale);
				boxPlayApplication.createConfigurationContext(config);
			} catch (Exception exception) {
				; // Unavailable in old API
			}
			
			if (autoReCache) {
				helperManager.getCacheHelper().recache();
			}
		}
	}
	
	public String getLocaleString() {
		return boxPlayApplication.getPreferences().getString(getString(R.string.boxplay_other_settings_application_pref_language_key), getString(R.string.boxplay_other_settings_application_pref_language_default_value)).toLowerCase();
	}
	
	public Locale getLocale() {
		return new Locale(getLocaleString());
	}
	
}