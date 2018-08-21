package caceresenzo.apps.boxplay.managers;

import java.io.IOException;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import caceresenzo.android.libs.internet.AndroidUpdater;
import caceresenzo.android.libs.internet.AndroidUpdater.OnUpdateStateChange;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.bytes.ByteFormat;
import caceresenzo.libs.comparator.Version;
import caceresenzo.libs.comparator.VersionType;
import caceresenzo.libs.json.JsonObject;
import caceresenzo.libs.network.Downloader;
import caceresenzo.libs.parse.ParseUtils;
import caceresenzo.libs.thread.ThreadUtils;

public class UpdateManager extends AbstractManager {
	
	private static final String TAG = UpdateManager.class.getSimpleName();
	
	protected static final String PREF_KEY_JUST_INSTALLED = "update_just_installed";
	protected static final String VERSION_KEY = "update_last_version_code";
	protected static final int NO_VERSION = -1;
	
	private DataManager dataManager;
	
	private AlertDialog.Builder updateAlertDialogBuilder;
	private AlertDialog updateAlertDialog;
	
	private String rawUpdateMessageFormat;
	
	private Version lastVersion;
	private VersionType lastVersionType;
	private String lastVersionName, lastVersionUrl, lastVersionSize;
	private long lastVersionLength;
	private boolean dialogReady = false, updateAvailable = false, downloadStarted = false;
	private int downloadedPourcent = 0;
	
	private static boolean alreadyInformedUser = false, firstTimeInstalled;
	
	private int lastVersionCode, currentVersionCode;
	// private String currentVersionName;
	
	public void initialize() {
		dataManager = getManagers().getDataManager();
		rawUpdateMessageFormat = getString(R.string.boxplay_update_available_message_format);
		lastVersionSize = getString(R.string.boxplay_update_available_message_format_size_searching);
		
		lastVersionCode = getManagers().getPreferences().getInt(VERSION_KEY, NO_VERSION);
		
		try {
			PackageInfo packageInfo = boxPlayApplication.getPackageManager().getPackageInfo(boxPlayApplication.getPackageName(), 0);
			
			currentVersionCode = packageInfo.versionCode;
			// currentVersionName = packageInfo.versionName;
		} catch (NameNotFoundException exception) {
			currentVersionCode = NO_VERSION;
			Log.e(TAG, "Could not get version information from manifest!", exception);
		}
		
		try {
			firstTimeInstalled = getManagers().getPreferences().contains(PREF_KEY_JUST_INSTALLED);
		} catch (Exception exception) {
			;
		}
	}
	
	public boolean isFirstRunOnThisUpdate() {
		return lastVersionCode < currentVersionCode;
	}
	
	public void debugForceFirstTimeInstalled() {
		firstTimeInstalled = true;
	}
	
	public boolean isFirstTimeInstalled() {
		return firstTimeInstalled;
	}
	
	public void updateFirstTimeInstalled() {
		getManagers().getPreferences().edit().putBoolean(PREF_KEY_JUST_INSTALLED, true).commit();
	}
	
	public void saveUpdateVersion() {
		getManagers().getPreferences().edit().putInt(VERSION_KEY, currentVersionCode).commit();
	}
	
	public void prepareDialogBuild() {
		Thread buildingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (!dataManager.isWorkableDataReady()) {
					ThreadUtils.sleep(100L);
				}
				
				BoxPlayApplication.getHandler().post(new Runnable() {
					@Override
					public void run() {
						buildDialog();
						
						if (dialogReady) {
							informUser();
						}
					}
				});
				
				while (!dialogReady) {
					ThreadUtils.sleep(100L);
				}
				
				if (!updateAvailable) {
					return;
				}
				
				try {
					lastVersionSize = ByteFormat.toHumanBytes(lastVersionLength = Downloader.getFileSize(lastVersionUrl), 2);
				} catch (IOException exception) {
					exception.printStackTrace();
					lastVersionSize = getString(R.string.boxplay_update_available_message_format_size_searching_error) + " [" + exception.getLocalizedMessage() + "]";
				}
				
				if (!downloadStarted) {
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							updateDialogMessage();
						}
					});
				}
			}
		});
		buildingThread.start();
	}
	
	private void buildDialog() {
		parseJson();
		
		updateAlertDialogBuilder = new AlertDialog.Builder(boxPlayApplication.getAttachedActivity());
		updateAlertDialogBuilder.setTitle(getString(R.string.boxplay_menu_action_update));
		updateAlertDialogBuilder.setMessage(formatDialogMessage());
		
		updateAlertDialogBuilder.setPositiveButton(getString(R.string.boxplay_update_available_download), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				; // Will be remplaced
			}
		});
		updateAlertDialogBuilder.setNegativeButton(getString(R.string.boxplay_update_available_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		updateAlertDialog = updateAlertDialogBuilder.create();
		updateAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				Button button = ((AlertDialog) updateAlertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (downloadStarted) {
							return;
						}
						
						boxPlayApplication.toast(R.string.boxplay_update_available_download_starting).show();
						downloadStarted = true;
						AndroidUpdater.updateIfNeeded(boxPlayApplication.getAttachedActivity(), "BoxPlay", BoxPlayApplication.getVersion(), lastVersion, lastVersionUrl, new OnUpdateStateChange() {
							@Override
							public void onProgress(int length) {
								downloadedPourcent = (int) (length * 100f / lastVersionLength);
								BoxPlayApplication.getHandler().post(new Runnable() {
									@Override
									public void run() {
										updateDialogMessage();
									}
								});
							}
							
							@Override
							public void onInstall() {
								BoxPlayApplication.getHandler().post(new Runnable() {
									@Override
									public void run() {
										boxPlayApplication.toast(R.string.boxplay_update_available_install).show();
										updateAlertDialog.dismiss();
									}
								});
							}
							
							@Override
							public void onFinish() {
								;
							}
							
							@Override
							public void onFailed(final Exception exception) {
								BoxPlayApplication.getHandler().post(new Runnable() {
									@Override
									public void run() {
										boxPlayApplication.toast(R.string.boxplay_update_available_download_failed, exception.getLocalizedMessage()).show();
									}
								});
							}
						}, BoxPlayApplication.REQUEST_ID_UPDATE);
						
					}
				});
			}
		});
		
		dialogReady = true;
	}
	
	private void parseJson() {
		JsonObject json = dataManager.getJsonData();
		
		try { // Can be null or cast exception
			HashMap<?, ?> updateMap = (HashMap<?, ?>) json.get("update");
			
			lastVersionType = VersionType.fromString(ParseUtils.parseString(updateMap.get("update_last_version_type"), null));
			lastVersion = new Version(ParseUtils.parseString(updateMap.get("update_last_version"), null), lastVersionType);
			lastVersionName = ParseUtils.parseString(updateMap.get("update_name"), null);
			lastVersionUrl = ParseUtils.parseString(updateMap.get("update_file_url"), null);
		} catch (Exception exception) {
			return;
		}
		
		if (lastVersion == null || lastVersionName == null || lastVersionUrl == null) {
			return;
		}
		
		updateAvailable = lastVersion.compareTo(BoxPlayApplication.getVersion()) == 1;
	}
	
	public void showDialog() {
		showDialog(false);
	}
	
	private void showDialog(boolean ignoreVerifs) {
		if (!ignoreVerifs) {
			if (!dialogReady) {
				return;
			}
			if (!informUser()) {
				return;
			}
		}
		updateAlertDialog.show();
	}
	
	public boolean informUser() {
		if (!updateAvailable) {
			boxPlayApplication.snackbar(R.string.boxplay_update_uptodate, Snackbar.LENGTH_LONG).show();
			return false;
		}
		
		if (!alreadyInformedUser) {
			alreadyInformedUser = true;
			
			boxPlayApplication.snackbar(getString(R.string.boxplay_update_available, lastVersion.get()), 8000).setAction(getString(R.string.boxplay_update_available_download), new OnClickListener() {
				@Override
				public void onClick(View view) {
					showDialog(true);
				}
			}).show();
		}
		
		return true;
	}
	
	private void updateDialogMessage() {
		updateAlertDialog.setMessage(formatDialogMessage());
	}
	
	private String formatDialogMessage() {
		if (updateAvailable) {
			return rawUpdateMessageFormat //
					.replace("_version_", lastVersion.get(true)) //
					.replace("_name_", lastVersionName) //
					.replace("_size_", lastVersionSize) //
					.replace("_downloaded_", downloadedPourcent + "%") //
			;
		}
		
		return "";
	}
	
}