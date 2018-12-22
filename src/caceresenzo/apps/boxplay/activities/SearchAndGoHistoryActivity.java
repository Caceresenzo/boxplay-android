package caceresenzo.apps.boxplay.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.culture.CultureFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.PageCultureSearchAndGoFragment;
import caceresenzo.apps.boxplay.managers.SearchAndGoManager;

public class SearchAndGoHistoryActivity extends BaseBoxPlayActivty {
	
	/* Managers */
	private PageCultureSearchAndGoFragment searchAndGoFragment;
	private SearchAndGoManager searchAndGoManager;
	
	/* Views */
	private Toolbar toolbar;
	private ActionBar actionBar;
	private RecyclerView recyclerView;
	
	/* Constructor */
	public SearchAndGoHistoryActivity() {
		super();
		
		this.searchAndGoManager = managers.getSearchAndGoManager();
		
		try {
			this.searchAndGoFragment = (PageCultureSearchAndGoFragment) CultureFragment.getCultureFragment().withSearchAndGo().getActualFragment();
		} catch (Exception exception) {
			/* NullPointerException */
		}
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
		
		coordinatorLayout = findViewById(R.id.activity_searchandgo_history_coordinatorlayout_container);
		
		recyclerView = (RecyclerView) findViewById(R.id.activity_searchandgo_history_recyclerview_list);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new HistoryItemViewAdapter(searchAndGoManager.getSearchHistory()));
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
	 * {@link RecyclerView}'s view adapter for {@link HistoryItemViewHolder}.
	 * 
	 * @author Enzo CACERES
	 */
	class HistoryItemViewAdapter extends RecyclerView.Adapter<HistoryItemViewHolder> {
		
		/* Variables */
		private List<SearchAndGoManager.SearchHistoryItem> list;
		
		/* Constructor */
		public HistoryItemViewAdapter(List<SearchAndGoManager.SearchHistoryItem> list) {
			this.list = list;
		}
		
		@Override
		public HistoryItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_searchandgo_history, viewGroup, false);
			return new HistoryItemViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(HistoryItemViewHolder viewHolder, int position) {
			SearchAndGoManager.SearchHistoryItem item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	/**
	 * {@link ViewHolder} for the {@link HistoryItemViewAdapter}.
	 * 
	 * @author Enzo CACERES
	 */
	class HistoryItemViewHolder extends RecyclerView.ViewHolder {
		
		/* Views */
		private View view;
		private TextView dateTextView, queryTextView;
		
		/* Constructor */
		public HistoryItemViewHolder(View itemView) {
			super(itemView);
			
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