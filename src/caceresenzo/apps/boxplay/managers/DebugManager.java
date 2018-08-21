package caceresenzo.apps.boxplay.managers;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;

/**
 * Manager used for debug stuff, like video extraction logs
 * 
 * @author Enzo CACERES
 */
public class DebugManager extends AbstractManager {
	
	private boolean prefOpenLogsAtExtractorEnd = false;
	
	@Override
	protected void initialize() {
		updatePreferences();
	}
	
	/**
	 * When a preferences, related to extraction, change, call this function to update cached value
	 */
	public void updatePreferences() {
		this.prefOpenLogsAtExtractorEnd = getManagers().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_debug_pref_extractor_show_logs_key), false);
	}
	
	/**
	 * Preference, return true of false if the extractor logs should be open at the end of the extraction
	 * 
	 * @return Should open logs
	 */
	public boolean openLogsAtExtractorEnd() {
		return prefOpenLogsAtExtractorEnd;
	}
	
}