package caceresenzo.apps.boxplay.services.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.util.Log;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.apps.boxplay.services.tasks.implementations.SearchAndGoUpdateCheckerTask;
import caceresenzo.apps.boxplay.services.tasks.implementations.SearchAndGoUpdateCheckerTask2;
import caceresenzo.apps.boxplay.services.tasks.implementations.UpdateCheckerTask;
import caceresenzo.libs.math.MathUtils;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class ForegroundTaskExecutor extends WorkerThread {
	
	/* Tag */
	public static final String TAG = ForegroundTaskExecutor.class.getSimpleName();
	
	/* Static */
	private static List<Class<? extends ForegroundTask>> taskClasses;
	static {
		taskClasses = new ArrayList<>();

		taskClasses.add(SearchAndGoUpdateCheckerTask.class);
		taskClasses.add(SearchAndGoUpdateCheckerTask2.class);
		taskClasses.add(UpdateCheckerTask.class);
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
				Log.i(TAG, "Executing: " + task.getClass());
				
				publishUpdate(task, BoxPlayForegroundService.INDETERMINATE_PROGRESS);
				
				task.observe(new WorkerThread.ProgressObserver() {
					@Override
					public void onProgress(WorkerThread worker, int max, int value) {
						publishUpdate(task, (int) MathUtils.pourcent(value, max));
					}
				});
				
				task.start();
				
				try {
					task.join();
				} catch (InterruptedException exception) {
					Log.i(TAG, "Failed to join thread.", exception);
				}
				
				task.removeObserver();
				
				if (threadShouldStop()) {
					break;
				}
				
				if (i != tasks.size()) { /* Not the last */
					publishUpdate(null, BoxPlayForegroundService.INDETERMINATE_PROGRESS);
					
					ThreadUtils.sleep(1000L);
				}
			}
			
			actualForegroundTask = null;
		}
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