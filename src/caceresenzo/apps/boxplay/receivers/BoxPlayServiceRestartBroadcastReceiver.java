package caceresenzo.apps.boxplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import caceresenzo.apps.boxplay.services.BoxPlayBackgroundService;

public class BoxPlayServiceRestartBroadcastReceiver extends BroadcastReceiver {
	
	public static final String RESTART_ACTION = "caceresenzo.apps.boxplay.reveivers.BoxPlayServiceRestartBroadcastReceiver.RESTART_SERVICE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		start(context);
	}
	
	public static void start(Context context) {
		Intent intent = new Intent(context, BoxPlayBackgroundService.class);
		if (Build.VERSION.SDK_INT < 26) {
			context.startService(intent);
		} else {
			context.startForegroundService(intent);
		}
	}
	
}