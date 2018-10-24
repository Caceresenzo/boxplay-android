package caceresenzo.apps.boxplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import caceresenzo.android.libs.task.AlarmManagerWrapper;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;

public class BoxPlayServiceBroadcastReceiver extends BroadcastReceiver {
	
	/* Tag */
	public static final String TAG = BoxPlayServiceBroadcastReceiver.class.getSimpleName();
	
	/* Alarm Manager Wrapper */
	public static final AlarmManagerWrapper ALARM_MANAGER_WRAPPER = new AlarmManagerWrapper();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// ALARM_MANAGER_WRAPPER.context(context);
		//
		// if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
		// ALARM_MANAGER_WRAPPER.cancel(getClass());
		// }
		//
		// BoxPlayForegroundService.startIfNotAlready(context);
		//
		// ALARM_MANAGER_WRAPPER.schedule(BoxPlayApplication.getManagers().getBackgroundServiceManager().getExecutionFrequency(), getClass());
	}
	
}