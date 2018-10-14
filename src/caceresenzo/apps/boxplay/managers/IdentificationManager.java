package caceresenzo.apps.boxplay.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import caceresenzo.apps.boxplay.activities.identification.LoginActivity;
import caceresenzo.apps.boxplay.managers.IdentificationManager.LoginSubManager.LoginWorker;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.apps.boxplay.managers.XManagers.SubManager;
import caceresenzo.libs.boxplay.api.BoxPlayApi;
import caceresenzo.libs.boxplay.api.request.implementations.user.identification.UserLoginApiRequest;
import caceresenzo.libs.boxplay.api.request.implementations.user.identification.UserRegisterApiRequest;
import caceresenzo.libs.boxplay.api.response.ApiResponse;
import caceresenzo.libs.boxplay.users.User;
import caceresenzo.libs.thread.implementations.WorkerThread;

/**
 * Manager handling all stuff for the identification
 * 
 * @author Enzo CACERES
 */
public class IdentificationManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = IdentificationManager.class.getSimpleName();
	
	/* Sub Managers */
	private final LoginSubManager loginSubManager;
	private final RegisterSubManager registerSubManager;
	
	/* Database */
	private UserDatabaseHelper userDatabaseHelper;
	
	/* Api */
	private final BoxPlayApi boxPlayApi;
	
	private User loggedUser;
	
	/* Constructor */
	public IdentificationManager() {
		this.loginSubManager = new LoginSubManager();
		this.registerSubManager = new RegisterSubManager();
		
		this.boxPlayApi = new BoxPlayApi();
		
		this.userDatabaseHelper = new UserDatabaseHelper(boxPlayApplication);
	}
	
	@Override
	protected void initialize() {
		// updateLoggerUser(userDatabaseHelper.getSavedUser());
		
		// if (!checkUserValidity()) {
		LoginActivity.start();
		// }
	}
	
	/**
	 * @return Actual {@link LoginSubManager}
	 */
	public LoginSubManager getLoginSubManager() {
		return loginSubManager;
	}
	
	/**
	 * @return Actual {@link RegisterSubManager}
	 */
	public RegisterSubManager getRegisterSubManager() {
		return registerSubManager;
	}
	
	/**
	 * @return Helper handling the databases informations
	 */
	public UserDatabaseHelper getUserDatabaseHelper() {
		return userDatabaseHelper;
	}
	
	/**
	 * @return Actually instanced {@link BoxPlayApi}
	 */
	public BoxPlayApi getBoxPlayApi() {
		return boxPlayApi;
	}
	
	/**
	 * @return Actually logged user instance
	 */
	public User getLoggedUser() {
		return loggedUser;
	}
	
	/**
	 * @param user
	 *            Update the logged user
	 */
	public void updateLoggerUser(User user) {
		this.loggedUser = user;
		
		this.boxPlayApi.changeToken(user != null ? user.getIdentificationToken() : null);
		if (user != null) {
			getManagers().onUserLogged(user, boxPlayApi);
		}
	}
	
	/**
	 * Start the full user validity checking system<br>
	 * It will return only if the local value is null or valid to work with<br>
	 * If false; the login system will wake up and try to login from saved credidentials (if any) or directly ask the user to login
	 * 
	 * @return Basic user validity
	 */
	public boolean checkUserValidity() {
		return loggedUser != null; // TODO: Finish API-Retry system
	}
	
	/**
	 * Tell the manager that the user want to logout<br>
	 * This will call a database clear and will start the {@link LoginActivity}
	 */
	public void logout() {
		userDatabaseHelper.deleteUser();
		
		updateLoggerUser(null);
		
		LoginActivity.start();
	}
	
	/**
	 * Sub Manager class to handle login with the API
	 * 
	 * @author Enzo CACERES
	 */
	public class LoginSubManager extends SubManager {
		
		/* Worker */
		private LoginWorker worker;
		
		/**
		 * Start, if not already, a new {@link LoginWorker} to handle the login work with provided informations
		 * 
		 * @param username
		 *            Target user's username to login
		 * @param password
		 *            Target user's password to login
		 * @param callback
		 *            Callback used for communicating with the worker
		 * @throws IllegalArgumentException
		 *             If the callback is null
		 */
		public void login(String username, String password, LoginCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Callback can't be null.");
			}
			
			if (!WorkerThread.isWorkerFree(worker)) {
				boxPlayApplication.toast("LoginWorker is busy.").show();
				return;
			}
			
			worker = new LoginWorker();
			worker.apply(username, password, callback).start();
		}
		
		/**
		 * Worker class that will contact the API
		 * 
		 * @author Enzo CACERES
		 */
		class LoginWorker extends WorkerThread {
			private String username, password;
			private LoginCallback callback;
			
			@Override
			protected void execute() {
				ApiResponse<User> apiResponse = new UserLoginApiRequest(username, password).call(boxPlayApi);
				User user = apiResponse.selfProcess();
				
				updateLoggerUser(user);
				userDatabaseHelper.register(username, password, user);
				
				callback.onApiResponse(apiResponse, user);
			}
			
			/**
			 * Apply the local data to the worker
			 * 
			 * @param username
			 *            Target user's username to login
			 * @param password
			 *            Target user's password to login
			 * @param callback
			 *            Callback used for communicating with the worker
			 * @return Itself
			 */
			public LoginWorker apply(String username, String password, LoginCallback callback) {
				this.username = username;
				this.password = password;
				this.callback = callback;
				
				return this;
			}
		}
		
	}
	
	/**
	 * Callback class to communicate with the {@link LoginWorker}
	 * 
	 * @author Enzo CACERES
	 */
	public static interface LoginCallback {
		
		/**
		 * Called when the API has returned a response for the login request
		 * 
		 * @param apiResponse
		 *            Returned response
		 * @param user
		 *            Created user from the {@link ApiResponse}
		 */
		void onApiResponse(ApiResponse<User> apiResponse, User user);
		
	}
	
	public class RegisterSubManager extends SubManager {
		
		/* Worker */
		private RegisterWorker worker;
		
		/**
		 * Start, if not already, a new {@link RegisterWorker} to handle the register work with provided informations
		 * 
		 * @param username
		 *            Target user's username to login
		 * @param email
		 *            Target user's email to login
		 * @param password
		 *            Target user's password to login
		 * @param callback
		 *            Callback used for communicating with the worker
		 * @throws IllegalArgumentException
		 *             If the callback is null
		 */
		public void register(String username, String email, String password, RegisterCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Callback can't be null.");
			}
			
			if (!WorkerThread.isWorkerFree(worker)) {
				boxPlayApplication.toast("RegisterWorker is busy.").show();
				return;
			}
			
			worker = new RegisterWorker();
			worker.apply(username, email, password, callback).start();
		}
		
		/**
		 * Worker class that will contact the API
		 * 
		 * @author Enzo CACERES
		 */
		class RegisterWorker extends WorkerThread {
			private String username, email, password;
			private RegisterCallback callback;
			
			@Override
			protected void execute() {
				ApiResponse<?> apiResponse = new UserRegisterApiRequest(username, email, password).call(boxPlayApi).selfProcess();
				
				callback.onApiResponse(apiResponse);
			}
			
			/**
			 * Apply the local data to the worker
			 * 
			 * @param username
			 *            Target user's username to login
			 * @param password
			 *            Target user's password to login
			 * @param callback
			 *            Callback used for communicating with the worker
			 * @return Itself
			 */
			public RegisterWorker apply(String username, String email, String password, RegisterCallback callback) {
				this.username = username;
				this.email = email;
				this.password = password;
				this.callback = callback;
				
				return this;
			}
		}
	}
	
	/**
	 * Callback class to communicate with the {@link RegisterWorker}
	 * 
	 * @author Enzo CACERES
	 */
	public static interface RegisterCallback {
		
		/**
		 * Called when the API has returned a response for the register request
		 * 
		 * @param apiResponse
		 *            Returned response
		 */
		void onApiResponse(ApiResponse<?> apiResponse);
		
	}
	
	/**
	 * Database helper class to save and restore user
	 * 
	 * @author Enzo CACERES
	 */
	public static class UserDatabaseHelper extends SQLiteOpenHelper {
		
		/* Constants */
		public static final String TABLE_CREDIDENTIALS = "credidentials";
		
		public static final String COLUMN_CREDIDENTIALS_USERNAME = "username";
		public static final String COLUMN_CREDIDENTIALS_PASSWORD = "password";
		
		public static final String TABLE_USER_INFO = "user_info";
		
		public static final String COLUMN_USER_INFO_ID = "id";
		public static final String COLUMN_USER_INFO_USERNAME = "username";
		public static final String COLUMN_USER_INFO_EMAIL = "email";
		public static final String COLUMN_USER_INFO_TOKEN = "token";
		
		public static final String[] TABLES = { TABLE_USER_INFO, TABLE_CREDIDENTIALS };
		
		/* Constructor */
		public UserDatabaseHelper(Context context) {
			super(context, "user.db", null, 1);
		}
		
		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s TEXT, %s TEXT)", TABLE_CREDIDENTIALS, COLUMN_CREDIDENTIALS_USERNAME, COLUMN_CREDIDENTIALS_PASSWORD));
			database.execSQL(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s INTEGER, %s TEXT, %s TEXT, %s TEXT)", TABLE_USER_INFO, COLUMN_USER_INFO_ID, COLUMN_USER_INFO_USERNAME, COLUMN_USER_INFO_EMAIL, COLUMN_USER_INFO_TOKEN));
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			if (oldVersion < newVersion) {
				for (String table : TABLES) {
					database.execSQL(String.format("DROP TABLE %s;", table));
				}
				
				onCreate(database);
			}
		}
		
		/**
		 * Drop table and recreate them to be sure that everything is removed
		 */
		protected void clearDatabase() {
			SQLiteDatabase database = getReadableDatabase();
			
			if (database != null) {
				onUpgrade(database, 0, 1);
			}
		}
		
		/**
		 * Clear a specific table from the database
		 * 
		 * @param table
		 *            Target table to be deleted
		 */
		protected void clearTable(String table) {
			SQLiteDatabase database = getReadableDatabase();
			
			if (database != null) {
				database.execSQL(String.format("DELETE FROM `%s`;", table));
			}
		}
		
		/**
		 * Get the saved credidentials<br>
		 * Useful for API retry or pre-filling login text entry
		 * 
		 * @param withPassword
		 *            Tell if you want or not the password (used only for the API retry system)
		 * @return Saved credidentials, acting like a map, with key:
		 *         <ul>
		 *         <li>{@link #COLUMN_CREDIDENTIALS_USERNAME} ({@value #COLUMN_CREDIDENTIALS_USERNAME}) for username</li>
		 *         <li>{@link #COLUMN_CREDIDENTIALS_PASSWORD} ({@value #COLUMN_CREDIDENTIALS_PASSWORD}) for the password is you asked for it</li>
		 *         </ul>
		 */
		public ContentValues getSavedCredidentials(boolean withPassword) {
			SQLiteDatabase database = getReadableDatabase();
			if (database == null) {
				return null;
			}
			
			ContentValues row = new ContentValues();
			
			Cursor curor = database.rawQuery(String.format("SELECT * FROM `%s`", TABLE_CREDIDENTIALS), new String[] {});
			if (curor.moveToNext()) {
				row.put(COLUMN_CREDIDENTIALS_USERNAME, curor.getString(0));
				
				if (withPassword) {
					row.put(COLUMN_CREDIDENTIALS_PASSWORD, curor.getString(1));
				}
			}
			
			curor.close();
			database.close();
			
			return row;
		}
		
		/**
		 * Get a new {@link User} object but with same information as when saving
		 * 
		 * @return New {@link User} instance
		 */
		public User getSavedUser() {
			SQLiteDatabase database = getReadableDatabase();
			if (database == null) {
				return null;
			}
			
			User user = null;
			
			Cursor cursor = database.rawQuery(String.format("SELECT * FROM `%s`", TABLE_USER_INFO), new String[] {});
			if (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String username = cursor.getString(1);
				String email = cursor.getString(2);
				String token = cursor.getString(3);
				
				user = new User(id, token, username, email);
			}
			
			cursor.close();
			database.close();
			
			return user;
		}
		
		/**
		 * Quickly call {@link #registerCredidentials(String, String)} and {@link #registerUser(User)} combined, see their javadocs for more details
		 */
		protected boolean register(String username, String password, User user) {
			return registerCredidentials(username, password) && registerUser(user);
		}
		
		/**
		 * Save credidentials to the database
		 * 
		 * @param username
		 *            Target username
		 * @param password
		 *            Target password
		 * @return Success state
		 */
		protected boolean registerCredidentials(String username, String password) {
			SQLiteDatabase database = getWritableDatabase();
			if (database == null) {
				return false;
			}
			
			clearTable(TABLE_CREDIDENTIALS);
			
			ContentValues credidentialsRow = new ContentValues();
			credidentialsRow.put(COLUMN_CREDIDENTIALS_USERNAME, username);
			credidentialsRow.put(COLUMN_CREDIDENTIALS_PASSWORD, password);
			database.insert(TABLE_CREDIDENTIALS, null, credidentialsRow);
			
			database.close();
			
			return true;
		}
		
		/**
		 * Save a copy of every value of the {@link User} object for furur restoration
		 * 
		 * @param user
		 *            Target user
		 * @return Success state
		 */
		protected boolean registerUser(User user) {
			SQLiteDatabase database = getWritableDatabase();
			if (database == null || user == null) {
				return false;
			}
			
			clearTable(TABLE_USER_INFO);
			
			ContentValues userInfoRow = new ContentValues();
			userInfoRow.put(COLUMN_USER_INFO_USERNAME, user.getUsername());
			userInfoRow.put(COLUMN_USER_INFO_EMAIL, user.getEmail());
			userInfoRow.put(COLUMN_USER_INFO_TOKEN, user.getIdentificationToken());
			database.insert(TABLE_USER_INFO, null, userInfoRow);
			
			database.close();
			
			return true;
		}
		
		/**
		 * Completly remove all information about the {@link User} in the database.<br>
		 * Will call: {@link #clearDatabase()}
		 */
		protected void deleteUser() {
			clearDatabase();
		}
	}
	
}