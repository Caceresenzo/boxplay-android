package caceresenzo.apps.boxplay.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import caceresenzo.android.libs.internet.NetworkUtils;
import caceresenzo.android.libs.service.ServiceUtils;
import caceresenzo.apps.boxplay.receivers.BoxPlayServiceRestartBroadcastReceiver;
import caceresenzo.libs.thread.AbstractHelpedThread;
import caceresenzo.libs.thread.ThreadUtils;

public class BoxPlayBackgroundService extends Service {
	
	/* Tag */
	public static final String TAG = BoxPlayBackgroundService.class.getSimpleName();
	
	/* Constants */
	public static final Intent RESTART_INTENT = new Intent(BoxPlayServiceRestartBroadcastReceiver.RESTART_ACTION);
	
	public static final long TASK_WAIT_PERIOD = 1000 * 5;
	public static final long TASK_WAIT_INTERNET = 1000 * 15;
	
	/* Instance */
	private static BoxPlayBackgroundService SERVICE;
	
	/* Managers */
	private Handler handler;
	
	/* Service */
	private IBinder binder;
	
	/* Task */
	private BackgroundTask backgroundTask;
	
	/* Variable */
	private boolean internetConnected;
	private int loop = 0;
	
	/* Receivers */
	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			internetConnected = NetworkUtils.isConnected(context);
		}
	};
	
	/* Constructor */
	public BoxPlayBackgroundService() {
		super();
		SERVICE = this;
		
		this.handler = new Handler();
		
		this.binder = new LocalBinder();
		
		this.backgroundTask = new BackgroundTask();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		/* Emulating */
		connectionChangeReceiver.onReceive(this, new Intent());
		
		registerReceiver(connectionChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		Log.d(TAG, "Service created!");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		backgroundTask.start();
		
		Log.d(TAG, "Service started!");
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(connectionChangeReceiver);
		
		sendBroadcast(RESTART_INTENT);
		
		if (backgroundTask != null) {
			backgroundTask.cancel();
			backgroundTask = null;
		}
		
		Log.d(TAG, "Service destroyed!");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class LocalBinder extends Binder {
		public BoxPlayBackgroundService getService() {
			return BoxPlayBackgroundService.this;
		}
	}
	
	class BackgroundTask extends AbstractHelpedThread {
		@Override
		protected void onRun() {
			Log.d(TAG, "Looping... (" + ++loop + ")");
			
			while (!internetConnected) {
				Log.d(TAG, "No internet. Waiting...");
				
				ThreadUtils.sleep(TASK_WAIT_INTERNET);
			}
			
			/*
			 * Insert working stuff here
			 * 
			 * TODO: Add Search n' Go Subscribers
			 */
			// Subscriber.checkEveryProvider();
			// String content;
			// try {
			// content = Downloader.getUrlContent("http://monip.org");
			// } catch (IOException exception) {
			// content = exception.getLocalizedMessage();
			// }
			//
			// Log.i(TAG, "Downloaded target at loop: " + loop);
			//
			// final String finalContent = content;
			// handler.post(new Runnable() {
			// @Override
			// public void run() {
			// ToastUtils.makeShort(BoxPlayBackgroundService.this, finalContent);
			// }
			// });
		}
		
		@Override
		protected void onFinished() {
			ThreadUtils.sleep(TASK_WAIT_PERIOD);
			
			backgroundTask = new BackgroundTask();
			backgroundTask.start();
		}
		
		@Override
		protected void onCancelled() {
			;
		}
	}
	
	public static Intent getServiceIntent(Context context) {
		return new Intent(context, BoxPlayBackgroundService.class);
	}
	
	public static boolean isRunning(Context context) {
		return ServiceUtils.isServiceRunning(context, BoxPlayBackgroundService.class);
	}
	
	public static void startIfNotAlready(Context context) {
		if (!isRunning(context)) {
			// context.startService(getServiceIntent(context)); // TODO: Make it work properly
		}
	}
	
	public static void stopIfNotAlready(Context context) {
		if (isRunning(context)) {
			context.stopService(getServiceIntent(context));
		}
	}
	
	public static BoxPlayBackgroundService getBoxPlayBackgroundService() {
		return SERVICE;
	}
	
}