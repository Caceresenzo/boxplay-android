package caceresenzo.apps.boxplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;

public class BoxPlayServiceBroadcastReceiver extends BroadcastReceiver {
	
	/* Tag */
	public static final String TAG = BoxPlayServiceBroadcastReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			BoxPlayApplication.getBoxPlayApplication().getManagers().getBackgroundServiceManager().scheduleService();
		}
		
		BoxPlayForegroundService.startIfNotAlready(context);
	}
	
}