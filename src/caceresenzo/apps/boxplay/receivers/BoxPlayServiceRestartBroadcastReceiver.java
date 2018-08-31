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
		switch (intent.getAction()) {
			case RESTART_ACTION: {
				BoxPlayBackgroundService.startIfNotAlready(context);
				break;
			}
			
			case "android.intent.action.BOOT_COMPLETED": {
				if (Build.VERSION.SDK_INT < 26) { /* Android O don't allow it anymore */
					BoxPlayBackgroundService.startIfNotAlready(context);
				}
				break;
			}
			
			default: {
				throw new IllegalStateException("Unhandled intent action: " + intent.getAction());
			}
		}
	}
	
}