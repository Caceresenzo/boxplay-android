package caceresenzo.apps.boxplay.activities.identification;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.managers.IdentificationManager;
import caceresenzo.apps.boxplay.managers.IdentificationManager.LoginCallback;
import caceresenzo.apps.boxplay.managers.IdentificationManager.LoginSubManager;
import caceresenzo.apps.boxplay.managers.IdentificationManager.UserDatabaseHelper;
import caceresenzo.libs.boxplay.api.ApiResponse;
import caceresenzo.libs.boxplay.api.request.implementations.user.UserApiRequest;
import caceresenzo.libs.boxplay.users.User;
import caceresenzo.libs.string.StringUtils;

public class LoginActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = LoginActivity.class.getSimpleName();
	
	/* Constants */
	private static final int REQUEST_ID_SIGNUP = 0;
	
	/* Managers */
	private IdentificationManager identificationManager;
	private LoginSubManager loginSubManager;
	
	/* Views */
	private EditText usernameEditText, passwordEditText;
	private Button loginButton;
	private TextView registerLinkTextView;
	
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
		
		initializeViews();
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
	
	private void login() {
		Log.d(TAG, "Starting login");
		
		if (!validate()) {
			onLoginFailed();
			return;
		}
		
		loginButton.setEnabled(false);
		
		final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Authenticating...");
		progressDialog.show();
		
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
							onLoginFailed();
						}
						
						progressDialog.dismiss();
					}
				});
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ID_SIGNUP) {
			if (resultCode == RESULT_OK) {
				
				// TODO: Implement successful signup logic here
				// By default we just finish the Activity and log them in automatically
				this.finish();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	private void onLoginSuccess() {
		loginButton.setEnabled(true);
		finish();
	}
	
	private void onLoginFailed() {
		Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
		
		loginButton.setEnabled(true);
	}
	
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
	
}