package caceresenzo.apps.boxplay.services.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import caceresenzo.android.libs.internet.NetworkUtils;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.apps.boxplay.services.tasks.implementations.SearchAndGoUpdateCheckerTask;
import caceresenzo.libs.math.MathUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class ForegroundTaskExecutor extends WorkerThread {
	
	/* Tag */
	public static final String TAG = ForegroundTaskExecutor.class.getSimpleName();
	
	/* Static */
	private static List<Class<? extends ForegroundTask>> taskClasses;
	static {
		taskClasses = new ArrayList<>();
		
		// taskClasses.add(UpdateCheckerTask.class);
		taskClasses.add(SearchAndGoUpdateCheckerTask.class);
	}
	
	/* Managers */
	protected BoxPlayForegroundService service;
	protected Handler handler;
	
	/* Task */
	private ForegroundTask actualForegroundTask;
	
	/* Constructor */
	public ForegroundTaskExecutor(BoxPlayForegroundService service, Handler handler) {
		this.service = service;
		this.handler = handler;
		
		publishUpdate(null, BoxPlayForegroundService.INDETERMINATE_PROGRESS);
	}
	
	@Override
	public void execute() {
		List<ForegroundTask> tasks = new ArrayList<>();
		for (Class<? extends ForegroundTask> taskClass : taskClasses) {
			try {
				tasks.add(taskClass.newInstance());
			} catch (Exception exception) {
				Log.w(TAG, "Failed to instanciate foreground task class: " + taskClass.getSimpleName());
			}
		}
		
		for (int i = 0; i < tasks.size(); i++) {
			final ForegroundTask task = actualForegroundTask = tasks.get(i);
			
			if (task != null) { /* Security... */
				Log.i(TAG, "Executing: " + task.getClass().getName());
				
				if (task.requireNetwork() && !NetworkUtils.isConnected(BoxPlayApplication.getBoxPlayApplication())) {
					Log.i(TAG, "Cancelled due to missing network.");
					continue;
				}
				
				publishUpdate(task, BoxPlayForegroundService.INDETERMINATE_PROGRESS);
				
				task.observe(new WorkerThread.ProgressObserver() {
					@Override
					public void onProgress(WorkerThread worker, int max, int value) {
						publishUpdate(task, (int) MathUtils.pourcent(value, max));
					}
				});
				
				try {
					task.start(this).join();
				} catch (InterruptedException exception) {
					Log.i(TAG, "Failed to join thread.", exception);
				}
				
				task.removeObserver();
			}
			
			if (threadShouldStop()) {
				break;
			}
		}
	}
	
	public void publishUpdate(final ForegroundTask task, int progress, int max) {
		publishUpdate(task, (int) MathUtils.pourcent(progress, max));
	}
	
	public void publishUpdate(final ForegroundTask task, final int progress) {
		if (!threadShouldStop()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					service.updateNotification(task, progress);
				}
			});
		}
	}
	
	@Override
	public void shouldStop() {
		super.shouldStop();
		
		if (actualForegroundTask != null) {
			actualForegroundTask.shouldStop();
		}
	}
	
}