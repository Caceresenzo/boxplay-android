package caceresenzo.apps.boxplay.activities;

import java.util.ArrayList;
import java.util.List;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;
import caceresenzo.apps.boxplay.fragments.culture.CultureFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.PageCultureSearchAndGoFragment;
import caceresenzo.apps.boxplay.fragments.mylist.MyListFragment;
import caceresenzo.apps.boxplay.fragments.other.SettingsFragment;
import caceresenzo.apps.boxplay.fragments.other.about.AboutFragment;
import caceresenzo.apps.boxplay.fragments.premium.adult.AdultExplorerFragment;
import caceresenzo.apps.boxplay.fragments.social.SocialFragment;
import caceresenzo.apps.boxplay.fragments.store.StoreFragment;
import caceresenzo.apps.boxplay.fragments.store.StorePageFragment;
import caceresenzo.apps.boxplay.helper.LocaleHelper;
import caceresenzo.apps.boxplay.managers.TutorialManager.Tutorialable;
import caceresenzo.libs.boxplay.culture.searchngo.providers.ProviderManager;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;

/**
 * Main BoxPlay class
 * 
 * @author Enzo CACERES
 */
public class BoxPlayActivity extends BaseBoxPlayActivty implements NavigationView.OnNavigationItemSelectedListener, Tutorialable {
	
	/* Set Build as Debug */
	public static final boolean BUILD_DEBUG = false;
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_LAST_FRAGMENT_CLASS = "last_fragment_class";
	public static final String BUNDLE_KEY_LAST_FRAGMENT_TAB_ID = "last_fragment_tab_id";
	public static final String BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID = "last_drawer_selected_item_id";
	public static final String BUNDLE_KEY_EXTRA_FRAGMENT_CULTURE_SEARCHANDGO_LAST_QUERY = "extra_fragment_culture_searchandgo_last_query";
	
	/* Tutorial Path Ids */
	public static final int TUTORIAL_PROGRESS_DRAWER = 0;
	public static final int TUTORIAL_PROGRESS_SEARCH = 1;
	public static final int TUTORIAL_PROGRESS_MENU = 2;
	public static final int TUTORIAL_PROGRESS_SWIPE = 3;
	public static final int TUTORIAL_PROGRESS_VIDEO = 4;
	public static final int TUTORIAL_PROGRESS_MUSIC = 5;
	
	/* Instance */
	private static BoxPlayActivity INSTANCE;
	
	/* Views */
	private Toolbar toolbar;
	private DrawerLayout drawer;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private NavigationView navigationView;
	private Menu optionsMenu;
	
	private SlidingUpPanelLayout slidingUpPanelLayout;
	
	/* Variables */
	private String lastOpenFragment, extraFragmentCultureSearchAndGoLastQuery;
	private int lastOpenTab, lastDrawerSelectedItemId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null); // Dynamic NavigationView
		setContentView(R.layout.activity_boxplay);
		INSTANCE = this;
		
		initializeViews();
		initializeSystems();
		
		if (savedInstanceState != null) {
			lastOpenFragment = savedInstanceState.getString(BUNDLE_KEY_LAST_FRAGMENT_CLASS);
			lastOpenTab = savedInstanceState.getInt(BUNDLE_KEY_LAST_FRAGMENT_TAB_ID, NO_VALUE);
			lastDrawerSelectedItemId = savedInstanceState.getInt(BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID, NO_VALUE);
			extraFragmentCultureSearchAndGoLastQuery = savedInstanceState.getString(BUNDLE_KEY_EXTRA_FRAGMENT_CULTURE_SEARCHANDGO_LAST_QUERY);
			
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
					//
					else if (FRAGMENT_TO_OPEN instanceof CultureFragment) {
						final CultureFragment cultureFragment = (CultureFragment) FRAGMENT_TO_OPEN;
						
						if (CultureFragment.PAGE_SEARCHANDGO == lastOpenTab && extraFragmentCultureSearchAndGoLastQuery != null) {
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									if (cultureFragment.getActualFragment() instanceof PageCultureSearchAndGoFragment) {
										PageCultureSearchAndGoFragment searchAndGoFragment = (PageCultureSearchAndGoFragment) cultureFragment.getActualFragment();
										
										searchAndGoFragment.applyQuery(extraFragmentCultureSearchAndGoLastQuery);
									}
								}
							}, 200);
						}
					}
					
					showFragment(FRAGMENT_TO_OPEN);
					
					FRAGMENT_TO_OPEN = null;
				}
				
				if (MENUITEM_ID_TO_SELECT != NO_VALUE) {
					updateDrawerSelection(getMenuItemById(MENUITEM_ID_TO_SELECT));
					
					MENUITEM_ID_TO_SELECT = NO_VALUE;
				}
			}
		}, 200);
		
		managers.getMusicManager().registerMusicSlidingPanel(slidingUpPanelLayout);
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		onNavigationItemSelected(navigationView.getMenu().findItem(R.id.drawer_boxplay_store_video));
		navigationView.getMenu().findItem(R.id.drawer_boxplay_store_video).setChecked(true);
		
		// getManagers().getUpdateManager().debugForceFirstTimeInstalled();
		if (managers.getUpdateManager().isFirstTimeInstalled()) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					managers.getTutorialManager().executeActivityTutorial(BoxPlayActivity.this);
				}
			}, 1000);
		} else {
			if (managers.getUpdateManager().isFirstRunOnThisUpdate()) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						helper.updateSeachMenu(R.id.drawer_boxplay_other_about);
						showFragment(new AboutFragment().withChangeLog());
					}
					
				}, 3000);
			}
		}
		managers.getUpdateManager().saveUpdateVersion();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		Fragment lastFragment = helper.getLastFragment();
		if (lastFragment != null) {
			outState.putString(BUNDLE_KEY_LAST_FRAGMENT_CLASS, lastFragment.getClass().getCanonicalName());
			
			if (lastFragment instanceof BaseTabLayoutFragment) {
				outState.putInt(BUNDLE_KEY_LAST_FRAGMENT_TAB_ID, ((BaseTabLayoutFragment) lastFragment).getLastOpenPosition());
			}
			
			outState.putInt(BUNDLE_KEY_LAST_DRAWER_SELECTED_ITEM_ID, helper.getLastFragmentMenuItemId());
			
			if (lastFragment instanceof CultureFragment) {
				CultureFragment cultureFragment = (CultureFragment) lastFragment;
				
				if (cultureFragment.getActualFragment() instanceof PageCultureSearchAndGoFragment) {
					outState.putString(BUNDLE_KEY_EXTRA_FRAGMENT_CULTURE_SEARCHANDGO_LAST_QUERY, ((PageCultureSearchAndGoFragment) cultureFragment.getActualFragment()).getActualQuery());
				}
			}
		}
	}
	
	@Override
	protected void onStop() {
		// MANAGERS.getMusicManager().saveDatabase();
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		if (managers.getUpdateManager().isFirstTimeInstalled()) {
			managers.getUpdateManager().updateFirstTimeInstalled();
		}
		
		managers.getMusicManager().saveDatabase();
		
		managers.destroy();
		
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case BoxPlayApplication.REQUEST_ID_VLC_VIDEO: {
				VideoActivity videoActivity = VideoActivity.getVideoActivity();
				if (videoActivity != null) {
					videoActivity.onActivityResult(requestCode, resultCode, data);
				}
				break;
			}
		}
	}
	
	/**
	 * Function to initialize views
	 */
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
		
		slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.activity_boxplay_slidinglayout_container);
	}
	
	/**
	 * Function to initialize sub-systems, like cache
	 */
	private void initializeSystems() {
		helper.prepareCache(boxPlayApplication);
	}
	
	/**
	 * Function to help restoring activity
	 */
	private void initializeRestoration() {
		if (lastOpenFragment == null) {
			return;
		}
		
		try {
			FRAGMENT_TO_OPEN = (Fragment) Class.forName(lastOpenFragment).newInstance();
			
			if (FRAGMENT_TO_OPEN instanceof BaseTabLayoutFragment && lastOpenTab != NO_VALUE) {
				((BaseTabLayoutFragment) FRAGMENT_TO_OPEN).withPage(lastOpenTab);
			}
		} catch (Exception exception) {
			;
		}
		
		if (lastDrawerSelectedItemId != NO_VALUE) {
			MENUITEM_ID_TO_SELECT = lastDrawerSelectedItemId;
		}
		
		if (extraFragmentCultureSearchAndGoLastQuery != null) {
			if (extraFragmentCultureSearchAndGoLastQuery == " " || extraFragmentCultureSearchAndGoLastQuery.isEmpty()) {
				extraFragmentCultureSearchAndGoLastQuery = null;
			}
		}
	}
	
	private void initializeDebug() {
		if (!BUILD_DEBUG) {
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
		SearchAndGoDetailActivity.start(new SearchAndGoResult(ProviderManager.MANGALEL.create(), //
				"Arifureta Shokugyou de Sekai Saikyou", //
				"https://www.manga-lel.com/manga/arifureta-shokugyou-de-sekai-saikyou/", //
				"https://www.manga-lel.com//uploads/manga/arifureta-shokugyou-de-sekai-saikyou/cover/cover_250x350.jpg")); //
		
		// MangaChapterReaderActivity.start(null);
		
		// Anime
		// SearchAndGoDetailActivity.start(new SearchAndGoResult(ProviderManager.JETANIME.create(), //
		// "Death March Kara Hajimaru Isekai Kyousoukyoku", //
		// "https://www.jetanime.co/anime/death-march-kara-hajimaru-isekai-kyousoukyoku/", //
		// "https://www.jetanime.co/assets/imgs/death-march-kara-hajimaru-isekai-kyousoukyoku.jpg")); //
	}
	
	/**
	 * Used to show the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu instanceof MenuBuilder) {
			((MenuBuilder) menu).setOptionalIconsVisible(true);
		}
		
		getMenuInflater().inflate(R.menu.main, menu);
		optionsMenu = menu;
		
		initializeDebug();
		
		return true;
	}
	
	/**
	 * Function call when someone clicked on main menu item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case R.id.menu_main_action_update: {
				managers.getUpdateManager().showDialog();
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
				boxPlayApplication.toast("Unhandled onOptionsItemSelected(item.getTitle() = \"" + item.getTitle() + "\");").show();
				break;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Called when someone press the back button
	 * 
	 * Added a custom behavior:
	 * 
	 * If someone has the option to open/collapse the drawer menu with the back button, application will never stop
	 * 
	 * If someone don't have this option, and the drawer is already close, the application will quit
	 */
	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			if (slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED) && !slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.HIDDEN)) {
				slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
			} else {
				if (BoxPlayApplication.getBoxPlayApplication().getPreferences().getBoolean(getString(R.string.boxplay_other_settings_menu_pref_drawer_extend_collapse_back_button_key), true)) {
					drawer.openDrawer(GravityCompat.START);
				} else {
					super.onBackPressed();
				}
			}
		}
	}
	
	/**
	 * Force a new selected item for the drawer
	 * 
	 * @param id
	 *            Correspond to the id of the menu, if don't exists, nothing will append
	 */
	public void forceFragmentPath(int id) {
		MenuItem targetItem = getMenuItemById(id);
		
		if (targetItem != null) {
			onNavigationItemSelected(targetItem);
		}
	}
	
	/**
	 * Get a MenuItem from the Drawer by its id
	 * 
	 * @param id
	 *            Target id
	 * @return Corresponding MenuItem, null if not found
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
			helper.unselectAllMenu();
			
			item.setChecked(true);
		}
		
		helper.updateSeachMenu(id);
	}
	
	/**
	 * Drawer function, called when a item has been clicked
	 */
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		Fragment actualFragment = helper.getLastFragment();
		helper.setLastFragmentMenuItemId(id);
		
		updateDrawerSelection(item);
		
		switch (id) {
			/* Store */
			case R.id.drawer_boxplay_store_video:
			case R.id.drawer_boxplay_store_music: {
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
					case R.id.drawer_boxplay_store_music: {
						storeFragment.withMusic();
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
				if (managers.getPremiumManager().isPremiumKeyValid()) {
					showFragment(new AdultExplorerFragment());
				} else {
					managers.getPremiumManager().updateLicence(null);
					showFragment(actualFragment);
				}
				
				break;
			}
			
			/* My List */
			case R.id.drawer_boxplay_mylist_watchlater: {
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
				}
				
				showFragment(myListFragment);
				break;
			}
			
			/* Settings */
			case R.id.drawer_boxplay_other_settings: {
				showFragment(new SettingsFragment());
				break;
			}
			
			/* About */
			case R.id.drawer_boxplay_other_about: {
				showFragment(new AboutFragment());
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
	 * Function used to fill the main {@link FrameLayout} of the application with a fragment instance
	 * 
	 * @param fragment
	 *            The new fragment
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
					.commit() //
			;
			
			helper.setLastFragment(fragment);
		} catch (Exception exception) {
			FRAGMENT_TO_OPEN = fragment;
		}
	}
	
	/**
	 * Tutorialable, used to create the tutorial path
	 */
	@SuppressWarnings("deprecation")
	@Override
	public TapTargetSequence getTapTargetSequence() {
		List<TapTarget> sequences = new ArrayList<TapTarget>();
		Display display = getWindowManager().getDefaultDisplay();
		
		Rect storeRectangle = new Rect(24, 24, 24, 24);
		storeRectangle.offsetTo(display.getWidth() / 2, display.getHeight() / 2);
		
		sequences.add(applyTutorialObjectTheme( //
				TapTarget.forToolbarNavigationIcon(toolbar, getString(R.string.boxplay_tutorial_main_drawer_title), getString(R.string.boxplay_tutorial_main_drawer_description)) //
						.id(TUTORIAL_PROGRESS_DRAWER) //
		)); //
		
		sequences.add(applyTutorialObjectTheme( //
				TapTarget.forToolbarMenuItem(toolbar, R.id.menu_main_action_search, getString(R.string.boxplay_tutorial_main_search_title), getString(R.string.boxplay_tutorial_main_search_description)) //
						.id(TUTORIAL_PROGRESS_SEARCH) //
		)); //
		
		sequences.add(applyTutorialObjectTheme( //
				TapTarget.forToolbarOverflow(toolbar, getString(R.string.boxplay_tutorial_main_options_title), getString(R.string.boxplay_tutorial_main_options_description)) //
						.id(TUTORIAL_PROGRESS_MENU))); //
		
		sequences.add(applyTutorialObjectTheme(TapTarget.forBounds(storeRectangle, getString(R.string.boxplay_tutorial_main_swipe_title), getString(R.string.boxplay_tutorial_main_swipe_description)) //
				.id(TUTORIAL_PROGRESS_SWIPE) //
				.icon(getResources().getDrawable(R.mipmap.image_hand_swipe)) //
		)); //
		
		Fragment lastFragment = helper.getLastFragment();
		if (lastFragment instanceof StoreFragment) {
			sequences.add(applyTutorialObjectTheme( //
					TapTarget.forBounds(storeRectangle, getString(R.string.boxplay_tutorial_main_video_title), getString(R.string.boxplay_tutorial_main_video_description)) //
							.id(TUTORIAL_PROGRESS_VIDEO) //
							.outerCircleAlpha(0.8F) //
			) //
					.dimColor(android.R.color.transparent) //
					.targetCircleColor(android.R.color.transparent) //
			); //
			
			sequences.add(applyTutorialObjectTheme( //
					TapTarget.forBounds(storeRectangle, getString(R.string.boxplay_tutorial_main_music_title), getString(R.string.boxplay_tutorial_main_music_description)) //
							.id(TUTORIAL_PROGRESS_MUSIC) //
							.outerCircleAlpha(0.8F) //
			) //
					.dimColor(android.R.color.transparent) //
					.targetCircleColor(android.R.color.transparent) //
			); //
		}
		
		return new TapTargetSequence(this) //
				.targets(sequences).listener(new TapTargetSequence.Listener() {
					@Override
					public void onSequenceFinish() {
						Fragment lastFragment = helper.getLastFragment();
						if (lastFragment instanceof StoreFragment) {
							((StoreFragment) lastFragment).withVideo();
						}
						
						managers.getTutorialManager().saveTutorialFinished(BoxPlayActivity.this);
					}
					
					@Override
					public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
						int id = lastTarget.id() + 1;
						
						switch (id) {
							case TUTORIAL_PROGRESS_VIDEO:
							case TUTORIAL_PROGRESS_MUSIC: {
								Fragment lastFragment = helper.getLastFragment();
								
								if (lastFragment instanceof StoreFragment) {
									switch (id) {
										case TUTORIAL_PROGRESS_VIDEO: {
											((StoreFragment) lastFragment).withVideo();
											break;
										}
										
										case TUTORIAL_PROGRESS_MUSIC: {
											((StoreFragment) lastFragment).withMusic();
											break;
										}
									}
								}
								
								break;
							}
						}
					}
					
					@Override
					public void onSequenceCanceled(TapTarget lastTarget) {
						; // Impossible
					}
				});
	}
	
	/**
	 * Quick function to apply tutorial common theme, reduce code
	 * 
	 * @param tapTarget
	 *            Actual sequence
	 * @return The actual sequence, but with common theme applied
	 */
	private TapTarget applyTutorialObjectTheme(TapTarget tapTarget) {
		return tapTarget.dimColor(android.R.color.black) // Background
				.outerCircleColor(R.color.colorAccent) // Big circle
				.targetCircleColor(R.color.colorPrimary) // Moving circle color (animation)
				.textColor(android.R.color.black) //
				.transparentTarget(true) //
				.cancelable(false); //
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
	
	/**
	 * Get the activity instance
	 */
	public static BoxPlayActivity getBoxPlayActivity() {
		return (BoxPlayActivity) INSTANCE;
	}
	
}