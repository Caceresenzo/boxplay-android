package caceresenzo.apps.boxplay.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import caceresenzo.android.libs.menu.MenuTintUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.activities.test.AdsTestActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.application.Constants;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.fragments.culture.CultureFragment;
import caceresenzo.apps.boxplay.fragments.mylist.MyListFragment;
import caceresenzo.apps.boxplay.fragments.other.AdsFragment;
import caceresenzo.apps.boxplay.fragments.other.SettingsFragment;
import caceresenzo.apps.boxplay.fragments.other.about.AboutFragment;
import caceresenzo.apps.boxplay.fragments.premium.adult.AdultExplorerFragment;
import caceresenzo.apps.boxplay.fragments.social.SocialFragment;
import caceresenzo.apps.boxplay.fragments.store.StoreFragment;
import caceresenzo.apps.boxplay.fragments.store.StorePageFragment;
import caceresenzo.apps.boxplay.fragments.user.UserFragment;
import caceresenzo.apps.boxplay.helper.implementations.LocaleHelper;
import caceresenzo.apps.boxplay.services.BoxPlayForegroundService;
import caceresenzo.apps.boxplay.utils.Restorable;

/**
 * Main BoxPlay Activity class.
 * 
 * @author Enzo CACERES
 */
public class BoxPlayActivity extends BaseBoxPlayActivty implements NavigationView.OnNavigationItemSelectedListener {
	
	/* Tag */
	public static final String TAG = BoxPlayActivity.class.getSimpleName();
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_LAST_FRAGMENT_CLASS = "last_fragment_class";
	public static final String BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID = "last_drawer_selected_item_id";
	
	/* Instance */
	private static BoxPlayActivity INSTANCE;
	
	/* Views */
	private Toolbar toolbar;
	private DrawerLayout drawer;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private NavigationView navigationView;
	private Menu optionsMenu;
	
	/* Variables */
	private Bundle savedInstanceState;
	private String lastOpenFragment;
	private int lastDrawerSelectedItemId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boxplay);
		INSTANCE = this;
		
		initializeViews();
		
		if ((this.savedInstanceState = savedInstanceState) != null) {
			lastOpenFragment = savedInstanceState.getString(BUNDLE_KEY_LAST_FRAGMENT_CLASS);
			lastDrawerSelectedItemId = savedInstanceState.getInt(BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID, NO_VALUE);
			
			initializeRestoration();
		}
		
		ready();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (FRAGMENT_TO_OPEN != null) {
					if (FRAGMENT_TO_OPEN instanceof SettingsFragment) {
						final SettingsFragment settingsFragment = (SettingsFragment) FRAGMENT_TO_OPEN;
						
						settingsFragment.reset();
						
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								String lastKey = settingsFragment.getLastPreferenceKey();
								
								if (lastKey != null) {
									settingsFragment.scrollToPreference(lastKey);
								}
							}
						}, 200);
					}
					
					showFragment(FRAGMENT_TO_OPEN);
					
					FRAGMENT_TO_OPEN = null;
					
					if (MENUITEM_ID_TO_SELECT != NO_VALUE) {
						updateDrawerSelection(getMenuItemById(MENUITEM_ID_TO_SELECT));
						
						MENUITEM_ID_TO_SELECT = NO_VALUE;
					}
				}
				
				if (savedInstanceState != null) {
					final Fragment lastFragment = viewHelper.getLastFragment();
					
					if (lastFragment instanceof Restorable) {
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								((Restorable) lastFragment).restoreInstanceState(savedInstanceState);
								
								savedInstanceState.clear();
								savedInstanceState = null;
							}
						}, 200);
					}
				}
			}
		}, 200);
		
		menuHelper.recacheIds();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if (managers.getUpdateManager().isFirstRunOnThisUpdate()) {
			
			menuHelper.updateSeachMenu(R.id.drawer_boxplay_other_about);
			FRAGMENT_TO_OPEN = new AboutFragment().withChangeLog();
		}
		
		if (FRAGMENT_TO_OPEN == null) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					onNavigationItemSelected(navigationView.getMenu().findItem(R.id.drawer_boxplay_store_video));
					navigationView.getMenu().findItem(R.id.drawer_boxplay_store_video).setChecked(true);
				}
			}, 20);
		}
		
		managers.getUpdateManager().saveUpdateVersion();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		Fragment lastFragment = viewHelper.getLastFragment();
		
		if (lastFragment instanceof BaseBoxPlayFragment) {
			((BaseBoxPlayFragment) lastFragment).saveInstanceState(outState);
		}
		
		if (lastFragment != null) {
			outState.putString(BUNDLE_KEY_LAST_FRAGMENT_CLASS, lastFragment.getClass().getCanonicalName());
			outState.putInt(BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID, viewHelper.getLastFragmentMenuItemId());
		}
	}
	
	@Override
	protected void onDestroy() {
		if (managers.getUpdateManager().isFirstTimeInstalled()) {
			managers.getUpdateManager().updateFirstTimeInstalled();
		}
		
		managers.destroy();
		
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case Constants.REQUEST_ID.REQUEST_ID_VLC_VIDEO: {
				VideoActivity videoActivity = VideoActivity.getVideoActivity();
				if (videoActivity != null) {
					videoActivity.onActivityResult(requestCode, resultCode, data);
				}
				break;
			}
		}
	}
	
	/** Function to initialize views. */
	private void initializeViews() {
		toolbar = (Toolbar) findViewById(R.id.activity_boxplay_toolbar_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setElevation(0);
		
		drawer = (DrawerLayout) findViewById(R.id.activity_boxplay_drawerlayout_container);
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(actionBarDrawerToggle);
		actionBarDrawerToggle.syncState();
		
		navigationView = (NavigationView) findViewById(R.id.activity_boxplay_navigationview_container);
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.getMenu().getItem(0).setChecked(true);
		
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_boxplay_coordinatorlayout_container);
	}
	
	/** Function to help restoring activity. */
	private void initializeRestoration() {
		if (lastOpenFragment == null) {
			return;
		}
		
		try {
			FRAGMENT_TO_OPEN = (Fragment) Class.forName(lastOpenFragment).newInstance();
		} catch (Exception exception) {
			;
		}
		
		if (lastDrawerSelectedItemId != NO_VALUE) {
			MENUITEM_ID_TO_SELECT = lastDrawerSelectedItemId;
		}
	}
	
	private void initializeDebug() {
		if (!BoxPlayApplication.BUILD_DEBUG) {
			return;
		}
		
		optionsMenu.findItem(R.id.menu_main_action_debug).setVisible(true);
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// showFragment(new CultureFragment().withSearchAndGo());
			}
		}, 200);
	}
	
	private void onDebugClick(MenuItem menuItem) {
		// startActivity(new Intent(this, VideoPlayerActivity.class));
		
		// Manga
		// SearchAndGoDetailActivity.start(new SearchAndGoResult(ProviderManager.MANGALEL.create(), //
		// "Arifureta Shokugyou de Sekai Saikyou", //
		// "https://www.manga-lel.com/manga/arifureta-shokugyou-de-sekai-saikyou/", //
		// "https://www.manga-lel.com//uploads/manga/arifureta-shokugyou-de-sekai-saikyou/cover/cover_250x350.jpg")); //
		
		// MangaChapterReaderActivity.start(null);
		
		// Anime
		// SearchAndGoDetailActivity.start(new SearchAndGoResult(ProviderManager.JETANIME.create(), //
		// "Death March Kara Hajimaru Isekai Kyousoukyoku", //
		// "https://www.jetanime.co/anime/death-march-kara-hajimaru-isekai-kyousoukyoku/", //
		// "https://www.jetanime.co/assets/imgs/death-march-kara-hajimaru-isekai-kyousoukyoku.jpg")); //
		
		AdsTestActivity.start();
	}
	
	/** Used to show the menu. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu instanceof MenuBuilder) {
			((MenuBuilder) menu).setOptionalIconsVisible(true);
		}
		
		getMenuInflater().inflate(R.menu.main, menu);
		optionsMenu = menu;
		
		MenuTintUtils.tintAllIcons(optionsMenu, R.color.colorBackground, R.id.menu_main_action_search);
		
		initializeDebug();
		
		return true;
	}
	
	/** Function call when someone clicked on main menu item. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case R.id.menu_main_action_update: {
				managers.getUpdateManager().showDialog();
				return true;
			}
			
			case R.id.menu_main_action_force_service_call: {
				BoxPlayForegroundService.startIfNotAlready(this);
				return true;
			}
			
			case R.id.menu_main_action_logout: {
				// TODO managers.getIdentificationManager().logout();
				return true;
			}
			
			case R.id.menu_main_action_search: {
				StorePageFragment.handleSearch(item);
				break;
			}
			
			case R.id.menu_main_action_debug: {
				onDebugClick(item);
				break;
			}
			
			default: {
				boxPlayApplication.toast("[" + BoxPlayActivity.class.getSimpleName() + "]\nUnhandled onOptionsItemSelected(item.getTitle() = \"" + item.getTitle() + "\");").show();
				break;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Called when someone press the back button.<br>
	 * Added a custom behavior:
	 * <ul>
	 * <li>If someone has the option to open/collapse the drawer menu with the back button, application will never stop.</li>
	 * <li>If someone don't have this option, and the drawer is already close, the application will quit.</li>
	 * </ul>
	 */
	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			if (BoxPlayApplication.getBoxPlayApplication().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_key), true)) {
				drawer.openDrawer(GravityCompat.START);
			} else {
				super.onBackPressed();
			}
		}
	}
	
	/**
	 * Force a new selected item for the drawer.
	 * 
	 * @param id
	 *            Correspond to the id of the menu, if don't exists, nothing will append.
	 */
	public void forceFragmentPath(int id) {
		MenuItem targetItem = getMenuItemById(id);
		
		if (targetItem != null) {
			onNavigationItemSelected(targetItem);
		}
	}
	
	/**
	 * Get a MenuItem from the Drawer by its id.
	 * 
	 * @param id
	 *            Target id.
	 * @return Corresponding MenuItem, null if not found.
	 */
	public MenuItem getMenuItemById(int id) {
		return navigationView.getMenu().findItem(id);
	}
	
	public void updateDrawerSelection(MenuItem item) {
		if (item == null) {
			return;
		}
		
		int id = item.getItemId();
		
		if (item.isCheckable()) {
			menuHelper.unselectAllMenu();
			
			item.setChecked(true);
		}
		
		menuHelper.updateSeachMenu(id);
	}
	
	/**
	 * Drawer function, called when a item has been clicked.
	 */
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		Fragment actualFragment = viewHelper.getLastFragment();
		viewHelper.setLastFragmentMenuItemId(id);
		
		updateDrawerSelection(item);
		
		switch (id) {
			/* User */
			case R.id.drawer_boxplay_user_profile: {
				UserFragment userFragment;
				
				if (actualFragment instanceof UserFragment) {
					userFragment = ((UserFragment) actualFragment);
				} else {
					userFragment = new UserFragment();
				}
				
				switch (id) {
					default:
					case R.id.drawer_boxplay_user_profile: {
						userFragment.withProfile();
						break;
					}
				}
				
				showFragment(userFragment);
				break;
			}
			
			/* Store */
			case R.id.drawer_boxplay_store_video: {
				StoreFragment storeFragment;
				
				if (actualFragment instanceof StoreFragment) {
					storeFragment = ((StoreFragment) actualFragment);
				} else {
					storeFragment = new StoreFragment();
				}
				
				switch (id) {
					default:
					case R.id.drawer_boxplay_store_video: {
						storeFragment.withVideo();
						break;
					}
				}
				
				showFragment(storeFragment);
				break;
			}
			
			/* Social */
			case R.id.drawer_boxplay_connect_feed:
			case R.id.drawer_boxplay_connect_friends:
			case R.id.drawer_boxplay_connect_chat: {
				SocialFragment socialFragment;
				
				if (actualFragment instanceof SocialFragment) {
					socialFragment = ((SocialFragment) actualFragment);
				} else {
					socialFragment = new SocialFragment();
				}
				
				switch (id) {
					default:
					case R.id.drawer_boxplay_connect_feed: {
						socialFragment.withFeed();
						break;
					}
					case R.id.drawer_boxplay_connect_friends: {
						socialFragment.withFriend();
						break;
					}
					case R.id.drawer_boxplay_connect_chat: {
						socialFragment.withChat();
						break;
					}
				}
				
				showFragment(socialFragment);
				break;
			}
			
			/* Culture */
			case R.id.drawer_boxplay_culture_searchngo: {
				CultureFragment cultureFragment;
				
				if (actualFragment instanceof CultureFragment) {
					cultureFragment = ((CultureFragment) actualFragment);
				} else {
					cultureFragment = new CultureFragment();
				}
				
				switch (id) {
					default:
					case R.id.drawer_boxplay_culture_searchngo: {
						cultureFragment.withSearchAndGo();
						break;
					}
				}
				
				showFragment(cultureFragment);
				break;
			}
			
			/* Premium */
			// TODO: Do PremiumFragment instead of AdultExplorerFragment
			case R.id.drawer_boxplay_premium_adult: {
				if (managers.getPremiumManager().isPremiumUsable()) {
					showFragment(new AdultExplorerFragment());
				} else {
					managers.getPremiumManager().updateLicence(null);
					showFragment(actualFragment);
				}
				
				break;
			}
			
			/* My List */
			case R.id.drawer_boxplay_mylist_watchlater:
			case R.id.drawer_boxplay_mylist_subscriptions: {
				MyListFragment myListFragment;
				
				if (actualFragment instanceof MyListFragment) {
					myListFragment = ((MyListFragment) actualFragment);
				} else {
					myListFragment = new MyListFragment();
				}
				
				switch (id) {
					default:
					case R.id.drawer_boxplay_mylist_watchlater: {
						myListFragment.withWatchLater();
						break;
					}
					
					case R.id.drawer_boxplay_mylist_subscriptions: {
						myListFragment.withSubscriptions();
						break;
					}
				}
				
				showFragment(myListFragment);
				break;
			}
			
			/* Settings */
			case R.id.drawer_boxplay_other_settings: {
				SettingsFragment settingsFragment;
				
				if (actualFragment instanceof SettingsFragment) {
					settingsFragment = ((SettingsFragment) actualFragment);
				} else {
					settingsFragment = new SettingsFragment();
				}
				
				showFragment(settingsFragment);
				break;
			}
			
			/* Ads */
			case R.id.drawer_boxplay_other_ads: {
				AdsFragment adsFragment;
				
				if (actualFragment instanceof AdsFragment) {
					adsFragment = ((AdsFragment) actualFragment);
				} else {
					adsFragment = new AdsFragment();
				}
				
				showFragment(adsFragment);
				break;
			}
			
			/* About */
			case R.id.drawer_boxplay_other_about: {
				AboutFragment aboutFragment;
				
				if (actualFragment instanceof AboutFragment) {
					aboutFragment = ((AboutFragment) actualFragment);
				} else {
					aboutFragment = new AboutFragment();
				}
				
				showFragment(aboutFragment);
				break;
			}
			
			/* Default */
			default: {
				boxPlayApplication.toast("Unhandled onNavigationItemSelected(item.getTitle() = \"" + item.getTitle() + "\");").show();
				return false;
			}
		}
		
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
	
	/**
	 * Function used to fill the main {@link FrameLayout} of the application with a fragment instance.
	 * 
	 * @param fragment
	 *            The new fragment.
	 */
	public void showFragment(Fragment fragment) {
		if (fragment == null) {
			return;
		}
		
		try {
			FragmentManager fragmentManager = getSupportFragmentManager();
			
			fragmentManager //
					.beginTransaction() //
					.replace(R.id.activity_boxplay_framelayout_container_main, fragment) //
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) //
					.commit();
			
			viewHelper.setLastFragment(fragment);
		} catch (Exception exception) {
			FRAGMENT_TO_OPEN = fragment;
		}
	}
	
	public Toolbar getToolbar() {
		return toolbar;
	}
	
	public DrawerLayout getDrawer() {
		return drawer;
	}
	
	public ActionBarDrawerToggle getActionBarDrawerToggle() {
		return actionBarDrawerToggle;
	}
	
	public NavigationView getNavigationView() {
		return navigationView;
	}
	
	public Menu getOptionsMenu() {
		return optionsMenu;
	}
	
	/** @return Activity actual instance. */
	public static BoxPlayActivity getBoxPlayActivity() {
		return (BoxPlayActivity) INSTANCE;
	}
	
}