package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import caceresenzo.android.libs.intent.CommonIntentUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;
import caceresenzo.apps.boxplay.fragments.BaseViewPagerAdapter;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage.PageDetailContentSearchAndGoFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage.PageDetailInfoSearchAndGoFragment;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.thread.ThreadUtils;
import caceresenzo.libs.thread.implementations.WorkerThread;

public class SearchAndGoDetailActivity extends BaseBoxPlayActivty {
	
	/* Tag */
	public static final String TAG = SearchAndGoDetailActivity.class.getSimpleName();
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_SEARCH_RESULT_ITEM = "search_result_item";
	public static final String BUNDLE_KEY_ACTUAL_PAGE_POSITION = "actual_page_position";
	
	/* Instance */
	private static SearchAndGoDetailActivity INSTANCE;
	
	/* Result */
	private SearchAndGoResult searchAndGoResult;
	
	/* Views */
	private Menu menu;
	
	private Toolbar toolbar;
	private ActionBar actionBar;
	
	private TabLayout tabLayout;
	private ViewPager viewPager;
	
	/* Adaper */
	private BaseViewPagerAdapter adapter;
	
	/* Fragments */
	private PageDetailInfoSearchAndGoFragment infoFragment;
	private PageDetailContentSearchAndGoFragment contentFragment;
	
	/* Worker */
	private FetchingWorker fetchingWorker;
	
	/* Variables */
	private int actualPagePosition;
	
	/* Constructor */
	public SearchAndGoDetailActivity() {
		super();
		
		fetchingWorker = new FetchingWorker();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchandgo_detail);
		INSTANCE = this;
		
		searchAndGoResult = (SearchAndGoResult) getIntent().getSerializableExtra(BUNDLE_KEY_SEARCH_RESULT_ITEM);
		
		if (searchAndGoResult == null) {
			if (boxPlayApplication != null) {
				boxPlayApplication.toast(getString(R.string.boxplay_error_activity_invalid_data)).show();
			}
			finish();
		}
		
		initializeViews();
		
		displayResult();
		
		if (savedInstanceState != null) {
			final int savedPagePosition = savedInstanceState.getInt(BUNDLE_KEY_ACTUAL_PAGE_POSITION, NO_VALUE);
			
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					fillTabs();
					
					if (savedPagePosition != NO_VALUE) {
						viewPager.setCurrentItem(savedPagePosition);
					}
				}
			}, 100);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		
		if (menu instanceof MenuBuilder) {
			((MenuBuilder) menu).setOptionalIconsVisible(true);
		}
		
		getMenuInflater().inflate(R.menu.searchandgo_details, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case android.R.id.home: {
				onBackPressed();
				break;
			}
			
			case R.id.menu_searchandgo_details_open_in_browser: {
				if (searchAndGoResult != null) {
					CommonIntentUtils.openUrl(this, searchAndGoResult.getUrl());
				}
				return true;
			}
			
			case R.id.menu_searchandgo_details_reorder: {
				if (contentFragment != null) {
					contentFragment.reorder();
				}
				return true;
			}
			
			default: {
				boxPlayApplication.toast("[" + SearchAndGoDetailActivity.class.getSimpleName() + "]\nUnhandled onOptionsItemSelected(item.getTitle() = \"" + item.getTitle() + "\");").show();
				break;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_SEARCH_RESULT_ITEM, (Serializable) searchAndGoResult);
		outState.putSerializable(BUNDLE_KEY_ACTUAL_PAGE_POSITION, viewPager.getCurrentItem());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
		
		infoFragment = null;
		contentFragment = null;
		
		fetchingWorker.terminate();
	}
	
	private void initializeViews() {
		toolbar = (Toolbar) findViewById(R.id.activity_searchandgo_detail_toolbar_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		tabLayout = (TabLayout) findViewById(R.id.activity_searchandgo_detail_tablayout_container);
		viewPager = (ViewPager) findViewById(R.id.activity_searchandgo_detail_viewpager_container);
		
		fillTabs();
		
		viewPager.setOffscreenPageLimit(2);
		
		tabLayout.setupWithViewPager(viewPager);
		
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				firePageUpdateEvent(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				;
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				;
			}
		});
	}
	
	public void firePageUpdateEvent() {
		firePageUpdateEvent(actualPagePosition);
	}
	
	public void firePageUpdateEvent(int newPosition) {
		BaseBoxPlayFragment openFragment;
		
		switch (actualPagePosition = newPosition) {
			case 0: {
				openFragment = infoFragment;
				break;
			}
			
			case 1: {
				openFragment = contentFragment;
				break;
			}
			
			default: {
				openFragment = null;
				break;
			}
		}
		
		for (BaseBoxPlayFragment fragment : new BaseBoxPlayFragment[] { infoFragment, contentFragment }) {
			if (fragment instanceof PageListener) {
				((PageListener) fragment).onPageOpen(SearchAndGoDetailActivity.this, openFragment);
			}
		}
	}
	
	private void fillTabs() {
		adapter = new BaseViewPagerAdapter(getSupportFragmentManager());
		
		adapter.addFragment(infoFragment = new PageDetailInfoSearchAndGoFragment(), R.string.boxplay_culture_searchngo_detail_tab_info);
		adapter.addFragment(contentFragment = new PageDetailContentSearchAndGoFragment(), R.string.boxplay_culture_searchngo_detail_tab_content);
		
		viewPager.setAdapter(adapter);
	}
	
	private void displayResult() {
		if (searchAndGoResult == null) {
			finish();
			return;
		}
		
		fetchingWorker = new FetchingWorker();
		
		actionBar.setTitle(searchAndGoResult.getName());
		fetchingWorker.applyResult(searchAndGoResult).start();
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	public static void start(SearchAndGoResult result) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		application.startActivity(createStartIntent(application, result));
	}
	
	public static Intent createStartIntent(Context context, SearchAndGoResult result) {
		Intent intent = new Intent(context, SearchAndGoDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_SEARCH_RESULT_ITEM, (Serializable) result);
		
		return intent;
	}
	
	class FetchingWorker extends WorkerThread {
		
		/* Variables */
		private final SearchAndGoDetailActivity parentActivity;
		private SearchAndGoResult result;
		
		private final List<AdditionalResultData> additionals;
		private final List<AdditionalResultData> contents;
		
		/* Constructor */
		public FetchingWorker() {
			super();
			
			this.parentActivity = (SearchAndGoDetailActivity) INSTANCE;
			
			this.additionals = new ArrayList<>();
			this.contents = new ArrayList<>();
		}
		
		@Override
		protected void execute() {
			SearchAndGoProvider provider = result.getParentProvider();
			
			try {
				additionals.addAll(provider.fetchMoreData(result));
			} catch (Exception exception) {
				Log.w(TAG, "Fetching more data created an exception.", exception);
			}
			
			try {
				contents.addAll(provider.fetchContent(result));
			} catch (Exception exception) {
				Log.w(TAG, "Fetching more content created an exception.", exception);
			}
		}
		
		@Override
		protected void done() {
			if (parentActivity != INSTANCE) {
				return;
			}
			
			while (infoFragment != null && !infoFragment.isReady()) {
				ThreadUtils.sleep(20L);
			}
			
			if (infoFragment != null) {
				BoxPlayApplication.getHandler().post(new Runnable() {
					@Override
					public void run() {
						infoFragment.applyResult(searchAndGoResult, additionals);
					}
				});
			}
			
			while (contentFragment != null && !contentFragment.isReady()) {
				ThreadUtils.sleep(20L);
			}
			
			if (contentFragment != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						contentFragment.applyResult(searchAndGoResult, contents);
					}
				});
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					firePageUpdateEvent();
				}
			});
		}
		
		public FetchingWorker applyResult(SearchAndGoResult result) {
			this.result = result;
			return this;
		}
		
	}
	
	public static SearchAndGoDetailActivity getSearchAndGoDetailActivity() {
		return (SearchAndGoDetailActivity) INSTANCE;
	}
	
	public static interface PageListener {
		
		void onPageOpen(SearchAndGoDetailActivity searchAndGoDetailActivity, BaseBoxPlayFragment baseBoxPlayFragment);
		
	}
	
}