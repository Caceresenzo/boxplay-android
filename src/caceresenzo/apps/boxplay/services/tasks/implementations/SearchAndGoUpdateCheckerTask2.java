package caceresenzo.apps.boxplay.services.tasks.implementations;

import caceresenzo.apps.boxplay.R;
import caceresenzo.libs.thread.ThreadUtils;

public class SearchAndGoUpdateCheckerTask2 extends SearchAndGoUpdateCheckerTask {
	
	private int maxSecond = 5;
	
	@Override
	protected void task() {
		for (int second = 0; second < maxSecond; second++) {
			publishProgress(maxSecond, second + 1);
			
			ThreadUtils.sleep(1000L);
			
			checkThread();
		}
	}
	
	@Override
	public int getTaskName() {
		return R.string.boxplay_service_foreground_task_searchngo_subscriptions2_title;
	}
	
}