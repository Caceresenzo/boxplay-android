package caceresenzo.apps.boxplay.managers;

import java.io.File;

import android.util.Log;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.Subscribable;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.Subscriber;
import caceresenzo.libs.boxplay.culture.searchngo.subscription.subscriber.SubscriberStorageSolution;

public class SubscriptionManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = SubscriptionManager.class.getSimpleName();
	
	/* Subscription System */
	private SubscriberStorageSolution storageSolution;
	
	@Override
	protected void initialize() {
		this.storageSolution = new SubscriberStorageSolution(new File(getManagers().getBaseDataDirectory(), "subscription/"));
	}
	
	public void unsubscribe(SearchAndGoResult searchAndGoResult) {
		Subscriber subscriber = getSubscriberFromResult(searchAndGoResult);
		
		if (subscriber != null) {
			subscriber.cleanUp(storageSolution, searchAndGoResult);
		}
	}
	
	public Subscriber getSubscriberFromResult(SearchAndGoResult searchAndGoResult) {
		SearchAndGoProvider parentProvider = searchAndGoResult.getParentProvider();
		
		if (parentProvider instanceof Subscribable) {
			Subscribable subscribable = (Subscribable) parentProvider;
			return subscribable.createSubscriber();
		}
		
		Log.w(TAG, "Not-subscribable parent provider for item: " + searchAndGoResult.toUniqueString());		
		
		return null;
	}
	
	public SubscriberStorageSolution getStorageSolution() {
		return storageSolution;
	}
	
}