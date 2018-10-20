package caceresenzo.apps.boxplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import caceresenzo.android.libs.task.AlarmManagerWrapper;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class BoxPlayBackgroundTaskReceiver extends BroadcastReceiver {
	
	/* Tag */
	public static final String TAG = BoxPlayBackgroundTaskReceiver.class.getSimpleName();
	
	/* Alarm Manager Wrapper */
	public static final AlarmManagerWrapper ALARM_MANAGER_WRAPPER = new AlarmManagerWrapper();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ALARM_MANAGER_WRAPPER.context(context);
		
		if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			ALARM_MANAGER_WRAPPER.cancel(getClass());
		}
		
		execute();
		
		ALARM_MANAGER_WRAPPER.schedule(BoxPlayApplication.getManagers().getBackgroundServiceManager().getExecutionFrequency(), getClass());
	}
	
	/**
	 * Called when an alarm has been received
	 */
	public void execute() {
		WorkerThread[] tasks = { new SearchAndGoUpdateCheckerTask() };
		
		for (WorkerThread task : tasks) {
			task.start();
		}
	}
	
	class SearchAndGoUpdateCheckerTask extends WorkerThread {
		@Override
		protected void execute() {
			Log.d(TAG, "Executed SearchAndGoUpdateCheckerTask");
		}
	}
	
	// public static void initializeClock(Context context) {
	// PendingIntent sender = PendingIntent.getBroadcast(context, 2, new Intent(context, BoxPlayBackgroundTaskAlarmReceiver.class), 0);
	// AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime(), 5000, sender);
	// }
	
	public static void firstInitialization() {
		
	}
	
}