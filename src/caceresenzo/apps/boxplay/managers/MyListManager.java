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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.boxplay.models.element.BoxPlayElement;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.thread.HelpedThread;
import caceresenzo.libs.thread.ThreadUtils;

public class MyListManager extends AbstractManager {
	
	public static final String TAG = MyListManager.class.getSimpleName();
	
	private File listFolder, rawListDataFile;
	
	private Map<String, MyListable> watchLaterList;
	
	private FetchWorker fetchWorker;
	
	private boolean videoManagerFinished;
	
	@Override
	public void initialize() {
		this.listFolder = new File(getManagers().getBaseApplicationDirectory() + "/list/");
		this.rawListDataFile = new File(listFolder, "watchinglist.javaraw");
		
		this.watchLaterList = new LinkedHashMap<>();
		
		this.fetchWorker = new FetchWorker();
		
		load();
	}
	
	@Override
	protected void destroy() {
		save();
	}
	
	public void addToWatchList(MyListable myListable) {
		if (!containsInWatchList(myListable)) {
			watchLaterList.put(myListable.toUniqueString(), myListable);
			
			save();
		}
	}
	
	public boolean containsInWatchList(MyListable myListable) {
		Log.d(TAG, "---------------: " + watchLaterList.containsKey(myListable.toUniqueString()));
		return watchLaterList.containsKey(myListable.toUniqueString());
	}
	
	public void removeFromWatchList(MyListable myListable) {
		watchLaterList.remove(myListable.toUniqueString());
		
		save();
	}
	
	public void fetchWatchLaterItems(FetchCallback callback) {
		if (fetchWorker.isRunning()) {
			boxPlayApplication.toast("Worker is budy").show();
			return;
		}
		
		fetchWorker = new FetchWorker();
		fetchWorker.applyData(callback).start();
	}
	
	public void videoManagerFinished(boolean hasFinished) {
		this.videoManagerFinished = hasFinished;
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		if (!checkFile()) {
			return;
		}
		
		try {
			InputStream file = new FileInputStream(rawListDataFile);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			
			try {
				Map<String, MyListable> recoveredItems = (Map<String, MyListable>) input.readObject();
				
				if (recoveredItems != null) {
					watchLaterList.putAll(recoveredItems);
				}
			} finally {
				input.close();
			}
		} catch (Exception exception) {
			Log.e(TAG, "Failed to load Watch Later list, first time ?", exception);
		}
		
		save(); // Will reset everything if incompatible or invalid loading
	}
	
	public void save() {
		if (!checkFile() && watchLaterList != null) {
			return;
		}
		
		try {
			OutputStream file = new FileOutputStream(rawListDataFile);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			
			try {
				output.writeObject(watchLaterList);
			} finally {
				output.close();
			}
		} catch (IOException exception) {
			Log.e(TAG, "Failed to save Watch Later list", exception);
		}
	}
	
	public boolean checkFile() {
		if (!rawListDataFile.exists() || rawListDataFile.isDirectory()) {
			try {
				rawListDataFile.mkdirs();
				rawListDataFile.delete();
				rawListDataFile.createNewFile();
				Log.i(TAG, "Base directory created!");
				return true;
			} catch (IOException exception) {
				Log.e(TAG, "Failed to create base directory.", exception);
				return false;
			}
		}
		
		return true;
	}
	
	class FetchWorker extends HelpedThread {
		private FetchCallback callback;
		private List<MyListable> outputListable;
		
		public FetchWorker() {
			super();
			
			this.outputListable = new ArrayList<>();
		}
		
		@Override
		protected void onRun() {
			save();
			
			while (!videoManagerFinished) {
				ThreadUtils.sleep(100L);
			}
			
			watchLaterList.clear();
			
			load();
			
			outputListable.addAll(watchLaterList.values());
			
			for (Object object : BoxPlayElement.getInstances().values()) {
				if (object instanceof VideoGroup) {
					VideoGroup videoGroup = (VideoGroup) object;
					
					if (videoGroup.isWatching()) {
						outputListable.add(videoGroup);
					}
				}
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (callback != null) {
						callback.onFetchFinished(outputListable);
					}
				}
			});
		}
		
		@Override
		protected void onFinished() {
			;
		}
		
		@Override
		protected void onCancelled() {
			;
		}
		
		public FetchWorker applyData(FetchCallback callback) {
			this.callback = callback;
			
			return this;
		}
	}
	
	public static interface FetchCallback {
		
		void onFetchFinished(List<MyListable> myListables);
		
	}
	
}