package caceresenzo.apps.boxplay.managers;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.activities.identification.LoginActivity;
import caceresenzo.apps.boxplay.managers.XManagers.AbstractManager;
import caceresenzo.libs.boxplay.api.BoxPlayApi;
import caceresenzo.libs.boxplay.users.User;

public class UserManager extends AbstractManager {
	
	/* Tag */
	public static final String TAG = UserManager.class.getSimpleName();
	
	private IdentificationManager identificationManager;
	
	/* Drawer Update */
	private BaseBoxPlayActivty attachedActivity;
	private User user;
	
	public UserManager() {
		this.identificationManager = new IdentificationManager();
	}
	
	@Override
	protected void initializeWhenUiReady(BaseBoxPlayActivty attachedActivity) {
		this.attachedActivity = attachedActivity;
		
		tryToUpdateDrawer();
	}
	
	@Override
	protected void initializeWhenUserLogged(final User user, BoxPlayApi boxPlayApi) {
		this.user = user;
		
		tryToUpdateDrawer();
	}
	
	/**
	 * Try to update the drawer as soon as the Attached Activity is a {@link BoxPlayActivity} instance, and the user is not null
	 */
	private void tryToUpdateDrawer() {
		if (attachedActivity instanceof BoxPlayActivity) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (user != null) {
						((BoxPlayActivity) attachedActivity).getNavigationView().getMenu().findItem(R.id.drawer_boxplay_user).setTitle(user.getUsername());
					} else {
						LoginActivity.start();
					}
				}
			});
		}
	}
	
}