package caceresenzo.apps.boxplay.services.tasks.implementations;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.services.tasks.ForegroundTask;
import caceresenzo.libs.thread.ThreadUtils;

/**
 * Dummy class.
 * 
 * @author Enzo CACERES
 */
public class UpdateCheckerTask extends ForegroundTask {
	
	@Override
	protected void task() {
		ThreadUtils.sleep(5000L);
		
		checkThread();
	}
	
	@Override
	public String getTaskName() {
		return getString(R.string.boxplay_service_foreground_task_application_update_title);
	}
	
	@Override
	public String getTaskDescription() {
		return getString(R.string.boxplay_service_foreground_task_application_update_description);
	}
	
}