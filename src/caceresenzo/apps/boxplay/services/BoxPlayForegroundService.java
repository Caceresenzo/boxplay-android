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
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import caceresenzo.android.libs.service.ServiceUtils;
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.apps.boxplay.services.tasks.ForegroundTask;
import caceresenzo.apps.boxplay.services.tasks.ForegroundTaskExecutor;

public class BoxPlayForegroundService extends Service {
	
	/* Tag */
	public static final String TAG = BoxPlayForegroundService.class.getSimpleName();
	
	/* Constants Shotcuts */
	public static final int NOTIFICATION_ID = Constants.NOTIFICATION_ID.BOXPLAY_FOREGROUND_SERVICE;
	public static final String MAIN_NOTIFICATION_CHANNEL = Constants.NOTIFICATION_CHANNEL.MAIN;
	public static final String SEARCH_AND_GO_UPDATE_NOTIFICATION_CHANNEL = Constants.NOTIFICATION_CHANNEL.SEARCH_AND_GO_UPDATE;
	
	/* Constants */
	public static final int INDETERMINATE_PROGRESS = -1;
	
	public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
	public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
	
	public static final String ACTION_CANCEL = "ACTION_CANCEL";
	
	/* Managers */
	private BoxPlayApplication boxPlayApplication;
	
	/* Variables */
	private ForegroundTaskExecutor executorWorker;
	
	/* Constructor */
	public BoxPlayForegroundService() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
	}
	
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
					
					if (BoxPlayApplication.BUILD_DEBUG) {
						Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
					}
					break;
				}
				
				case ACTION_CANCEL:
				case ACTION_STOP_FOREGROUND_SERVICE: {
					stopForegroundService();
					
					if (BoxPlayApplication.BUILD_DEBUG) {
						Toast.makeText(getApplicationContext(), action.equals(ACTION_CANCEL) ? "You click cancel button." : "Foreground service is stopped.", Toast.LENGTH_LONG).show();
					} else {
						if (action.equals(ACTION_CANCEL)) {
							BoxPlayApplication.getBoxPlayApplication().toast(R.string.boxplay_service_foreground_notification_cancel_message).show();
						}
					}
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
	
	/** Used to build and start foreground service. */
	private void startForegroundService() {
		Log.d(TAG, "Start foreground service.");
		
		startForeground(NOTIFICATION_ID, createNotification(null, INDETERMINATE_PROGRESS));
		
		execute();
	}
	
	/** Used to ask the service to stop as soon as possible. */
	private void stopForegroundService() {
		Log.d(TAG, "Stop foreground service.");
		
		if (BoxPlayApplication.BUILD_DEBUG) {
			ToastUtils.makeLong(this, "Stopping foreground service:: " + executorWorker).show();
		}
		
		if (executorWorker != null) {
			executorWorker.shouldStop();
		}
		
		stopForeground(true);
		
		stopSelf();
	}
	
	@SuppressWarnings("deprecation")
	private Notification createNotification(ForegroundTask task, int progress) {
		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MAIN_NOTIFICATION_CHANNEL);
		
		if (task != null) {
			NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
			bigTextStyle.setBigContentTitle(boxPlayApplication.getString(R.string.boxplay_service_foreground_task_actual, task.getTaskName()));
			bigTextStyle.bigText(task.getTaskDescription());
			builder.setStyle(bigTextStyle);
		}
		
		builder.setContentTitle(getString(R.string.boxplay_service_foreground_notification_title)) //
				.setContentText(getString(R.string.boxplay_service_foreground_notification_eta)) //
				.setProgress(100, progress, progress == INDETERMINATE_PROGRESS) //
				.setOnlyAlertOnce(true) //
				.setWhen(System.currentTimeMillis()) //
				.setSmallIcon(R.mipmap.icon_launcher) //
				.setPriority(NotificationCompat.PRIORITY_DEFAULT) //
				.setFullScreenIntent(pendingIntent, true); //
		
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
	
	public void updateNotification(ForegroundTask task, int progress) {
		Notification notification = createNotification(task, progress);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	/** Execute tasks. */
	public void execute() {
		executorWorker = new ForegroundTaskExecutor(this, new Handler(Looper.getMainLooper())) {
			@Override
			protected void done() {
				super.done();
				
				handler.post(new Runnable() {
					@Override
					public void run() {
						stopForegroundService();
					}
				});
				
				executorWorker = null;
			}
			
		};
		
		executorWorker.start();
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