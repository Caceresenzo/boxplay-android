package caceresenzo.apps.boxplay.activities.identification;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.managers.IdentificationManager;
import caceresenzo.apps.boxplay.managers.IdentificationManager.LoginCallback;
import caceresenzo.apps.boxplay.managers.IdentificationManager.LoginSubManager;
import caceresenzo.apps.boxplay.managers.IdentificationManager.UserDatabaseHelper;
import caceresenzo.libs.boxplay.api.request.implementations.user.UserApiRequest;
import caceresenzo.libs.boxplay.api.response.ApiResponse;
import caceresenzo.libs.boxplay.users.User;
import caceresenzo.libs.string.StringUtils;

public class LoginActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = LoginActivity.class.getSimpleName();
	
	/* Constants */
	private static final int REQUEST_ID_SIGNUP = 0;
	
	/* Instance */
	private static LoginActivity INSTANCE;
	
	/* Managers */
	private IdentificationManager identificationManager;
	private LoginSubManager loginSubManager;
	
	/* Views */
	private EditText usernameEditText, passwordEditText;
	private Button loginButton;
	private TextView registerLinkTextView;
	
	/* Dialog */
	private WorkingProgressDialog workingProgressDialog;
	
	/* Constructor */
	public LoginActivity() {
		super();
		
		this.identificationManager = managers.getIdentificationManager();
		this.loginSubManager = identificationManager.getLoginSubManager();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		INSTANCE = this;
		
		this.workingProgressDialog = WorkingProgressDialog.create(this);
		this.workingProgressDialog.update(R.string.boxplay_identification_login_working);
		
		initializeViews();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
	}
	
	/* Initialize Views */
	private void initializeViews() {
		usernameEditText = (EditText) findViewById(R.id.activity_login_edittext_username);
		passwordEditText = (EditText) findViewById(R.id.activity_login_edittext_password);
		
		loginButton = (Button) findViewById(R.id.activity_login_button_login);
		
		registerLinkTextView = (TextView) findViewById(R.id.activity_login_textview_register);
		
		ContentValues contentValues = identificationManager.getUserDatabaseHelper().getSavedCredidentials(false);
		if (contentValues.containsKey(UserDatabaseHelper.COLUMN_CREDIDENTIALS_USERNAME)) {
			usernameEditText.setText(contentValues.getAsString(UserDatabaseHelper.COLUMN_CREDIDENTIALS_USERNAME));
		}
		
		initializeListeners();
	}
	
	/* Initialize Listeners */
	private void initializeListeners() {
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				login();
			}
		});
		
		registerLinkTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivityForResult(intent, REQUEST_ID_SIGNUP);
			}
		});
	}
	
	/**
	 * Start the login sequence
	 */
	private void login() {
		Log.d(TAG, "Starting login");
		
		if (!validate()) {
			return;
		}
		
		loginButton.setEnabled(false);
		
		workingProgressDialog.show();
		
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		loginSubManager.login(username, password, new LoginCallback() {
			@Override
			public void onApiResponse(final ApiResponse<User> apiResponse, User user) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (apiResponse.isSuccess()) {
							onLoginSuccess();
						} else {
							onLoginFailed(apiResponse);
						}
						
						workingProgressDialog.hide();
					}
				});
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ID_SIGNUP: {
				if (resultCode == RESULT_OK && data.getExtras() != null) {
					Bundle extras = data.getExtras();

					String username = extras.getString(RegisterActivity.BUNDLE_KEY_USER_USERNAME);
					String password = extras.getString(RegisterActivity.BUNDLE_KEY_USER_PASSWORD);
					
					usernameEditText.setText(username);
					passwordEditText.setText(password);
					
					login();
				}
				break;
			}
			
			default: {
				break;
			}
		}
	}
	
	@Override /* Disable back button */
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	/**
	 * Called when the login has returned a success
	 */
	private void onLoginSuccess() {
		loginButton.setEnabled(true);
		
		finish();
	}
	
	/**
	 * Called when the login has failed
	 */
	private void onLoginFailed(ApiResponse<User> sourceRequest) {
		loginButton.setEnabled(true);
		
		boxPlayApplication.toast(viewHelper.enumToStringCacheTranslation(sourceRequest.getStatus())).show();
	}
	
	/**
	 * Validate all input and show an error if necessary
	 * 
	 * @return If the login can continue: inputs respect formattings rules
	 */
	private boolean validate() {
		boolean valid = true;
		
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		if (!StringUtils.validate(username) || !username.matches(UserApiRequest.USERNAME_PATTERN)) {
			valid = false;
			
			String error;
			if (username.length() < 4) {
				error = getString(R.string.boxplay_identification_input_error_username_too_short);
			} else if (username.length() > 16) {
				error = getString(R.string.boxplay_identification_input_error_username_too_long);
			} else {
				error = getString(R.string.boxplay_identification_input_error_username_illegal_character);
			}
			
			usernameEditText.setError(error);
		} else {
			usernameEditText.setError(null);
		}
		
		if (!StringUtils.validate(password) || password.length() < 4) {
			valid = false;
			
			passwordEditText.setError(getString(R.string.boxplay_identification_input_error_password_too_short));
		} else {
			passwordEditText.setError(null);
		}
		
		return valid;
	}
	
	/**
	 * Start a new {@link LoginActivity}
	 */
	public static void start() {
		if (INSTANCE != null) {
			return; /* Already started */
		}
		
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		application.startActivity(intent);
	}
	
}