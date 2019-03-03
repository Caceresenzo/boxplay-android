package caceresenzo.apps.boxplay.fragments.other;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import caceresenzo.android.libs.application.ApplicationUtils;
import caceresenzo.android.libs.internet.AdmAndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.implementations.ApplicationHelper;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ImageHelper;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.libs.licencekey.LicenceKey;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	/* Static */
	public static boolean PREF_MENU_EXPAND_COLLAPSE_BACK_BUTTON_ENABLE = true;
	
	/* Manager */
	private BoxPlayApplication boxPlayApplication;
	private ApplicationHelper applicationHelper;
	private CacheHelper cacheHelper;
	private ImageHelper imageHelper;
	private XManagers managers;
	
	/* Variables */
	private SharedPreferences sharedPreferences;
	
	private boolean initialization;
	
	private String lastPreferenceKey;
	
	/* Constructor */
	public SettingsFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.applicationHelper = boxPlayApplication.getApplicationHelper();
		this.cacheHelper = boxPlayApplication.getCacheHelper();
		this.imageHelper = boxPlayApplication.getImageHelper();
		this.managers = boxPlayApplication.getManagers();
		
		this.initialization = true;
	}
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		
		sharedPreferences = managers.getPreferences();
		
		int[] keysStringIds = {				
				/* Downloads */
				R.string.boxplay_other_settings_downloads_pref_use_adm_key, //
				
				/* BoxPlay */
				R.string.boxplay_other_settings_boxplay_pref_background_service_key, //
				R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_key, //
				
				/* Premium */
				R.string.boxplay_other_settings_premium_pref_premium_key_key, //
				
				/* Debug */
				R.string.boxplay_other_settings_debug_pref_extractor_show_logs_key, //
				
				/* Menu */
				R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_key, //
				
				/* Application */
				R.string.boxplay_other_settings_application_pref_language_key, //
				R.string.boxplay_other_settings_application_pref_crash_reporter_key //
		};
		
		for (int keyStringId : keysStringIds) {
			onSharedPreferenceChanged(sharedPreferences, getString(keyStringId));
		}
		
		initialization = false;
		
		initializeButton();
	}
	
	private void initializeButton() {
		final Preference clearImageCacheButton = findPreference(getString(R.string.boxplay_other_settings_application_pref_clear_image_cache_key));
		clearImageCacheButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					imageHelper.clearImageCache();
					preference.setSummary(getString(R.string.boxplay_other_settings_application_pref_clear_image_cache_summary_done));
				} catch (Exception exception) {
					preference.setSummary(getString(R.string.boxplay_other_settings_application_pref_clear_image_cache_summary_error, exception.getLocalizedMessage()));
				}
				return false;
			}
		});
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);
		
		if (preference == null) {
			return;
		}
		
		if (!initialization) {
			lastPreferenceKey = key;
		}
		
		if (preference instanceof SwitchPreference) {
			SwitchPreference switchPreference = (SwitchPreference) preference;
			
			if (key.equals(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_key))) {
				if (switchPreference.isChecked()) {
					preference.setSummary(R.string.boxplay_other_settings_boxplay_pref_background_service_summary_enabled);
				} else {
					preference.setSummary(R.string.boxplay_other_settings_boxplay_pref_background_service_summary_disabled);
				}
				
				if (!initialization) {
					managers.getBackgroundServiceManager().updateEnabledState(switchPreference.isChecked(), true);
				}
			}
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_application_pref_crash_reporter_key))) {
				if (switchPreference.isChecked()) {
					preference.setSummary(R.string.boxplay_other_settings_application_pref_crash_reporter_summary_enabled);
				} else {
					preference.setSummary(R.string.boxplay_other_settings_application_pref_crash_reporter_summary_disabled);
				}
			}
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_debug_pref_extractor_show_logs_key))) {
				if (switchPreference.isChecked()) {
					preference.setSummary(R.string.boxplay_other_settings_debug_pref_extractor_show_logs_summary_enabled);
				} else {
					preference.setSummary(R.string.boxplay_other_settings_debug_pref_extractor_show_logs_summary_disabled);
				}
				
				managers.getDebugManager().updatePreferences();
			}
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_downloads_pref_use_adm_key))) {
				if (!applicationHelper.isAdmInstalled()) {
					switchPreference.setChecked(false);
					switchPreference.setSummary(R.string.boxplay_other_settings_downloads_pref_use_adm_summary_not_installed);
					
					if (!initialization) {
						ApplicationUtils.openStore(boxPlayApplication, AdmAndroidDownloader.NORMAL_VERSION_PACKAGE);
					}
				} else {
					if (switchPreference.isChecked()) {
						preference.setSummary(R.string.boxplay_other_settings_downloads_pref_use_adm_summary_enabled);
					} else {
						preference.setSummary(R.string.boxplay_other_settings_downloads_pref_use_adm_summary_disabled);
					}
				}
			}
		} else if (preference instanceof CheckBoxPreference) {
			CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
			
			if (key.equals(getString(R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_key))) {
				if (checkBoxPreference.isChecked()) {
					checkBoxPreference.setSummary(R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_summary_enabled);
				} else {
					checkBoxPreference.setSummary(R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_summary_disabled);
				}
			}
		} else if (preference instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
			
			if (key.equals(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_key))) {
				preference.setSummary(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_summary, listPreference.getEntries()[prefIndex]));
				
				if (!initialization) {
					managers.getBackgroundServiceManager().updateExecutionFrequency((String) listPreference.getEntryValues()[prefIndex], true);
				}
			}
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_application_pref_language_key))) {
				if (prefIndex >= 0) {
					preference.setSummary(getString(R.string.boxplay_other_settings_application_pref_language_summary, listPreference.getEntries()[prefIndex]));
				}
				
				if (!initialization) {
					LocaleHelper.setLocale(getActivity(), sharedPreferences.getString(getString(R.string.boxplay_other_settings_application_pref_language_key), getString(R.string.boxplay_other_settings_application_pref_language_default_value)).toLowerCase());
					cacheHelper.recache();
					
					if (boxPlayApplication.getAttachedActivity() instanceof BoxPlayActivity) { // Not null AND good instance
						boxPlayApplication.getAttachedActivity().askRecreate(this);
					}
				}
			}
		} else if (preference instanceof EditTextPreference) {
			EditTextPreference editTextPreference = (EditTextPreference) preference;
			
			if (key.equals(getString(R.string.boxplay_other_settings_premium_pref_premium_key_key))) {
				LicenceKey licenceKey = LicenceKey.fromString(editTextPreference.getText());
				
				if (editTextPreference.getText() == null || editTextPreference.getText().isEmpty()) {
					editTextPreference.setSummary(getString(R.string.boxplay_other_settings_premium_pref_premium_key_summary_no_key));
				} else {
					if (licenceKey.verify().isValid()) {
						editTextPreference.setSummary(getString(R.string.boxplay_other_settings_premium_pref_premium_key_summary_valid, licenceKey.getKey()));
					} else {
						editTextPreference.setSummary(getString(R.string.boxplay_other_settings_premium_pref_premium_key_summary_invalid, licenceKey.getKey()));
					}
				}
				
				managers.getPremiumManager().updateLicence(licenceKey);
				
				editTextPreference.setText(licenceKey.getKey());
			}
		} else {
			try {
				preference.setSummary(sharedPreferences.getString(key, ""));
			} catch (Exception exception) {
				preference.setSummary(exception.getLocalizedMessage());
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public void reset() {
		initialization = true;
	}
	
	public String getLastPreferenceKey() {
		return lastPreferenceKey;
	}
	
}