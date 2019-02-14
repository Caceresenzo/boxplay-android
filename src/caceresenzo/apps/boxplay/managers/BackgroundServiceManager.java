package caceresenzo.apps.boxplay.managers;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.receivers.BoxPlayServiceBroadcastReceiver;

public class BackgroundServiceManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = BackgroundServiceManager.class.getSimpleName();
	
	/* Constants */
	public static final String ALARM_NAME = "boxplay_service_alarm";
	
	/* Managers */
	private AlarmManager alarmManager;
	
	/* Variables */
	private long serviceExecutionFrequency;
	private boolean serviceEnabledState;
	
	@Override
	protected void initialize() {
		this.alarmManager = (AlarmManager) boxPlayApplication.getSystemService(Context.ALARM_SERVICE);
		
		updateEnabledState(getManagers().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_key), true), false);
		updateExecutionFrequency(getManagers().getPreferences().getString(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_key), String.valueOf(Constants.MANAGER.BACKGROUND_SERVICE_DEFAULT_FREQUENCY)), false);
	}
	
	public void rescheduleService() {
		cancelScheduledService();
		
		if (serviceEnabledState) {
			scheduleService();
		}
	}
	
	public void scheduleService() {
		Intent intent = new Intent(boxPlayApplication, BoxPlayServiceBroadcastReceiver.class);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(boxPlayApplication, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime() + serviceExecutionFrequency, serviceExecutionFrequency, pendingIntent);
		
		Log.i(TAG, "Scheduled service. (frequency: every " + serviceExecutionFrequency + "s)");
	}
	
	public void cancelScheduledService() {
		alarmManager.cancel(PendingIntent.getBroadcast(boxPlayApplication, 0, new Intent(ALARM_NAME), PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public long getExecutionFrequency() {
		return serviceExecutionFrequency;
	}
	
	public void updateEnabledState(boolean newState, boolean reschedule) {
		this.serviceEnabledState = newState;
		
		if (reschedule) {
			rescheduleService();
		}
	}
	
	public void updateExecutionFrequency(String frequency, boolean reschedule) {
		this.serviceExecutionFrequency = Long.valueOf(frequency);
		
		if (reschedule) {
			rescheduleService();
		}
	}
	
}