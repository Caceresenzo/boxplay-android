package caceresenzo.apps.boxplay.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.MyListManager2.MyListSqliteBridge.MyListEntry;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.boxplay.models.store.music.MusicGroup;
import caceresenzo.libs.boxplay.models.store.video.VideoGroup;
import caceresenzo.libs.boxplay.mylist.MyListable;
import caceresenzo.libs.boxplay.mylist.binder.ListItemBinder;
import caceresenzo.libs.cryptography.MD5;

/**
 * MyLists manager.
 * 
 * @author Enzo CACERES
 */
public class MyListManager2 extends AbstractManager {
	
	/* Tag */
	public static final String TAG = MyListManager2.class.getSimpleName();
	
	/* Constants */
	public static final String DATABASE_NAME = "my-lists";
	public static final int DATABASE_VERSION = 2;
	
	public static final String DATABASE_COLUMN_ID = "id";
	public static final String DATABASE_COLUMN_POSITION = "position";
	public static final String DATABASE_COLUMN_TYPE = "type";
	public static final String DATABASE_COLUMN_BUILDER = "builder";
	public static final String DATABASE_COLUMN_CONTENT = "content";
	
	public static final String MYLIST_WATCH_LATER = "watch_later";
	public static final String MYLIST_SUBSCRIPTIONS = "subscriptions";
	
	public static final int ENTRY_TYPE_UNKNOWN = -10;
	public static final int ENTRY_TYPE_STORE_VIDEO = 0;
	public static final int ENTRY_TYPE_STORE_MUSIC = 1;
	public static final int ENTRY_TYPE_CULTURE_SEARCH_AND_GO = 10;
	
	public static final int ENTRY_NO_ID = -1;
	
	/* My Lists */
	private MyListSqliteBridge sqliteBridge;
	private List<MyList> myLists;
	private MyList watchLaterMyList, subscriptionsMyList;
	
	@Override
	public void initialize() {
		this.sqliteBridge = new MyListSqliteBridge(boxPlayApplication);
		this.myLists = new ArrayList<>();
		
		this.watchLaterMyList = create(MYLIST_WATCH_LATER);
		this.subscriptionsMyList = create(MYLIST_SUBSCRIPTIONS);
		
		sqliteBridge.initialize(myLists);
		
		loadAll();
	}
	
	/**
	 * Create a {@link MyList} instance.<br>
	 * This will also register it in the {@link #myLists} list.
	 * 
	 * @param name
	 *            Target name of the list you want to create.<br>
	 *            This will be used a the table name for entry storage.
	 * @return Created {@link MyList} instance.
	 */
	private MyList create(String name) {
		MyList myList = new MyList(name);
		myLists.add(myList);
		return myList;
	}
	
	@Override
	protected void destroy() {
		saveAll();
	}
	
	/**
	 * Call {@link MyList#load()} with every {@link MyList} created.
	 */
	public void loadAll() {
		for (MyList myList : myLists) {
			Log.i(TAG, String.format("Loading list \"%s\"...", myList.getName()));
			
			myList.load(sqliteBridge);
		}
	}
	
	/**
	 * Call {@link MyList#save()} with every {@link MyList} created.
	 */
	public void saveAll() {
		for (MyList myList : myLists) {
			Log.i(TAG, String.format("Saving list \"%s\"...", myList.getName()));
			
			myList.save(sqliteBridge);
		}
	}
	
	public MyList getWatchLaterMyList() {
		return watchLaterMyList;
	}
	
	public MyList getSubscriptionsMyList() {
		return subscriptionsMyList;
	}
	
	public MyListSqliteBridge getSqliteBridge() {
		return sqliteBridge;
	}
	
	public static class MyListSqliteBridge extends SQLiteOpenHelper {
		
		/* Constructor */
		protected MyListSqliteBridge(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase database) {
			; /* Can't initialize lists here, because if one is added later, table would not have been created */
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			database.execSQL("SELECT 'DROP TABLE ' || name || ';' FROM sqlite_master WHERE type = 'table';");
			onCreate(database);
			
			if (database.isOpen()) {
				database.close();
			}
		}
		
		protected void initialize(List<MyList> myLists) {
			SQLiteDatabase database = getWritableDatabase();
			
			for (MyList myList : myLists) {
				database.execSQL(String.format(String.join("\n", //
						"CREATE TABLE IF NOT EXISTS `%s` (", //
						"	" + DATABASE_COLUMN_ID + " INTEGER PRIMARY KEY,", //
						"	" + DATABASE_COLUMN_POSITION + " INTEGER,", //
						"  	" + DATABASE_COLUMN_TYPE + " INTEGER,", //
						"  	" + DATABASE_COLUMN_BUILDER + " TEXT NOT NULL,", //
						"  	" + DATABASE_COLUMN_CONTENT + " TEXT NOT NULL", //
						");" //
				), myList.getName()));
			}
			
			database.close();
		}
		
		public List<MyListEntry> getEntries(MyList myList) {
			List<MyListEntry> entries = new ArrayList<>();
			List<Integer> failedIds = new ArrayList<>();
			
			SQLiteDatabase database = getReadableDatabase();
			
			Cursor cursor = database.query(myList.getName(), null, null, null, null, null, null);
			
			while (cursor.moveToNext()) {
				MyListEntry entry = MyListEntry.create(cursor);
				
				if (entry != null) {
					entries.add(entry);
				} else {
					int id = ENTRY_NO_ID;
					try {
						id = cursor.getInt(cursor.getColumnIndex(DATABASE_COLUMN_ID));
					} catch (Exception exception) {
						; /* In case of */
					}
					
					if (id != ENTRY_NO_ID) {
						failedIds.add(id);
					}
				}
			}
			
			database.close();
			
			if (!failedIds.isEmpty()) {
				deleteIds(myList, failedIds);
			}
			
			return entries;
		}
		
		/**
		 * Delete entries by their ids.
		 * 
		 * @param myList
		 *            Target {@link MyList}.
		 * @param ids
		 *            {@link List} of id you want to delete.
		 * @return If it completly success.
		 */
		public boolean deleteIds(MyList myList, List<Integer> ids) {
			SQLiteDatabase database = getWritableDatabase();
			
			boolean success = false;
			
			try {
				for (int id : ids) {
					database.delete(myList.getName(), DATABASE_COLUMN_ID + "=" + id, null);
				}
				
				success = true;
			} catch (Exception exception) {
				Log.w(TAG, "Failed to remove some ids from the database.", exception);
			}
			
			database.close();
			
			return success;
		}
		
		public void push(MyList myList, List<MyListEntry> entries) {
			SQLiteDatabase database = getWritableDatabase();
			
			/* Empty table */
			database.delete(myList.getName(), null, null);
			
			/* Insert new values */
			for (MyListEntry entry : entries) {
				ContentValues contentValues = new ContentValues();
				// if (entry.getId() != ENTRY_NO_ID) {
				// content.put(DATABASE_COLUMN_ID, entry.getId());
				// }
				contentValues.put(DATABASE_COLUMN_POSITION, entry.getPosition());
				contentValues.put(DATABASE_COLUMN_TYPE, entry.getType());
				contentValues.put(DATABASE_COLUMN_BUILDER, entry.getBuilderClass().getName());
				contentValues.put(DATABASE_COLUMN_CONTENT, entry.getContent());
				
				Log.d(TAG, String.format("Inserted: %s, last row %s", entry.getContent(), database.insert(myList.getName(), null, contentValues)));
			}
			
			database.close();
		}
		
		public static class MyListEntry {
			
			/* Variables */
			private final int id, position, type;
			private final Class<?> builder;
			private final String content;
			
			/* Constructor */
			private MyListEntry(int id, int position, int type, Class<?> builder, String content) {
				this.id = id;
				this.type = type;
				this.position = position;
				this.builder = builder;
				this.content = content;
			}
			
			public int getId() {
				return id;
			}
			
			public int getPosition() {
				return position;
			}
			
			public int getType() {
				return type;
			}
			
			public Class<?> getBuilderClass() {
				return builder;
			}
			
			public String getContent() {
				return content;
			}
			
			/**
			 * Create a new {@link MyListEntry} with a {@link Cursor} row.
			 * 
			 * @param cursor
			 *            Database's query cursor.
			 * @return New {@link MyListEntry} instance, or null if anything fails.
			 */
			public static MyListEntry create(Cursor cursor) {
				try {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(DATABASE_COLUMN_ID));
					int position = cursor.getInt(cursor.getColumnIndexOrThrow(DATABASE_COLUMN_POSITION));
					int type = cursor.getInt(cursor.getColumnIndexOrThrow(DATABASE_COLUMN_TYPE));
					Class<?> builder = Class.forName(cursor.getString(cursor.getColumnIndexOrThrow(DATABASE_COLUMN_BUILDER)));
					String content = cursor.getString(cursor.getColumnIndexOrThrow(DATABASE_COLUMN_CONTENT));
					
					return new MyListEntry(id, position, type, builder, content);
				} catch (Exception exception) {
					Log.w(TAG, "Failed to create entry from the database's cursor.", exception);
				}
				
				return null;
			}
			
			/**
			 * Create a new {@link MyListEntry} from a {@link ListItemBinder}.
			 * 
			 * @param myListable
			 *            Source item.
			 * @return New {@link MyListEntry} instance, or null if anything fails.
			 */
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public static MyListEntry create(MyListable myListable) {
				try {
					ListItemBinder itemBinder = Objects.requireNonNull(myListable.createCompatibleBinder());
					
					int id = ENTRY_NO_ID;
					int position = -1;
					int type = objectToType(myListable);
					Class<?> builder = itemBinder.getClass();
					String content = itemBinder.convertItemToString(myListable);
					
					return new MyListEntry(id, position, type, builder, content);
				} catch (Exception exception) {
					Log.w(TAG, "Failed to create entry from its item binder.", exception);
				}
				
				return null;
			}
		}
	}
	
	public static class MyList {
		
		/* Variables */
		private String name;
		private Map<String, MyListable> myListables;
		
		/* Constructor */
		public MyList(String name) {
			this.name = name;
			this.myListables = new HashMap<String, MyListable>() {
				@Override
				public MyListable put(String key, MyListable value) {
					return super.put(MD5.silentMd5(key), value);
				}
				
				@Override
				public MyListable remove(Object key) {
					return super.remove(MD5.silentMd5((String) key));
				}
				
				@Override
				public MyListable get(Object key) {
					return super.get(MD5.silentMd5((String) key));
				}
				
				@Override
				public boolean containsKey(Object key) {
					return super.containsKey(MD5.silentMd5((String) key));
				}
			};
		}
		
		public void load(MyListSqliteBridge sqliteBridge) {
			List<MyListEntry> entries = sqliteBridge.getEntries(this);
			
			for (MyListEntry entry : entries) {
				try {
					ListItemBinder<?, ?> binder = (ListItemBinder<?, ?>) entry.getBuilderClass().newInstance();
					Object reconstructed;
					
					switch (entry.getType()) {
						case ENTRY_TYPE_CULTURE_SEARCH_AND_GO: {
							reconstructed = binder.restoreItemFromString(entry.getContent());
							break;
						}
						
						case ENTRY_TYPE_STORE_VIDEO:
						case ENTRY_TYPE_STORE_MUSIC:
						default: {
							throw new UnsupportedOperationException("This type is not supported.");
						}
					}
					
					if (reconstructed instanceof MyListable) {
						MyListable myListable = (MyListable) reconstructed;
						
						myListables.put(myListable.toUniqueString(), myListable);
					}
				} catch (Exception exception) {
					Log.w(TAG, "Failed to restore entry.", exception);
				}
			}
		}
		
		public void save(MyListSqliteBridge sqliteBridge) {
			List<MyListEntry> entries = new ArrayList<>();
			
			for (MyListable myListable : myListables.values()) {
				try {
					MyListEntry entry = MyListEntry.create(myListable);
					
					if (entry != null) {
						entries.add(entry);
					}
				} catch (Exception exception) {
					Log.w(TAG, "Failed to create entry from MyListable.", exception);
				}
			}
			
			sqliteBridge.push(this, entries);
		}
		
		public void fetch(MyListManager2.FetchCallback callback) {
			callback.onFetchFinished(new ArrayList<MyListable>(myListables.values()));
		}
		
		public String getName() {
			return name;
		}
		
		public void add(MyListable myListable) {
			if (!contains(myListable)) {
				Log.d(TAG, "Added item: " + myListable.toUniqueString());
				myListables.put(myListable.toUniqueString(), myListable);
				
				save(BoxPlayApplication.getManagers().myListManager2.getSqliteBridge());
			} else {
				Log.d(TAG, "Trying to add item already in the list.");
			}
		}
		
		public void remove(MyListable myListable) {
			if (contains(myListable)) {
				Log.d(TAG, "Removed item: " + myListable.toUniqueString());
				myListables.remove(myListable.toUniqueString());
				
				save(BoxPlayApplication.getManagers().myListManager2.getSqliteBridge());
			} else {
				Log.d(TAG, "Trying to remove item that are not in the list.");
			}
		}
		
		public boolean contains(MyListable myListable) {
			return myListables.containsKey(myListable.toUniqueString());
		}
	}
	
	public static int objectToType(MyListable myListable) {
		if (myListable instanceof VideoGroup) {
			return ENTRY_TYPE_STORE_VIDEO;
		} else if (myListable instanceof MusicGroup) {
			return ENTRY_TYPE_STORE_MUSIC;
		} else if (myListable instanceof SearchAndGoResult) {
			return ENTRY_TYPE_CULTURE_SEARCH_AND_GO;
		}
		
		return ENTRY_TYPE_UNKNOWN;
	}
	
	public static interface FetchCallback {
		void onFetchFinished(List<MyListable> myListables);
		
		void onException(Exception exception);
	}
	
}