package caceresenzo.apps.boxplay.managers;

import java.lang.reflect.Method;

import android.Manifest;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;

public class PermissionManager extends AbstractManager {
	
	public static final String TAG = PermissionManager.class.getSimpleName();
	
	@Override
	protected void initialize() {
		hackSecureMode();
	}
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		askPermission();
	}
	
	private void askPermission() {
		if (Build.VERSION.SDK_INT >= 23) {
			try {
				String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.REQUEST_INSTALL_PACKAGES };
				boxPlayApplication.getAttachedActivity().requestPermissions(permissions, BoxPlayApplication.REQUEST_ID_PERMISSION);
			} catch (Exception exception) {
				Log.e(TAG, "Failed to request permissions", exception);
			}
		}
	}
	
	private void hackSecureMode() {
		if (Build.VERSION.SDK_INT >= 24) {
			try {
				Method method = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
				method.invoke(null);
			} catch (Exception exception) {
				Log.e(TAG, "Failed to call disableDeathOnFileUriExposure()", exception);
			}
		}
	}
	
}