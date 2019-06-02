package caceresenzo.apps.boxplay.fragments.other;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.fragments.BaseBoxPlayFragment;

public class AdsFragment extends BaseBoxPlayFragment {
	
	/* Views */
	private RecyclerView recyclerView;
	
	/* Ads */
	private AdView adView;
	private InterstitialAd interstitialAd;
	private RewardedVideoAd rewardedVideoAd;
	
	/* Variables */
	private final List<SimpleAdsItem> adsItems;
	
	/* Constructor */
	public AdsFragment() {
		super();
		
		this.adsItems = new ArrayList<>();
		
		this.adsItems.add(new SimpleAdsItem(R.string.boxplay_other_ads_type_banner) {
			@Override
			public void load(final ProgressBar progressBar, final TextView etaTextView) {
				adView = new AdView(boxPlayApplication);
				adView.setAdSize(AdSize.BANNER);
				
				if (BoxPlayApplication.BUILD_DEBUG) {
					adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
				} else {
					adView.setAdUnitId(getString(R.string.admob_adunit_test_banner));
				}
				
				final LinearLayout parentLayout = (LinearLayout) progressBar.getParent();
				parentLayout.addView(adView);
				
				adView.setAdListener(new AdListener() {
					@Override
					public void onAdLoaded() {
						Toast.makeText(boxPlayApplication, "Ad (banner) is loaded!", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						
						etaTextView.setText(R.string.boxplay_other_ads_eta_ready);
					}
					
					@Override
					public void onAdClosed() {
						Toast.makeText(boxPlayApplication, "Ad (banner) is closed!", Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(View.INVISIBLE);
					}
					
					@Override
					public void onAdFailedToLoad(int errorCode) {
						Toast.makeText(boxPlayApplication, "Ad (banner) failed to load! error code: " + errorCode + "(" + getString(errorToReason(errorCode)) + ")", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						progressBar.setProgressTintList(ColorStateList.valueOf(boxPlayApplication.getColor(R.color.colorError)));
						
						etaTextView.setText(errorToReason(errorCode));
					}
					
					@Override
					public void onAdLeftApplication() {
						Toast.makeText(boxPlayApplication, "Ad (banner) left application!", Toast.LENGTH_SHORT).show();
						
						progressBar.setVisibility(View.INVISIBLE);
					}
					
					@Override
					public void onAdOpened() {
						Toast.makeText(boxPlayApplication, "Ad (banner) is opened!", Toast.LENGTH_SHORT).show();
					}
				});
				
				loadAds(etaTextView);
			}
			
			private void loadAds(TextView etaTextView) {
				etaTextView.setText(R.string.boxplay_other_ads_eta_loading);
				
				adView.loadAd(new AdRequest.Builder().build());
			}
			
			@Override
			public View.OnClickListener getOnClickListener() {
				return new OnClickListener() {
					@Override
					public void onClick(View view) {
						
					}
				};
			}
		});
		
		this.adsItems.add(new SimpleAdsItem(R.string.boxplay_other_ads_type_interstitial) {
			@Override
			public void load(final ProgressBar progressBar, final TextView etaTextView) {
				interstitialAd = new InterstitialAd(boxPlayApplication);
				
				if (BoxPlayApplication.BUILD_DEBUG) {
					interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
				} else {
					interstitialAd.setAdUnitId(getString(R.string.admob_adunit_test_interstitial));
				}
				
				interstitialAd.setAdListener(new AdListener() {
					@Override
					public void onAdLoaded() {
						Toast.makeText(boxPlayApplication, "Ad is loaded!", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						
						etaTextView.setText(R.string.boxplay_other_ads_eta_ready);
					}
					
					@Override
					public void onAdClosed() {
						Toast.makeText(boxPlayApplication, "Ad is closed!", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(true);
						
						loadAds(etaTextView);
					}
					
					@Override
					public void onAdFailedToLoad(int errorCode) {
						Toast.makeText(boxPlayApplication, "Ad failed to load! error code: " + errorCode + "(" + getString(errorToReason(errorCode)) + ")", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						progressBar.setProgressTintList(ColorStateList.valueOf(boxPlayApplication.getColor(R.color.colorError)));
						
						etaTextView.setText(errorToReason(errorCode));
					}
					
					@Override
					public void onAdLeftApplication() {
						Toast.makeText(boxPlayApplication, "Ad left application!", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onAdOpened() {
						Toast.makeText(boxPlayApplication, "Ad is opened!", Toast.LENGTH_SHORT).show();
					}
				});
				
				loadAds(etaTextView);
			}
			
			private void loadAds(TextView etaTextView) {
				etaTextView.setText(R.string.boxplay_other_ads_eta_loading);
				
				interstitialAd.loadAd(new AdRequest.Builder().build());
			}
			
			@Override
			public View.OnClickListener getOnClickListener() {
				return new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (interstitialAd.isLoaded()) {
							interstitialAd.show();
						}
					}
				};
			}
		});
		
		this.adsItems.add(new SimpleAdsItem(R.string.boxplay_other_ads_type_rewarded_video) {
			@Override
			public void load(final ProgressBar progressBar, final TextView etaTextView) {
				rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(boxPlayApplication);
				
				rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
					@Override
					public void onRewarded(RewardItem rewardItem) {
						Toast.makeText(boxPlayApplication, "onRewarded! currency: " + rewardItem.getType() + "  amount: " + rewardItem.getAmount(), Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onRewardedVideoAdLeftApplication() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(true);
					}
					
					@Override
					public void onRewardedVideoAdClosed() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(true);
						
						loadAds(etaTextView);
					}
					
					@Override
					public void onRewardedVideoAdFailedToLoad(int errorCode) {
						Toast.makeText(boxPlayApplication, "onRewardedVideoAdFailedToLoad error code: " + errorCode + "(" + getString(errorToReason(errorCode)) + ")", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						progressBar.setProgressTintList(ColorStateList.valueOf(boxPlayApplication.getColor(R.color.colorError)));
						
						etaTextView.setText(errorToReason(errorCode));
					}
					
					@Override
					public void onRewardedVideoAdLoaded() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
						
						progressBar.setIndeterminate(false);
						
						etaTextView.setText(R.string.boxplay_other_ads_eta_ready);
					}
					
					@Override
					public void onRewardedVideoAdOpened() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onRewardedVideoStarted() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onRewardedVideoCompleted() {
						Toast.makeText(boxPlayApplication, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
					}
				});
				
				// mRewardedVideoAd.loadAd("ca-app-pub-8224350508001877/5745343281", new AdRequest.Builder().build());
				
				loadAds(etaTextView);
			}
			
			private void loadAds(TextView etaTextView) {
				String adUnit = "ca-app-pub-3940256099942544/5224354917";
				
				if (!BoxPlayApplication.BUILD_DEBUG) {
					adUnit = getString(R.string.admob_adunit_test_interstitial);
				}
				
				etaTextView.setText(R.string.boxplay_other_ads_eta_loading);
				
				rewardedVideoAd.loadAd(adUnit, new AdRequest.Builder().build());
			}
			
			@Override
			public View.OnClickListener getOnClickListener() {
				return new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (rewardedVideoAd.isLoaded()) {
							rewardedVideoAd.show();
						}
					}
				};
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ads, container, false);
		
		recyclerView = (RecyclerView) view.findViewById(R.id.fragment_ads_recyclerview_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(new SimpleAdsViewAdapter(this.adsItems));
		recyclerView.setHasFixedSize(true);
		recyclerView.setNestedScrollingEnabled(false);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		recyclerView.requestFocus();
	}
	
	class SimpleAdsViewAdapter extends RecyclerView.Adapter<SimpleAdsViewHolder> {
		private List<SimpleAdsItem> list;
		
		public SimpleAdsViewAdapter(List<SimpleAdsItem> list) {
			this.list = list;
		}
		
		@Override
		public SimpleAdsViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ads_item, viewGroup, false);
			return new SimpleAdsViewHolder(view);
		}
		
		@Override
		public void onBindViewHolder(SimpleAdsViewHolder viewHolder, int position) {
			viewHolder.bind(list.get(position));
		}
		
		@Override
		public int getItemCount() {
			return list.size();
		}
	}
	
	class SimpleAdsViewHolder extends RecyclerView.ViewHolder {
		
		/* Views */
		private View view;
		private TextView contentTextView, etaTextView;
		private ProgressBar progressBar;
		
		/* Constructor */
		public SimpleAdsViewHolder(View itemView) {
			super(itemView);
			
			this.view = itemView;
			this.contentTextView = (TextView) itemView.findViewById(R.id.item_ads_item_textview_content);
			this.etaTextView = (TextView) itemView.findViewById(R.id.item_ads_item_textview_eta);
			this.progressBar = (ProgressBar) itemView.findViewById(R.id.item_ads_item_progressview_eta);
		}
		
		/* Binding */
		public void bind(SimpleAdsItem item) {
			view.setOnClickListener(item.getOnClickListener());
			contentTextView.setText(getString(item.getTextStringId()));
			
			item.load(progressBar, etaTextView);
		}
	}
	
	abstract class SimpleAdsItem {
		
		/* Variables */
		private int textStringId;
		
		/* Constructor */
		public SimpleAdsItem(int textStringId) {
			this.textStringId = textStringId;
		}
		
		/** @return Item's text to display. */
		public int getTextStringId() {
			return textStringId;
		}
		
		/**
		 * Called when the corresponding view has been created.
		 */
		public abstract void load(ProgressBar progressBar, TextView etaTextView);
		
		/**
		 * Called when the user click on the corresponding view.
		 */
		public abstract View.OnClickListener getOnClickListener();
		
	}
	
	@Override
	public void onResume() {
		if (rewardedVideoAd != null) {
			rewardedVideoAd.resume(boxPlayApplication);
		}
		
		if (adView != null) {
			adView.pause();
		}
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if (rewardedVideoAd != null) {
			rewardedVideoAd.pause(boxPlayApplication);
		}
		
		if (adView != null) {
			adView.resume();
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (rewardedVideoAd != null) {
			rewardedVideoAd.destroy(boxPlayApplication);
		}
		
		if (adView != null) {
			adView.destroy();
		}
		
		super.onDestroy();
	}
	
	public static int errorToReason(int errorCode) {
		switch (errorCode) {
			case AdRequest.ERROR_CODE_INTERNAL_ERROR: {
				return R.string.boxplay_other_ads_error_internal_error;
			}
			
			case AdRequest.ERROR_CODE_INVALID_REQUEST: {
				return R.string.boxplay_other_ads_error_invalid_request;
			}
			
			case AdRequest.ERROR_CODE_NETWORK_ERROR: {
				return R.string.boxplay_other_ads_error_network_error;
			}
			
			case AdRequest.ERROR_CODE_NO_FILL: {
				return R.string.boxplay_other_ads_error_no_fill;
			}
			
			default: {
				throw new IllegalStateException("Unknown error with code: " + errorCode);
			}
		}
	}
	
}