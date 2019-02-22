package caceresenzo.apps.boxplay.activities.identification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.managers.IdentificationManager.RegisterCallback;
import caceresenzo.apps.boxplay.managers.IdentificationManager.RegisterSubManager;
import caceresenzo.libs.boxplay.api.request.implementations.user.UserApiRequest;
import caceresenzo.libs.boxplay.api.response.ApiResponse;
import caceresenzo.libs.string.StringUtils;

public class RegisterActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = RegisterActivity.class.getSimpleName();
	
	/* Bundle keys */
	public static final String BUNDLE_KEY_USER_USERNAME = "username";
	public static final String BUNDLE_KEY_USER_PASSWORD = "password";
	
	/* Managers */
	private RegisterSubManager registerSubManager;
	
	/* Views */
	private EditText usernameEditText, emailEditText, passwordEditText;
	private Button registerButton;
	private TextView loginBackTextView;
	
	/* Dialog */
	private WorkingProgressDialog workingProgressDialog;
	
	/* Constructor */
	public RegisterActivity() {
		super();
		
		this.registerSubManager = managers.getIdentificationManager().getRegisterSubManager();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		this.workingProgressDialog = WorkingProgressDialog.create(this);
		this.workingProgressDialog.update(R.string.boxplay_identification_register_working);
		
		initializeViews();
	}
	
	/* Initialize Views */
	private void initializeViews() {
		usernameEditText = (EditText) findViewById(R.id.activity_register_edittext_username);
		emailEditText = (EditText) findViewById(R.id.activity_register_edittext_email);
		passwordEditText = (EditText) findViewById(R.id.activity_register_edittext_password);
		
		registerButton = (Button) findViewById(R.id.activity_register_button_register);
		
		loginBackTextView = (TextView) findViewById(R.id.activity_register_textview_login);
		
		initializeListeners();
	}
	
	/* Initialize Listeners */
	private void initializeListeners() {
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register();
			}
		});
		
		loginBackTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
	
	/**
	 * Start the register sequence
	 */
	private void register() {
		Log.d(TAG, "Starting registration");
		
		if (!validate()) {
			return;
		}
		
		registerButton.setEnabled(false);
		
		workingProgressDialog.show();
		
		final String username = usernameEditText.getText().toString();
		String email = emailEditText.getText().toString();
		final String password = passwordEditText.getText().toString();
		
		registerSubManager.register(username, email, password, new RegisterCallback() {
			@Override
			public void onApiResponse(final ApiResponse<?> apiResponse) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (apiResponse.isSuccess()) {
							onRegisterSuccess(username, password);
						} else {
							onRegisterFailed(apiResponse);
						}
						
						workingProgressDialog.hide();
					}
				});
			}
		});
	}
	
	/**
	 * Called when the register has returned a success
	 */
	private void onRegisterSuccess(String username, String password) {
		registerButton.setEnabled(true);
		
		Intent dataIntent = new Intent();
		dataIntent.putExtra(BUNDLE_KEY_USER_USERNAME, username);
		dataIntent.putExtra(BUNDLE_KEY_USER_PASSWORD, password);
		setResult(RESULT_OK, dataIntent);
		
		finish();
	}
	
	/**
	 * Called when the register has failed
	 */
	private void onRegisterFailed(ApiResponse<?> sourceRequest) {
		registerButton.setEnabled(true);
		
		boxPlayApplication.toast(cacheHelper.translate(sourceRequest.getStatus())).show();
	}
	
	/**
	 * Validate all input and show an error if necessary
	 * 
	 * @return If the register can continue: inputs respect formattings rules
	 */
	private boolean validate() {
		boolean valid = true;
		
		String name = usernameEditText.getText().toString();
		String email = emailEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		if (!StringUtils.validate(name) || !name.matches(UserApiRequest.USERNAME_PATTERN)) {
			valid = false;
			
			String error;
			if (name.length() < 4) {
				error = getString(R.string.boxplay_identification_input_error_username_too_short);
			} else if (name.length() > 16) {
				error = getString(R.string.boxplay_identification_input_error_username_too_long);
			} else {
				error = getString(R.string.boxplay_identification_input_error_username_illegal_character);
			}
			
			usernameEditText.setError(error);
		} else {
			usernameEditText.setError(null);
		}
		
		if (!StringUtils.validate(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			valid = false;
			
			emailEditText.setError(getString(R.string.boxplay_identification_input_error_email_invalid));
		} else {
			emailEditText.setError(null);
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