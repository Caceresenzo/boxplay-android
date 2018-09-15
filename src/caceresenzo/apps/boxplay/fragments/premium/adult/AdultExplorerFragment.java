package caceresenzo.apps.boxplay.fragments.premium.adult;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import caceresenzo.android.libs.list.EndlessRecyclerViewScrollListener;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.PremiumManager.AdultPremiumSubManager;
import caceresenzo.apps.boxplay.managers.PremiumManager.AdultSubModuleCallback;
import caceresenzo.apps.boxplay.managers.XManagers;
import caceresenzo.libs.boxplay.models.premium.adult.AdultVideo;

public class AdultExplorerFragment extends Fragment {
	/* Managers */
	private BoxPlayApplication boxPlayApplication;
	private Handler handler;
	private ViewHelper viewHelper;
	private XManagers managers;
	
	/* Sub-Managers */
	private AdultPremiumSubManager adultSubManager = BoxPlayApplication.getManagers().getPremiumManager().getAdultSubManager();
	
	/* Views */
	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView recyclerView;
	
	/* Dialog */
	private WorkingProgressDialog workingProgressDialog;
	
	/* Variables */
	private boolean startingAsRefreshing = true;
	
	/* Constructor */
	public AdultExplorerFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.viewHelper = BoxPlayApplication.getViewHelper();
		this.managers = BoxPlayApplication.getManagers();
		
		adultSubManager.attachCallback(new AdultSubModuleCallback() {
			@Override
			public void onLoadFinish() {
				recyclerView.getAdapter().notifyDataSetChanged();
				swipeRefreshLayout.setRefreshing(false);
				workingProgressDialog.hide();
			}
			
			@Override
			public void onUrlReady(final String url) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (viewHelper.isVlcInstalled()) {
							managers.getVideoManager().openVLC(url, null);
						} else {
							boxPlayApplication.toast(R.string.boxplay_premium_adult_status_error_vlc_not_installed).show();
						}
						
						swipeRefreshLayout.setRefreshing(false);
						workingProgressDialog.hide();
					}
				});
			}
			
			@Override
			public void onLoadFailed(Exception exception) {
				if (boxPlayApplication != null) {
					boxPlayApplication.toast(R.string.boxplay_premium_adult_status_error_failed_to_load, exception.toString()).show();
				}
				
				if (swipeRefreshLayout != null) {
					swipeRefreshLayout.setRefreshing(false);
				} else {
					startingAsRefreshing = true;
				}
				
				workingProgressDialog.hide();
			}
			
			@Override
			public void onStatusUpdate(final int ressourceId) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						workingProgressDialog.update(ressourceId);
					}
				});
			}
			
			@Override
			public void onError(final int ressourceId, final Object... arguments) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						workingProgressDialog.hide();
						
						boxPlayApplication.toast(ressourceId, arguments).show();
					}
				});
			}
			
			@Override
			public WorkingProgressDialog returnDialog() {
				return workingProgressDialog;
			}
		});
		
		adultSubManager.fetchNextPage();
		
		workingProgressDialog = WorkingProgressDialog.create(BoxPlayApplication.getBoxPlayApplication().getAttachedActivity());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_adult_explorer, container, false);
		
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_adult_explorer_swiperefreshlayout_container);
		swipeRefreshLayout.setRefreshing(startingAsRefreshing);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				swipeRefreshLayout.setRefreshing(true);
				
				if (adultSubManager.isWorking()) {
					swipeRefreshLayout.setRefreshing(false);
					return;
				}
				
				adultSubManager.resetFetchData();
			}
		});
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_adult_explorer_recyclerview_list);
		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
		recyclerView.setAdapter(new AdultViewAdapter(adultSubManager.getAllVideos()));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		recyclerView.setOnScrollListener(new EndlessRecyclerViewScrollListener((GridLayoutManager) recyclerView.getLayoutManager()) {
			@Override
			public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
				adultSubManager.fetchPage(page + 1);
			}
		});
		
		return view;
	}
	
	class AdultViewAdapter extends RecyclerView.Adapter<AdultViewHolder> {
		private List<AdultVideo> list;
		
		public AdultViewAdapter(List<AdultVideo> list) {
			this.list = list;
		}
		
		@Override
		public AdultViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_adult_video, viewGroup, false);
			return new AdultViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(AdultViewHolder viewHolder, int position) {
			AdultVideo item = list.get(position);
			viewHolder.bind(item);
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class AdultViewHolder extends RecyclerView.ViewHolder {
		private View view;
		private ImageView thumbnailImageView;
		private TextView titleTextView, viewCountTextView;
		
		public AdultViewHolder(View itemView) {
			super(itemView);
			
			view = itemView;
			thumbnailImageView = (ImageView) itemView.findViewById(R.id.item_adult_video_imageview_thumbnail);
			titleTextView = (TextView) itemView.findViewById(R.id.item_adult_video_textview_title);
			viewCountTextView = (TextView) itemView.findViewById(R.id.item_adult_video_textview_view_count);
		}
		
		public void bind(final AdultVideo adultVideo) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					workingProgressDialog.show();
					adultSubManager.fetchVideoPage(adultVideo.getTargetUrl());
				}
			});
			
			viewHelper.downloadToImageView(thumbnailImageView, adultVideo.getImageUrl());
			
			titleTextView.setText(adultVideo.getTitle());
			
			if (adultVideo.hasViewCount()) {
				viewCountTextView.setText(String.valueOf(adultVideo.getViewCount()));
			} else {
				viewCountTextView.setVisibility(View.GONE);
			}
		}
	}
	
}