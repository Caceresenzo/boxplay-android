package caceresenzo.apps.boxplay.managers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.util.Log;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.boxplay.models.element.BoxPlayElement;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

/**
 * Manager for Lists managements
 * 
 * @author Enzo CACERES
 */
public class MyListManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = MyListManager.class.getSimpleName();
	
	/* File / Folders */
	private File listFolder; /* Used by MyList instances, must be define before creating lists */
	
	/* Lists */
	private MyList watchLaterList, subscriptionsList;
	
	private List<MyList> myLists;
	
	/* Variables */
	private static boolean videoManagerFinished;
	
	@Override
	public void initialize() {
		this.listFolder = new File(getManagers().getBaseDataDirectory() + "/list/");
		
		this.myLists = new ArrayList<>();
		
		this.myLists.add(this.watchLaterList = new WatchLaterList());
		this.myLists.add(this.subscriptionsList = new SubscriptionsList());
		
		loadAll();
	}
	
	@Override
	protected void destroy() {
		saveAll();
	}
	
	/**
	 * Call {@link MyList#load()} for every {@link MyList}
	 */
	public void loadAll() {
		for (MyList myList : myLists) {
			myList.load();
		}
	}
	
	/**
	 * Call {@link MyList#save()} for every {@link MyList}
	 */
	public void saveAll() {
		for (MyList myList : myLists) {
			myList.save();
		}
	}
	
	/**
	 * Get the Watch Later {@link MyList} instance
	 * 
	 * @return Watch Later List
	 */
	public MyList getWatchLaterList() {
		return watchLaterList;
	}
	
	/**
	 * Get the Subscriptions {@link MyList} instance
	 * 
	 * @return Subscriptions List
	 */
	public MyList getSubscriptionsList() {
		return subscriptionsList;
	}
	
	/**
	 * Tell if the VideoManager has finished working
	 * 
	 * @param hasFinished
	 *            New state
	 */
	public static void videoManagerFinished(boolean hasFinished) {
		videoManagerFinished = hasFinished;
	}
	
	/**
	 * Lock the {@link Thread} while the {@link VideoManager} finish
	 */
	public static void waitVideoManager() {
		while (!videoManagerFinished) {
			ThreadUtils.sleep(100L);
		}
	}
	
	/**
	 * Watch Later List
	 * 
	 * @author Enzo CACERES
	 */
	class WatchLaterList extends MyList {
		private File rawDataFile;
		
		public WatchLaterList() {
			super(new WatchListFetchWorker());
			
			this.rawDataFile = new File(listFolder, "watchlater.list.javaraw");
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void load() {
			if (!checkFile()) {
				Log.e(TAG, "Loading aborted.");
				return;
			}
			Log.i(TAG, String.format("Loading list: %s", getClass().getSimpleName()));
			
			if (myListables == null) {
				myListables = new LinkedHashMap<>();
			}
			
			try {
				InputStream file = new FileInputStream(rawDataFile);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				
				try {
					Map<String, MyListable> recoveredItems = (Map<String, MyListable>) input.readObject();
					
					if (recoveredItems != null) {
						myListables.putAll(recoveredItems);
					}
				} finally {
					input.close();
				}
			} catch (Exception exception) {
				Log.e(TAG, "Failed to load Watch Later list, first time ?", exception);
			}
			
			save(); // Will reset everything if incompatible or invalid loading
		}
		
		@Override
		public void save() {
			if (!checkFile() || myListables == null) {
				Log.e(TAG, "Saving aborted.");
				return;
			}
			Log.i(TAG, String.format("Saving list: %s with content: %s", getClass().getSimpleName(), values()));
			
			try {
				OutputStream file = new FileOutputStream(rawDataFile);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				
				try {
					output.writeObject(myListables);
				} finally {
					output.close();
				}
			} catch (IOException exception) {
				Log.e(TAG, "Failed to save Watch Later list", exception);
			}
		}
		
		public boolean checkFile() {
			if (!rawDataFile.exists() || rawDataFile.isDirectory()) {
				try {
					rawDataFile.mkdirs();
					rawDataFile.delete();
					rawDataFile.createNewFile();
					Log.i(TAG, "Base directory created!");
					return true;
				} catch (IOException exception) {
					Log.e(TAG, "Failed to create base directory. (absolute path=" + rawDataFile.getAbsolutePath() + ")", exception);
					return false;
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Subscriptions List
	 * 
	 * @author Enzo CACERES
	 */
	class SubscriptionsList extends MyList {
		public SubscriptionsList() {
			super(null);
		}
		
		@Override
		public void save() {
			;
		}
		
		@Override
		public void load() {
			;
		}
	}
	
	/**
	 * Abstract class, base class to extend to create a new List
	 * 
	 * @author Enzo CACERES
	 */
	public abstract static class MyList {
		protected Map<String, MyListable> myListables;
		protected FetchWorker worker;
		
		/* Constructor */
		public MyList(FetchWorker worker) {
			this.worker = worker;
			
			this.myListables = new LinkedHashMap<>();
			
			if (worker != null) {
				worker.attachList(this);
			}
		}
		
		/**
		 * Add a {@link MyListable} to this {@link MyList} instance, will automaticly call {@link MyList#save()}
		 * 
		 * @param myListable
		 *            {@link MyListable} to be added
		 */
		public void addToList(MyListable myListable) {
			if (!containsInList(myListable)) {
				myListables.put(myListable.toUniqueString(), myListable);
				
				Log.d(TAG, String.format("Added item: %s to list: %s", myListable.toUniqueString(), getClass().getSimpleName()));
				
				save();
			}
		}
		
		/**
		 * Check if a {@link MyListable} is in this list
		 * 
		 * @param myListable
		 *            {@link MyListable} to check
		 * @return If it present or not ({@link Map#containsKey(Object)})
		 */
		public boolean containsInList(MyListable myListable) {
			return myListables.containsKey(myListable.toUniqueString());
		}
		
		/**
		 * Remove a {@link MyListable} to this {@link MyList} instance, will automaticly call {@link MyList#save()}
		 * 
		 * @param myListable
		 *            {@link MyListable} to be added
		 */
		public void removeFromList(MyListable myListable) {
			if (containsInList(myListable)) {
				myListables.remove(myListable.toUniqueString());
				
				Log.d(TAG, String.format("Removed item: %s from list: %s", myListable.toUniqueString(), getClass().getSimpleName()));
				
				save();
			}
		}
		
		/**
		 * Call {@link #clearList(boolean)} with autosave as false
		 */
		public void clearList() {
			clearList(false);
		}
		
		/**
		 * Clear list content
		 * 
		 * @param autosave
		 *            If you want to automaticly call {@link #save()} after clear
		 */
		public void clearList(boolean autosave) {
			myListables.clear();
			
			if (autosave) {
				save();
			}
		}
		
		/**
		 * Get the values of this list
		 * 
		 * @return All values
		 */
		public Collection<MyListable> values() {
			return myListables.values();
		}
		
		/**
		 * Get attached worker if this {@link MyList}
		 * 
		 * @return Attached {@link FetchWorker} instance
		 */
		public FetchWorker getWorker() {
			return worker;
		}
		
		/**
		 * 
		 * 
		 * @param callback
		 */
		public void fetch(FetchCallback callback) {
			if (worker.isRunning()) {
				BoxPlayApplication.getBoxPlayApplication().toast("Worker is budy").show();
				return;
			}
			
			// if (worker instanceof WatchListFetchWorker) {
			// worker = new WatchListFetchWorker();
			// } else {
			// throw new IllegalStateException("Unknown instance type of worker.");
			// }
			try {
				worker = worker.getClass().newInstance();
				
				worker.attachList(this).applyCallback(callback).start();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
		}
		
		public abstract void save();
		
		public abstract void load();
	}
	
	static class WatchListFetchWorker extends FetchWorker {
		@Override
		protected void execute() {
			if (myList == null) {
				terminate();
				return;
			}
			
			Log.d(TAG, "MyList content (before save): " + myList.values());
			
			myList.save();
			
			waitVideoManager();
			
			// myList.clearList();
			
			myList.load();
			
			Log.d(TAG, "MyList content (after load): " + myList.values());
			
			outputListable.addAll(myList.values());
			
			for (Object object : BoxPlayElement.getInstances().values()) {
				if (object instanceof VideoGroup) {
					VideoGroup videoGroup = (VideoGroup) object;
					
					if (videoGroup.isWatching()) {
						outputListable.add(videoGroup);
					}
				}
			}
		}
	}
	
	abstract static class FetchWorker extends WorkerThread {
		protected Handler handler;
		
		protected MyList myList;
		protected FetchCallback callback;
		protected List<MyListable> outputListable;
		
		public FetchWorker() {
			super();
			
			this.handler = BoxPlayApplication.getHandler();
			
			this.outputListable = new ArrayList<>();
		}
		
		public FetchWorker attachList(MyList myList) {
			this.myList = myList;
			
			return this;
		}
		
		@Override
		protected void done() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callback != null) {
						if (isCancelled()) {
							callback.onException(new WorkerThreadCancelledException());
						} else {
							callback.onFetchFinished(outputListable);
						}
					}
				}
			});
		}
		
		@Override
		protected void cancel() {
			done();
		}
		
		public FetchWorker applyCallback(FetchCallback callback) {
			this.callback = callback;
			
			return this;
		}
	}
	
	public static interface FetchCallback {
		void onFetchFinished(List<MyListable> myListables);
		
		void onException(Exception exception);
	}
	
}