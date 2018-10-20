package caceresenzo.apps.boxplay.managers;

import android.content.Intent;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.receivers.BoxPlayBackgroundTaskReceiver;

public class BackgroundServiceManager extends AbstractManager {
	
	@Override
	protected void initialize() {
		/* Fake Receiving */
		new BoxPlayBackgroundTaskReceiver().onReceive(boxPlayApplication, new Intent());
	}
	
	public long getExecutionFrequency() {
		return 5000;
	}
	
}