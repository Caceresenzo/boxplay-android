package caceresenzo.apps.boxplay.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import caceresenzo.apps.boxplay.receivers.BoxPlayServiceRestartBroadcastReceiver;

public class BoxPlayBackgroundService extends Service {
	
	/* Tag */
	public static final String TAG = BoxPlayBackgroundService.class.getSimpleName();
	
	/* Constants */
	public static final Intent RESTART_INTENT = new Intent(BoxPlayServiceRestartBroadcastReceiver.RESTART_ACTION);
	
	/* Managers */
	private Handler handler;
	
	/* Service */
	private IBinder binder;
	
	/* Timer */
	private Timer timer;
	private TimerTask timerTask;
	
	private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};
	
	/* Constructor */
	public BoxPlayBackgroundService() {
		this.handler = new Handler();
		
		this.binder = new LocalBinder();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int notificationId = (int) (System.currentTimeMillis() % 10000);
			startForeground(notificationId, new Notification.Builder(this).build());
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		registerReceiver(connectionChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		timerTask = new TimerTask() {
			public void run() { /* Like a thread */
				// String content;
				// try {
				// content = Downloader.getUrlContent("http://monip.org");
				// } catch (IOException e) {
				// content = e.getLocalizedMessage();
				// }
				//
				// final String finalContent = content;
				// handler.post(new Runnable() {
				// @Override
				// public void run() {
				// ToastUtils.makeShort(BoxPlayBackgroundService.this, finalContent);
				// }
				// });
			}
		};
		
		timer = new Timer();
		timer.schedule(timerTask, 0, 5000);
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		sendBroadcast(RESTART_INTENT);
		
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
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
	
}