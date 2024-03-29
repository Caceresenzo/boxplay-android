package caceresenzo.apps.boxplay.activities;

import java.util.Iterator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.AsyncLayoutInflater;
import android.support.v7.app.ActionBar;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.culture.CultureFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.PageCultureSearchAndGoFragment;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager.SearchHistoryItem;

public class SearchAndGoHistoryActivity extends BaseBoxPlayActivty {
	
	/* Managers */
	private PageCultureSearchAndGoFragment searchAndGoFragment;
	private SearchAndGoManager searchAndGoManager;
	
	/* Views */
	private Toolbar toolbar;
	private ActionBar actionBar;
	private LinearLayout listLinearLayout;
	private FrameLayout informationContainerFrameLayout;
	
	/* Constructor */
	public SearchAndGoHistoryActivity() {
		super();

		this.searchAndGoFragment = PageCultureSearchAndGoFragment.getSearchAndGoFragment();
		this.searchAndGoManager = managers.getSearchAndGoManager();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchandgo_history);
		
		if (searchAndGoFragment == null) {
			finish();
			return;
		}
		
		initializeViews();
		
		createNextContentItemView(new AsyncLayoutInflater(this), searchAndGoManager.getSearchHistory().iterator());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (menu instanceof MenuBuilder) {
			((MenuBuilder) menu).setOptionalIconsVisible(true);
		}
		
		getMenuInflater().inflate(R.menu.searchandgo_history, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
			case android.R.id.home: {
				/* Avoid the "Unhandled" message */
				break;
			}
			
			case R.id.menu_searchandgo_history_clear_history: {
				if (!searchAndGoManager.getSearchHistory().isEmpty()) {
					boxPlayApplication.toast(R.string.boxplay_culture_searchngo_history_clear).show();
					searchAndGoManager.getSearchSuggestionSubManager().clear();
					
					finish();
					return true;
				}

				finish();
				return false;
			}
			
			default: {
				boxPlayApplication.toast("[" + SearchAndGoHistoryActivity.class.getSimpleName() + "]\nUnhandled onOptionsItemSelected(item.getTitle() = \"" + item.getTitle() + "\");").show();
				break;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Initialize views
	 */
	private void initializeViews() {
		toolbar = (Toolbar) findViewById(R.id.activity_searchandgo_history_toolbar_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(getString(R.string.boxplay_culture_searchngo_history_activity_title));
		
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_searchandgo_history_coordinatorlayout_container);
		
		listLinearLayout = (LinearLayout) findViewById(R.id.activity_searchandgo_history_linearlayout_list);
		
		informationContainerFrameLayout = (FrameLayout) findViewById(R.id.activity_searchandgo_history_framelayout_info_container);
		
		if (!searchAndGoManager.getSearchHistory().isEmpty()) {
			informationContainerFrameLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Create {@link View} recursively.
	 * 
	 * @param asyncLayoutInflater
	 *            Global {@link AsyncLayoutInflater} used to inflate all {@link View}.
	 * @param contentIterator
	 *            Content {@link Iterator} from history list.
	 */
	private void createNextContentItemView(final AsyncLayoutInflater asyncLayoutInflater, final Iterator<SearchAndGoManager.SearchHistoryItem> contentIterator) {
		if (contentIterator.hasNext() && !isDestroyed()) {
			asyncLayoutInflater.inflate(R.layout.item_searchandgo_history, listLinearLayout, new AsyncLayoutInflater.OnInflateFinishedListener() {
				@Override
				public void onInflateFinished(View view, int resid, ViewGroup parent) {
					if (!isDestroyed()) {
						new HistoryItemViewBinder(view).bind(contentIterator.next());
						
						parent.addView(view);
						
						createNextContentItemView(asyncLayoutInflater, contentIterator);
					}
				}
			});
		}
	}
	
	/**
	 * Start a {@link SearchAndGoHistoryActivity}.<br>
	 * WARNING: This activity can only start correctly if it has been lunched from a {@link CultureFragment}.
	 */
	public static void start() {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, SearchAndGoHistoryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		application.startActivity(intent);
	}
	
	/**
	 * View binder for {@link SearchHistoryItem}
	 * 
	 * @author Enzo CACERES
	 */
	class HistoryItemViewBinder {
		
		/* Views */
		private View view;
		private TextView dateTextView, queryTextView;
		
		/* Constructor */
		public HistoryItemViewBinder(View itemView) {
			view = itemView;
			
			dateTextView = (TextView) itemView.findViewById(R.id.item_searchandgo_history_textview_date);
			queryTextView = (TextView) itemView.findViewById(R.id.item_searchandgo_history_textview_query);
		}
		
		/**
		 * Bind actual view to target item.
		 * 
		 * @param item
		 *            Target item.
		 */
		public void bind(final SearchAndGoManager.SearchHistoryItem item) {
			dateTextView.setText(DateUtils.getRelativeTimeSpanString(boxPlayApplication, item.getDate().getTime()));
			queryTextView.setText(item.getQuery());
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					searchAndGoFragment.applyQuery(item.getQuery());
					finish();
				}
			});
		}
	}
	
}