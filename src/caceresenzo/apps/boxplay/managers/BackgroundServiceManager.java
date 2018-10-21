package caceresenzo.apps.boxplay.managers;

import android.content.Intent;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.receivers.BoxPlayServiceBroadcastReceiver;

public class BackgroundServiceManager extends AbstractManager {
	
	private long serviceExecutionFrequency;
	
	@Override
	protected void initialize() {
		/* Fake Receiving */
		new BoxPlayServiceBroadcastReceiver().onReceive(boxPlayApplication, new Intent());
		
		updateExecutionFrequency(getManagers().getPreferences().getString(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_key), String.valueOf(Constants.MANAGER.BACKGROUND_SERVICE_DEFAULT_FREQUENCY)));
	}
	
	public long getExecutionFrequency() {
		return serviceExecutionFrequency;
	}
	
	public void updateExecutionFrequency(String frequency) {
		this.serviceExecutionFrequency = Long.valueOf(frequency);
	}
	
}