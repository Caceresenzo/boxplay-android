package caceresenzo.apps.boxplay.services.tasks.implementations;

import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
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
		
		this.myListManager = managers.getMyListManager();
		this.subscriptionManager = managers.getSubscriptionManager();
		
		this.subscriptionMyList = myListManager.getSubscriptionsMyList();
	}
	
	@Override
	protected void task() {
		List<MyListable> myListables = subscriptionMyList.reload(myListManager.getSqliteBridge()).fetch();
		
		for (int i = 0; i < myListables.size(); i++) {
			final SearchAndGoResult searchAndGoResult = (SearchAndGoResult) myListables.get(i);
			
			foregroundTaskExecutor.publishUpdate(this, i + 1, myListables.size());
			
			Subscriber subscriber = subscriptionManager.getSubscriberFromResult(searchAndGoResult);
			
			if (subscriber != null) {
				try {
					subscriber.fetch(subscriptionManager.getStorageSolution(), searchAndGoResult, new Subscriber.SubscriberCallback() {
						@Override
						public void onNewContent(final SubscriptionItem item) {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									int androidNotificationFakeId = (int) System.currentTimeMillis();
									
									@SuppressWarnings("deprecation")
									NotificationCompat.Builder builder = new NotificationCompat.Builder(boxPlayApplication);
									builder.setChannelId(BoxPlayForegroundService.SEARCH_AND_GO_UPDATE_NOTIFICATION_CHANNEL) //
											.setSmallIcon(R.mipmap.icon_launcher) //
											.setContentTitle(boxPlayApplication.getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_update_new_item)) //
											.setContentText(boxPlayApplication.getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_update_new_item_content, searchAndGoResult.getName(), item.getContent())) //
											.setAutoCancel(true) //
											.setContentIntent(PendingIntent.getActivity(boxPlayApplication, androidNotificationFakeId, SearchAndGoDetailActivity.createStartIntent(boxPlayApplication, searchAndGoResult), PendingIntent.FLAG_IMMUTABLE));
									
									NotificationManager notificationManager = (NotificationManager) boxPlayApplication.getSystemService(Context.NOTIFICATION_SERVICE);
									notificationManager.notify(androidNotificationFakeId, builder.build());
								}
							});
						}
						
						@Override
						public void onException(SearchAndGoResult result, Exception exception) {
							Log.e(TAG, "Error occured when trying to fetch item.", exception);
						}
					});
					
					if (BoxPlayApplication.BUILD_DEBUG) {
						SubscriberStorageSolution storageSolution = subscriptionManager.getStorageSolution();
						List<SubscriptionItem> localItems = storageSolution.getLocalStorageItems(searchAndGoResult);
						
						for (SubscriptionItem subscriptionItem : localItems) {
							Log.i(TAG, "-> ITEM : " + subscriptionItem.getContent());
						}
						
						int countToRemove = Math.min(localItems.size(), 3);
						for (int j = 0; j < countToRemove; j++) {
							localItems.remove(0);
						}
						
						for (SubscriptionItem subscriptionItem : localItems) {
							Log.i(TAG, "-> NOW ITEM : " + subscriptionItem.getContent());
						}
						
						storageSolution.updateLocalStorageItems(searchAndGoResult, localItems);
					}
				} catch (Exception exception) {
					Log.e(TAG, "Failed to fetch item.", exception);
				}
			}
			
			checkThread();
			
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