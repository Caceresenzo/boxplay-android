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

public class BackgroundServiceManager extends AbstractManager implements Constants.BROADCAST_ID {
	
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
		
		updateLocals();
		checkStatus();
	}
	
	/**
	 * Update the loaded values of the manager.
	 */
	public void updateLocals() {
		updateEnabledState(getManagers().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_key), true), false);
		updateExecutionFrequency(getManagers().getPreferences().getString(getString(R.string.boxplay_other_settings_boxplay_pref_background_service_frequency_key), String.valueOf(Constants.MANAGER.BACKGROUND_SERVICE_DEFAULT_FREQUENCY)), false);
	}
	
	public void checkStatus() {
		if (!shouldServiceBeRunning()) {
			return;
		}
		
		if (!isServiceRunning()) {
			Log.w(TAG, "Service not running (and should be), rescheduling...");
			
			rescheduleService();
		}
	}
	
	/**
	 * Reschedule the alarm wakeup for the Service.<br>
	 * If the service is not supposed to run, the {@link #scheduleService()} function will do nothing.
	 * 
	 * @see #cancelScheduledService()
	 * @see #scheduleService()
	 */
	public void rescheduleService() {
		cancelScheduledService();
		scheduleService();
	}
	
	/**
	 * Schedule the service.<br>
	 * It will be called on the actually loaded interval.<br>
	 * If the service is not supposed to run, the function will not do anything.
	 */
	public void scheduleService() {
		if (!shouldServiceBeRunning()) {
			return;
		}
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime() + serviceExecutionFrequency, serviceExecutionFrequency, createPendingIntent());
		
		Log.i(TAG, "Scheduled service. (frequency: every " + serviceExecutionFrequency + "s)");
	}
	
	/**
	 * Cancel (if any) pending intent used for the Service.
	 */
	public void cancelScheduledService() {
		PendingIntent pendingIntent = createPendingIntent();
		
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
		
		Log.i(TAG, "Cancelled scheduled service.");
	}
	
	/**
	 * @return Weather or not an alarm is already programmed or not.
	 */
	public boolean isServiceRunning() {
		return createPendingIntent(false) != null;
	}
	
	/**
	 * @return {@link Intent} for the {@link BoxPlayServiceBroadcastReceiver} wake-up.
	 */
	private Intent createIntent() {
		return new Intent(boxPlayApplication, BoxPlayServiceBroadcastReceiver.class);
	}
	
	/**
	 * @return A <code>FLAG_UPDATE_CURRENT</code> {@link PendingIntent} for the {@link AlarmManager}.
	 * @see #createPendingIntent(boolean)
	 */
	private PendingIntent createPendingIntent() {
		return createPendingIntent(true);
	}
	
	/**
	 * Create a {@link PendingIntent} that should be used with the {@link AlarmManager} to schedule or cancel the alarm.
	 * 
	 * @param updateCurrent
	 *            If the flag {@link PendingIntent#FLAG_UPDATE_CURRENT} shoud be use.<br>
	 *            If not, the flag {@link PendingIntent#FLAG_NO_CREATE} will be used.
	 * 
	 * @return {@link PendingIntent} that will be used to schedule or cancel the next execution.
	 */
	private PendingIntent createPendingIntent(boolean updateCurrent) {
		return PendingIntent.getBroadcast(boxPlayApplication, BOXPLAY_ALARM_SERVICE, createIntent(), updateCurrent ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_NO_CREATE);
	}
	
	/**
	 * Set a new enabled state for the service to run.
	 * 
	 * @param newState
	 *            New enabled state.
	 * @param reschedule
	 *            If the function {@link #rescheduleService()} should be called.
	 */
	public void updateEnabledState(boolean newState, boolean reschedule) {
		this.serviceEnabledState = newState;
		
		if (reschedule) {
			rescheduleService();
		}
	}
	
	/**
	 * Set a new execution frequency in second.
	 * 
	 * @param frequency
	 *            New execution frequency as a long string.
	 * @param reschedule
	 *            If the function {@link #rescheduleService()} should be called.
	 * @throws NumberFormatException
	 *             If the <code>frequency</code> string failed to be parsed as a {@link Long} value.
	 */
	public void updateExecutionFrequency(String frequency, boolean reschedule) {
		this.serviceExecutionFrequency = Long.valueOf(frequency);
		
		if (reschedule) {
			rescheduleService();
		}
	}
	
	/**
	 * @return Actually loaded execution frequency.
	 */
	public long getExecutionFrequency() {
		return serviceExecutionFrequency;
	}
	
	/**
	 * @return Weather the service should be running or not.
	 */
	public boolean shouldServiceBeRunning() {
		return serviceEnabledState;
	}
	
}