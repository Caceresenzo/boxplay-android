package caceresenzo.apps.boxplay.activities;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseViewPagerAdapter;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage.PageDetailContentSearchAndGoFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.detailpage.PageDetailInfoSearchAndGoFragment;
import caceresenzo.libs.boxplay.culture.searchngo.data.AdditionalResultData;
import caceresenzo.libs.boxplay.culture.searchngo.providers.SearchAndGoProvider;
import caceresenzo.libs.boxplay.culture.searchngo.result.SearchAndGoResult;
import caceresenzo.libs.thread.HelpedThread;
import caceresenzo.libs.thread.ThreadUtils;

public class SearchAndGoDetailActivity extends BaseBoxPlayActivty {
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_SEARCH_RESULT_ITEM = "search_result_item";
	
	/* Instance */
	private static SearchAndGoDetailActivity INSTANCE;
	
	/* Result */
	private SearchAndGoResult searchAndGoResult;
	
	/* Views */
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
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					fillTabs();
				}
			}, 100);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(BUNDLE_KEY_SEARCH_RESULT_ITEM, (Serializable) searchAndGoResult);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		INSTANCE = null;
		
		infoFragment = null;
		contentFragment = null;
		
		fetchingWorker.cancel();
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
	}
	
	private void fillTabs() {
		adapter = new BaseViewPagerAdapter(getSupportFragmentManager());
		
		adapter.addFragment(infoFragment = new PageDetailInfoSearchAndGoFragment(), getString(R.string.boxplay_culture_searchngo_detail_tab_info));
		adapter.addFragment(contentFragment = new PageDetailContentSearchAndGoFragment(), getString(R.string.boxplay_culture_searchngo_detail_tab_content));
		
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
	
	public static void start(SearchAndGoResult result) {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, SearchAndGoDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(BUNDLE_KEY_SEARCH_RESULT_ITEM, (Serializable) result);
		
		application.startActivity(intent);
	}
	
	class FetchingWorker extends HelpedThread {
		private final SearchAndGoDetailActivity parentActivity;
		private SearchAndGoResult result;
		
		private List<AdditionalResultData> additionals;
		private List<AdditionalResultData> contents;
		
		public FetchingWorker() {
			this.parentActivity = (SearchAndGoDetailActivity) INSTANCE;
		}
		
		@Override
		protected void onRun() {
			SearchAndGoProvider provider = result.getParentProvider();
			
			additionals = provider.fetchMoreData(result);
			contents = provider.fetchContent(result);
		}
		
		@Override
		protected void onFinished() {
			if (parentActivity != INSTANCE) {
				return;
			}
			
			if (additionals != null) {
				while (infoFragment != null && !infoFragment.isUiReady()) {
					ThreadUtils.sleep(20L);
					countLoop();
				}
				resetLoop();
				
				if (infoFragment != null) {
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							infoFragment.applyResult(searchAndGoResult, additionals);
						}
					});
				}
			}
			
			if (contents != null) {
				while (contentFragment != null && !contentFragment.isUiReady()) {
					ThreadUtils.sleep(20L);
					countLoop();
				}
				resetLoop();
				
				if (contentFragment != null) {
					BoxPlayApplication.getHandler().post(new Runnable() {
						@Override
						public void run() {
							contentFragment.applyResult(searchAndGoResult, contents);
						}
					});
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			;
		}
		
		boolean alreadyValidated = false;
		int timesLooped = 0;
		
		private void countLoop() {
			if (alreadyValidated) {
				return;
			}
			
			timesLooped++;
			
			if (timesLooped > 150) {
				fillTabs();
			}
		}
		
		private void resetLoop() {
			alreadyValidated = true;
		}
		
		public FetchingWorker applyResult(SearchAndGoResult result) {
			this.result = result;
			return this;
		}
	}
	
	public static SearchAndGoDetailActivity getSearchAndGoDetailActivity() {
		return (SearchAndGoDetailActivity) INSTANCE;
	}
	
}