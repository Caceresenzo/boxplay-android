package caceresenzo.apps.boxplay.services.tasks.implementations;

import java.util.List;

import android.app.NotificationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.apps.boxplay.managers.MyListManager.MyList;
import caceresenzo.apps.boxplay.managers.SubscriptionManager;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.apps.boxplay.services.tasks.ForegroundTask;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.item.SubscriptionItem;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.Subscriber;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.SubscriberStorageSolution;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.random.Randomizer;

public class SearchAndGoUpdateCheckerTask extends ForegroundTask {
	
	/* Tag */
	public static final String TAG = SearchAndGoUpdateCheckerTask.class.getSimpleName();
	
	/* Managers */
	private MyListManager myListManager;
	private SubscriptionManager subscriptionManager;
	
	/* My List */
	private MyList subscriptionMyList;
	
	public SearchAndGoUpdateCheckerTask() {
		super();
		
		this.myListManager = BoxPlayApplication.getManagers().getMyListManager();
		this.subscriptionManager = BoxPlayApplication.getManagers().getSubscriptionManager();
		
		this.subscriptionMyList = myListManager.getSubscriptionsMyList();
	}
	
	@Override
	protected void task() {
		List<MyListable> myListables = subscriptionMyList.fetch();
		
		for (MyListable myListable : myListables) {
			final SearchAndGoResult searchAndGoResult = (SearchAndGoResult) myListable;
			
			Log.i(TAG, "Fetching item: " + searchAndGoResult.getName());
			
			Subscriber subscriber = subscriptionManager.getSubscriberFromResult(searchAndGoResult);
			
			Log.i(TAG, "Subscriber: " + subscriber.getClass().getSimpleName());
			
			if (subscriber != null) {
				try {
					subscriber.fetch(subscriptionManager.getStorageSolution(), searchAndGoResult, new Subscriber.SubscriberCallback() {
						@Override
						public void onNewContent(final SubscriptionItem item) {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									ToastUtils.makeLong(BoxPlayApplication.getBoxPlayApplication(), "New item: " + item.getContent()).show();
									Log.i(TAG, "NEW EPISODE: " + item.getContent());
									
									NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BoxPlayApplication.getBoxPlayApplication());
									mBuilder.setChannelId(BoxPlayForegroundService.NOTIFICATION_CHANNEL) //
											.setSmallIcon(R.mipmap.icon_launcher) //
											.setContentTitle("New item available — Search n' Go") //
											.setContentText(searchAndGoResult.getName() + " — " + item.getContent()); //
									NotificationManager notificationManager = BoxPlayApplication.getBoxPlayApplication().getSystemService(NotificationManager.class);
									notificationManager.notify(Randomizer.randomInt(0, 99999), mBuilder.build());
								}
							});
						}
						
						@Override
						public void onException(SearchAndGoResult result, Exception exception) {
							Log.e(TAG, "Error occured when trying to fetch item.", exception);
						}
					});
					
					SubscriberStorageSolution storageSolution = subscriptionManager.getStorageSolution();
					List<SubscriptionItem> localItems = storageSolution.getLocalStorageItems(searchAndGoResult);
					
					for (SubscriptionItem subscriptionItem : localItems) {
						Log.i(TAG, "-> ITEM : " + subscriptionItem.getContent());
					}
					
					int countToRemove = Math.min(localItems.size(), 3);
					for (int i = 0; i < countToRemove; i++) {
						localItems.remove(0);
					}
					
					for (SubscriptionItem subscriptionItem : localItems) {
						Log.i(TAG, "-> NOW ITEM : " + subscriptionItem.getContent());
					}
					
					storageSolution.updateLocalStorageItems(searchAndGoResult, localItems);
				} catch (Exception exception) {
					Log.e(TAG, "Failed to fetch item.", exception);
				}
			}
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