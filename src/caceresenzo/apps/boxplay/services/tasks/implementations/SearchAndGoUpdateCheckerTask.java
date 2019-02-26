package caceresenzo.apps.boxplay.services.tasks.implementations;

import java.util.List;

import android.app.PendingIntent;
import android.util.Log;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.SearchAndGoDetailActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.MyListManager;
import caceresenzo.apps.boxplay.managers.MyListManager.MyList;
import caceresenzo.apps.boxplay.managers.SubscriptionManager;
import caceresenzo.apps.boxplay.services.tasks.ForegroundTask;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderWeakCache;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.item.SubscriptionItem;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.Subscriber;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.SubscriberStorageSolution;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.list.ListUtils;

public class SearchAndGoUpdateCheckerTask extends ForegroundTask {
	
	/* Tag */
	public static final String TAG = SearchAndGoUpdateCheckerTask.class.getSimpleName();
	
	/* Managers */
	private MyListManager myListManager;
	private SubscriptionManager subscriptionManager;
	
	/* My List */
	private MyList subscriptionMyList;
	
	/* Variables */
	private SearchAndGoResult actuallyCheckedResult;
	
	/* Constructor */
	public SearchAndGoUpdateCheckerTask() {
		super();
		
		this.myListManager = managers.getMyListManager();
		this.subscriptionManager = managers.getSubscriptionManager();
		
		this.subscriptionMyList = myListManager.getSubscriptionsMyList();
	}
	
	@Override
	protected void task() {
		Log.i(TAG, "Clearing cache, size=" + ProviderWeakCache.computeMemorySizeAndDestroy());
		
		List<MyListable> myListables = subscriptionMyList.reload().fetch();
		
		for (int i = 0; i < myListables.size(); i++) {
			final SearchAndGoResult searchAndGoResult = actuallyCheckedResult = (SearchAndGoResult) myListables.get(i);
			
			foregroundTaskExecutor.publishUpdate(this, i + 1, myListables.size());
			
			final Subscriber subscriber = subscriptionManager.getSubscriberFromResult(searchAndGoResult);
			
			if (subscriber != null) {
				try {
					subscriber.fetch(subscriptionManager.getStorageSolution(), searchAndGoResult, new Subscriber.SubscriberCallback() {
						@Override
						public void onNewContent(List<SubscriptionItem> items, SubscriptionItem lastestItem) {
							final SubscriptionItem item = ListUtils.getLastestItem(items);
							
							if (item != null) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										int androidNotificationFakeId = (int) System.currentTimeMillis();
										
										String content = item.getContent();
										if (subscriber.shouldNameBeReformatted()) {
											content = boxPlayApplication.getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_update_new_item_content, searchAndGoResult.getName(), item.getContent());
										}
										
										notificate(RANDOM_ID, createNotificationBuilder() //
												.setSmallIcon(R.mipmap.icon_launcher) //
												.setContentTitle(boxPlayApplication.getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_update_new_item)) //
												.setContentText(content) //
												.setAutoCancel(true) //
												.setContentIntent(PendingIntent.getActivity(boxPlayApplication, androidNotificationFakeId, SearchAndGoDetailActivity.createStartIntent(boxPlayApplication, searchAndGoResult), PendingIntent.FLAG_IMMUTABLE)) //
										);
									}
								});
							}
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
							localItems.remove(ListUtils.getLastestItem(localItems));
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
		
		actuallyCheckedResult = null;
	}
	
	@Override
	public String getTaskName() {
		return getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_title);
	}
	
	@Override
	public String getTaskDescription() {
		if (actuallyCheckedResult == null) {
			return getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_description);
		} else {
			return getString(R.string.boxplay_service_foreground_task_searchngo_subscriptions_description_with_item, actuallyCheckedResult.getName());
		}
	}
	
}