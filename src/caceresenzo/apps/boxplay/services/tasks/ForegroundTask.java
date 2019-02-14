package caceresenzo.apps.boxplay.services.tasks;

import android.support.annotation.StringRes;
import android.util.Log;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.libs.thread.implementations.WorkerThread;

public abstract class ForegroundTask extends WorkerThread {
	
	/* Tag */
	public static final String TAG = ForegroundTask.class.getSimpleName();
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	protected XManagers managers;
	
	protected ForegroundTaskExecutor foregroundTaskExecutor;
	
	public ForegroundTask() {
		super();
		
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.managers = BoxPlayApplication.getManagers();
	}
	
	@Override
	protected void execute() {
		Log.i(TAG, "Starting foreground task: " + getClass().getSimpleName());
		
		try {
			task();
		} catch (ThreadDeath threadDeath) {
			Log.i(TAG, "Task forced to stop.");
		} catch (Throwable throwable) {
			Log.w(TAG, "Unhandled task error.", throwable);
		}
	}
	
	/**
	 * Executed in a "safe" block (try-catch).<br>
	 * All task action should be done in this function.<br>
	 * You also need to call {@link #checkThread()} often (like at every loop) to check if the {@link #shouldStop()} function has been call or not.
	 */
	protected abstract void task();
	
	/**
	 * Call this function often to ensure to throw an exception that you should not handle, used to stop execution if the thread should stop as soon as possible.
	 * 
	 * @throws ThreadDeath
	 *             If the {@link #threadShouldStop()} return <code>true</code>.
	 */
	protected void checkThread() {
		if (threadShouldStop()) {
			throw new ThreadDeath();
		}
	}
	
	/**
	 * Start the {@link ForegroundTask} with a {@link ForegroundTaskExecutor} attached to it.
	 * 
	 * @param executor
	 *            Target executor.
	 * @return Itself
	 */
	public ForegroundTask start(ForegroundTaskExecutor executor) {
		this.foregroundTaskExecutor = executor;
		
		start();
		
		return this;
	}
	
	/**
	 * @return A string ressource id corresponding to the task name.
	 */
	@StringRes
	public abstract int getTaskName();
	
	/**
	 * @return A string ressource id corresponding to the task description.
	 */
	@StringRes
	public abstract int getTaskDescription();
	
	/**
	 * @return Weather or not the task need an internet connection to work. (<code>true</code> by default)
	 */
	public boolean requireNetwork() {
		return true;
	}
	
}