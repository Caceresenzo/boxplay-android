package caceresenzo.apps.boxplay.services.tasks;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.libs.thread.implementations.WorkerThread;

public abstract class ForegroundTask extends WorkerThread {
	
	/* Tag */
	public static final String TAG = ForegroundTask.class.getSimpleName();
	
	/* Constants */
	public static final int RANDOM_ID = -100;
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	protected Handler handler;
	protected XManagers managers;
	
	protected ForegroundTaskExecutor foregroundTaskExecutor;
	
	protected NotificationManager notificationManager;
	
	/* Constructor */
	public ForegroundTask() {
		super();
		
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.managers = boxPlayApplication.getManagers();
		
		this.notificationManager = (NotificationManager) boxPlayApplication.getSystemService(Context.NOTIFICATION_SERVICE);
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
	
	/** @return A new instance of the {@link NotificationCompat.Builder}. */
	protected NotificationCompat.Builder createNotificationBuilder() {
		return new NotificationCompat.Builder(boxPlayApplication, BoxPlayForegroundService.SEARCH_AND_GO_UPDATE_NOTIFICATION_CHANNEL);
	}
	
	/**
	 * Send a notification.
	 * 
	 * @param id
	 *            Target notification id.<br>
	 *            If you put {@link #RANDOM_ID}, a random id will be chosen from the {@link System#currentTimeMillis()}.
	 * @param builder
	 *            {@link Builder} used to create the notification.
	 */
	protected void notificate(int id, NotificationCompat.Builder builder) {
		if (id == RANDOM_ID) {
			id = (int) System.currentTimeMillis();
		}
		
		notificationManager.notify(id, builder.build());
	}
	
	/** @return A {@link String} corresponding to the task name. */
	@StringRes
	public abstract String getTaskName();
	
	/** @return A {@link String} corresponding to the task description. */
	@StringRes
	public abstract String getTaskDescription();
	
	/** @return Weather or not the task need an internet connection to work. (<code>true</code> by default) */
	public boolean requireNetwork() {
		return true;
	}
	
	/** @see {@link Application#getString(int)} */
	public final String getString(int resId) {
		return boxPlayApplication.getString(resId);
	}
	
	/** @see {@link Application#getString(int, Object...)} */
	public final String getString(int resId, Object... formatArgs) {
		return boxPlayApplication.getString(resId, formatArgs);
	}
	
}