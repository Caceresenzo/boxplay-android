package caceresenzo.apps.boxplay.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import caceresenzo.android.libs.service.ServiceUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.libs.math.MathUtils;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class BoxPlayForegroundService extends Service {
	
	/* Tag */
	public static final String TAG = BoxPlayForegroundService.class.getSimpleName();
	
	/* Constants Shotcuts */
	public static final int NOTIFICATION_ID = Constants.NOTIFICATION_ID.BOXPLAY_FOREGROUND_SERVICE;
	public static final String NOTIFICATION_CHANNEL = Constants.NOTIFICATION_CHANNEL.ANDROID_CHANNEL_ID;
	
	/* Constants */
	public static final int INDETERMINATE_PROGRESS = -1;
	
	public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
	public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
	
	public static final String ACTION_CANCEL = "ACTION_CANCEL";
	
	/* Variables */
	private ForegroundTask actualForegroundTask;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "My foreground service onCreate().");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			
			switch (action) {
				case ACTION_START_FOREGROUND_SERVICE: {
					startForegroundService();
					Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
					break;
				}
				
				case ACTION_CANCEL:
				case ACTION_STOP_FOREGROUND_SERVICE: {
					stopForegroundService();
					Toast.makeText(getApplicationContext(), action.equals(ACTION_CANCEL) ? "You click cancel button." : "Foreground service is stopped.", Toast.LENGTH_LONG).show();
					break;
				}
				
				default: {
					break;
				}
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	/* TODO: Return the communication channel to the service. */
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Used to build and start foreground service
	 */
	private void startForegroundService() {
		Log.d(TAG, "Start foreground service.");
		ServiceUtils.createNotificationChannel(this, NOTIFICATION_CHANNEL, R.string.boxplay_notification_channel_main_title, R.string.boxplay_notification_channel_main_description);
		
		startForeground(NOTIFICATION_ID, createNotification(INDETERMINATE_PROGRESS));
		
		execute();
	}
	
	/**
	 * Used to completly stop foreground service
	 */
	private void stopForegroundService() {
		Log.d(TAG, "Stop foreground service.");
		
		stopForeground(true);
		
		stopSelf();
	}
	
	@SuppressWarnings("deprecation")
	private Notification createNotification(int progress) {
		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
		
		if (actualForegroundTask != null) {
			NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
			bigTextStyle.setBigContentTitle(getString(R.string.boxplay_service_foreground_task_actual, getString(actualForegroundTask.getTaskName())));
			bigTextStyle.bigText(getString(actualForegroundTask.getTaskDescription()));
			builder.setStyle(bigTextStyle);
		}
		
		builder.setContentTitle(getString(R.string.boxplay_service_foreground_notification_title));
		builder.setContentText(getString(R.string.boxplay_service_foreground_notification_eta));
		builder.setProgress(100, progress, progress == INDETERMINATE_PROGRESS);
		builder.setOnlyAlertOnce(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setPriority(NotificationCompat.PRIORITY_MAX);
		builder.setFullScreenIntent(pendingIntent, true);
		
		Intent cancelIntent = new Intent(this, BoxPlayForegroundService.class);
		cancelIntent.setAction(ACTION_CANCEL);
		PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, cancelIntent, 0);
		NotificationCompat.Action cancelAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, getString(R.string.boxplay_service_foreground_notification_button_cancel), pendingPrevIntent);
		builder.addAction(cancelAction);
		
		Notification notification = builder.build();
		
		notification.vibrate = null;
		notification.vibrate = null;
		notification.defaults &= ~Notification.DEFAULT_SOUND;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		
		return notification;
	}
	
	private void updateNotification(int progress) {
		Notification notification = createNotification(progress);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	/**
	 * Execute tasks
	 */
	public void execute() {
		final ForegroundTask[] tasks = { new SearchAndGoUpdateCheckerTask(), new SearchAndGoUpdateCheckerTask2(), new SearchAndGoUpdateCheckerTask() };
		
		new WorkerThread() {
			private Handler handler;
			
			@Override
			protected void initialize() {
				this.handler = new Handler(getMainLooper());
				
				publishUpdate(INDETERMINATE_PROGRESS);
			}
			
			@Override
			public void execute() {
				for (int i = 0; i < tasks.length; i++) {
					ForegroundTask task = actualForegroundTask = tasks[i];
					
					publishUpdate(INDETERMINATE_PROGRESS);
					
					task.observe(new WorkerThread.ProgressObserver() {
						@Override
						public void onProgress(WorkerThread worker, int max, int value) {
							publishUpdate((int) MathUtils.pourcent(value, max));
						}
					});
					
					task.start();
					
					try {
						task.join();
					} catch (InterruptedException exception) {
						Log.i(TAG, "Failed to join thread.", exception);
					}
					
					task.removeObserver();
					
					/* Not the last */
					if (i != tasks.length) {
						publishUpdate(INDETERMINATE_PROGRESS);
						
						ThreadUtils.sleep(1000L);
					}
				}
			}
			
			public void publishUpdate(final int progress) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						updateNotification(progress);
					}
				});
			}
			
			@Override
			protected void done() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						stopForegroundService();
					}
				});
			};
			
		}.start();
	}
	
	class SearchAndGoUpdateCheckerTask extends ForegroundTask {
		private int maxSecond = 5;
		
		@Override
		protected void execute() {
			Log.d(TAG, "Executed SearchAndGoUpdateCheckerTask");
			
			for (int second = 0; second < maxSecond; second++) {
				publishProgress(maxSecond, second + 1);
				
				ThreadUtils.sleep(1000L);
			}
		}
		
		@Override
		public int getTaskName() {
			return R.string.boxplay_service_foreground_task_searchngo_subscriptions_title;
		}
		
		@Override
		public int getTaskDescription() {
			return R.string.boxplay_service_foreground_task_searchngo_subscriptions_description;
		}
	}
	
	class SearchAndGoUpdateCheckerTask2 extends SearchAndGoUpdateCheckerTask {
		private int maxSecond = 5;
		
		@Override
		protected void execute() {
			Log.d(TAG, "Executed SearchAndGoUpdateCheckerTask2");
			
			for (int second = 0; second < maxSecond; second++) {
				publishProgress(maxSecond, second + 1);
				
				ThreadUtils.sleep(1000L);
			}
		}
		
		@Override
		public int getTaskName() {
			return R.string.boxplay_service_foreground_task_searchngo_subscriptions2_title;
		}
	}
	
	abstract class ForegroundTask extends WorkerThread {
		
		@StringRes
		public abstract int getTaskName();
		
		@StringRes
		public abstract int getTaskDescription();
		
	}
	
	public static Intent getServiceIntent(Context context) {
		return new Intent(context, BoxPlayForegroundService.class);
	}
	
	public static boolean isRunning(Context context) {
		return ServiceUtils.isServiceRunning(context, BoxPlayForegroundService.class);
	}
	
	public static void startIfNotAlready(Context context) {
		if (!isRunning(context)) {
			Intent intent = getServiceIntent(context);
			intent.setAction(ACTION_START_FOREGROUND_SERVICE);
			executeServiceIntent(context, intent);
		}
	}
	
	public static void stopIfNotAlready(Context context) {
		if (isRunning(context)) {
			Intent intent = getServiceIntent(context);
			intent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
			executeServiceIntent(context, intent);
		}
	}
	
	private static void executeServiceIntent(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}
	
}