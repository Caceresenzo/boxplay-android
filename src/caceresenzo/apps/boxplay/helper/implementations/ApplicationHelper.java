package caceresenzo.apps.boxplay.helper.implementations;

import caceresenzo.android.libs.application.ApplicationUtils;
import caceresenzo.android.libs.internet.AdmAndroidDownloader;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;

public class ApplicationHelper extends AbstractHelper {
	
	/* Constructor */
	public ApplicationHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	/**
	 * @return Weather or not the package <code>org.videolan.vlc</code> is installed.
	 */
	public boolean isVlcInstalled() {
		return ApplicationUtils.isApplicationInstalled(boxPlayApplication, "org.videolan.vlc");
	}
	
	/**
	 * Check if ADM download is enabled to replace Android default download manager.
	 * 
	 * @return ADM enabled state.
	 */
	public boolean isAdmEnabled() {
		if (!isAdmInstalled()) {
			return false;
		}
		
		try {
			return boxPlayApplication.getManagers().getPreferences().getBoolean(boxPlayApplication.getString(R.string.boxplay_other_settings_downloads_pref_use_adm_key), false);
		} catch (Exception exception) {
			return false;
		}
	}
	
	/**
	 * Check if at least the normal or pro version of ADM is installed.
	 * 
	 * @return ADM installed state
	 */
	public boolean isAdmInstalled() {
		return AdmAndroidDownloader.isAdmInstalled(boxPlayApplication) != AdmAndroidDownloader.NO_VERSION;
	}
	
}