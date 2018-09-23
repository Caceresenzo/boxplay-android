package caceresenzo.apps.boxplay.activities.identification;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.managers.IdentificationManager.RegisterSubManager;
import caceresenzo.libs.boxplay.api.request.implementations.user.UserApiRequest;
import caceresenzo.libs.string.StringUtils;

public class RegisterActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = RegisterActivity.class.getSimpleName();
	
	/* Bundle */
	public static final String BUNDLE_USER = "user";
	
	/* Managers */
	private RegisterSubManager registerSubManager;
	
	/* Views */
	private EditText usernameEditText, emailEditText, passwordEditText;
	private Button registerButton;
	private TextView loginBackTextView;
	
	/* Constructor */
	public RegisterActivity() {
		super();
		
		this.registerSubManager = managers.getIdentificationManager().getRegisterSubManager();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
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
	
	private void register() {
		Log.d(TAG, "Starting registration");
		
		if (!validate()) {
			onSignupFailed();
			return;
		}
		
		registerButton.setEnabled(false);
		
		final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Creating Account...");
		progressDialog.show();
		
		String name = usernameEditText.getText().toString();
		String email = emailEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		// TODO: Implement your own signup logic here.
		
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				// On complete call either onSignupSuccess or onSignupFailed
				// depending on success
				onSignupSuccess();
				// onSignupFailed();
				progressDialog.dismiss();
			}
		}, 3000);
	}
	
	public void onSignupSuccess() {
		registerButton.setEnabled(true);
		setResult(RESULT_OK, null);
		finish();
	}
	
	public void onSignupFailed() {
		Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
		
		registerButton.setEnabled(true);
	}
	
	public boolean validate() {
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