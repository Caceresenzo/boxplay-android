package caceresenzo.apps.boxplay.fragments.other;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.EditText;
import caceresenzo.android.libs.application.ApplicationUtils;
import caceresenzo.android.libs.internet.AdmAndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.implementations.ApplicationHelper;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ImageHelper;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.managers.PremiumManager;
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
				R.string.boxplay_other_settings_premium_pref_premium_locked_key, //
				R.string.boxplay_other_settings_premium_pref_premium_key_key, //
				R.string.boxplay_other_settings_premium_pref_premium_hiding_key, //
				
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
		
		initializeSpecifics();
		initializeButton();
	}
	
	private void initializeSpecifics() {
		final SwitchPreference lockingSwitchPreference = (SwitchPreference) findPreference(getString(R.string.boxplay_other_settings_premium_pref_premium_locked_key));
		
		lockingSwitchPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final PremiumManager premiumManager = managers.getPremiumManager();
				
				final EditText editText = new EditText(boxPlayApplication.getAttachedActivity());
				
				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(boxPlayApplication.getAttachedActivity()) //
						.setTitle(R.string.boxplay_other_settings_premium_pref_premium_locked_dialog_title) //
						.setView(editText) //
				;
				
				if (premiumManager.isPremiumLocked()) {
					alertDialogBuilder.setPositiveButton(R.string.boxplay_other_settings_premium_pref_premium_locked_dialog_button_unlock, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (premiumManager.tryToUnlock(editText.getText().toString())) {
								lockingSwitchPreference.setChecked(false);
							} else {
								lockingSwitchPreference.setChecked(true);
								boxPlayApplication.toast(R.string.boxplay_other_settings_premium_pref_premium_locked_error_wrong_password).show();
							}
							
							premiumManager.updateDrawer();
						}
					});
				} else {
					alertDialogBuilder.setPositiveButton(R.string.boxplay_other_settings_premium_pref_premium_locked_dialog_button_lock, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (premiumManager.lock(editText.getText().toString())) {
								lockingSwitchPreference.setChecked(true);
							} else {
								lockingSwitchPreference.setChecked(false);
								boxPlayApplication.toast(R.string.boxplay_other_settings_premium_pref_premium_locked_error_already_locked).show();
							}
							
							premiumManager.updateDrawer();
						}
					});
				}
				
				alertDialogBuilder.show();
				
				premiumManager.updateDrawer();
				return false;
			}
		});
		
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
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_premium_pref_premium_locked_key))) {
				boolean locked = managers.getPremiumManager().isPremiumLocked();
				
				if (locked) {
					preference.setSummary(R.string.boxplay_other_settings_premium_pref_premium_locked_summary_locked);
				} else {
					preference.setSummary(R.string.boxplay_other_settings_premium_pref_premium_locked_summary_unlocked);
				}

				findPreference(getString(R.string.boxplay_other_settings_premium_pref_premium_hiding_key)).setEnabled(!locked);
				findPreference(getString(R.string.boxplay_other_settings_premium_pref_premium_key_key)).setEnabled(!locked);
			}
			//
			else if (key.equals(getString(R.string.boxplay_other_settings_premium_pref_premium_hiding_key))) {
				PremiumManager premiumManager = managers.getPremiumManager();
				
				boolean keyIsValid = premiumManager.isPremiumKeyValid();
				int summary = 0;
				
				if (keyIsValid) {
					if (premiumManager.isPremiumHidden()) {
						summary = R.string.boxplay_other_settings_premium_pref_premium_hiding_summary_hidden;
					} else {
						summary = R.string.boxplay_other_settings_premium_pref_premium_hiding_summary_visible;
					}
				} else {
					summary = R.string.boxplay_other_settings_premium_pref_premium_hiding_summary_invalid;
					switchPreference.setChecked(false);
				}
				
				preference.setEnabled(keyIsValid && !premiumManager.isPremiumLocked());
				preference.setSummary(summary);
				
				premiumManager.updateDrawer();
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
				onSharedPreferenceChanged(sharedPreferences, getString(R.string.boxplay_other_settings_premium_pref_premium_hiding_key));
				
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