package caceresenzo.apps.boxplay.fragments.premium.adult;

import java.util.List;

import android.os.Bundle;
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
import caceresenzo.android.libs.toast.ToastUtils;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.dialog.WorkingProgressDialog;
import caceresenzo.apps.boxplay.managers.PremiumManager.AdultPremiumSubManager;
import caceresenzo.apps.boxplay.managers.PremiumManager.AdultSubModuleCallback;
import caceresenzo.libs.boxplay.models.premium.adult.AdultVideo;

public class AdultExplorerFragment extends Fragment {
	
	private AdultPremiumSubManager adultSubManager = BoxPlayApplication.getManagers().getPremiumManager().getAdultSubManager();
	
	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView recyclerView;
	
	private WorkingProgressDialog workingProgressDialog;
	
	private boolean startingAsRefreshing = true;
	
	public AdultExplorerFragment() {
		adultSubManager.attachCallback(new AdultSubModuleCallback() {
			@Override
			public void onLoadFinish() {
				recyclerView.getAdapter().notifyDataSetChanged();
				swipeRefreshLayout.setRefreshing(false);
				workingProgressDialog.hide();
			}
			
			@Override
			public void onUrlReady(String url) {
				if (BoxPlayApplication.getViewHelper().isVlcInstalled()) {
					BoxPlayApplication.getManagers().getVideoManager().openVLC(url, null);
				} else {
					
				}
				
				swipeRefreshLayout.setRefreshing(false);
				workingProgressDialog.hide();
			}
			
			@Override
			public void onLoadFailed(Exception exception) {
				if (getContext() != null) {
					ToastUtils.makeLong(getContext(), exception.toString());
					// DialogUtils.showDialog(getContext(), "Error.", StringUtils.fromException(exception));
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
				BoxPlayApplication.getHandler().post(new Runnable() {
					@Override
					public void run() {
						workingProgressDialog.update(ressourceId);
					}
				});
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
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
			
			BoxPlayApplication.getViewHelper().downloadToImageView(thumbnailImageView, adultVideo.getImageUrl());
			// thumbnailImageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_boxplay_easter_egg));
			
			titleTextView.setText(adultVideo.getTitle());
			// titleTextView.setText("Cool stuff");
			
			viewCountTextView.setText(String.valueOf(adultVideo.getViewCount()));
			viewCountTextView.setVisibility(adultVideo.hasViewCount() ? View.VISIBLE : View.GONE);
		}
	}
	
}