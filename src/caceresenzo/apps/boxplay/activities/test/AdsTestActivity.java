package caceresenzo.apps.boxplay.activities.test;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.ImageViewerActivity;
import caceresenzo.apps.boxplay.activities.base.BaseBoxPlayActivty;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;

public class AdsTestActivity extends BaseBoxPlayActivty {
	
	private AdView mAdView;
	private Button btnFullscreenAd, btnShowRewardedVideoAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_ads);
		
		btnFullscreenAd = (Button) findViewById(R.id.btn_fullscreen_ad);
		btnShowRewardedVideoAd = (Button) findViewById(R.id.btn_show_rewarded_video);
		// btnFullscreenAd.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(MainActivity.this, InterstitialAdActivity.class));
		// }
		// });
		//
		// btnShowRewardedVideoAd.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(MainActivity.this, RewardedVideoAdActivity.class));
		// }
		// });
		
		// TODO - remove this if condition
		// it's for demo purpose
		if (TextUtils.isEmpty(getString(R.string.admob_adunit_test_banner))) {
			Toast.makeText(getApplicationContext(), "Please mention your Banner Ad ID in strings.xml", Toast.LENGTH_LONG).show();
			return;
		}
		
		mAdView = (AdView) findViewById(R.id.adView);
		// mAdView.setAdSize(AdSize.BANNER);
		// mAdView.setAdUnitId(getString(R.string.banner_home_footer));
		
		AdRequest adRequest = new AdRequest.Builder() //
//				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				// Check the LogCat to get your test device ID
				.addTestDevice("2720D7C212713CD5F763C53F3AB7D325") //
				.build();
		
		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
			}
			
			@Override
			public void onAdClosed() {
				Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onAdFailedToLoad(int errorCode) {
				Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onAdLeftApplication() {
				Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onAdOpened() {
				super.onAdOpened();
			}
		});
		
		mAdView.loadAd(adRequest);
	}
	
	@Override
	public void onPause() {
		if (mAdView != null) {
			mAdView.pause();
		}
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mAdView != null) {
			mAdView.resume();
		}
	}
	
	@Override
	public void onDestroy() {
		if (mAdView != null) {
			mAdView.destroy();
		}
		super.onDestroy();
	}
	
	public static void start() {
		BoxPlayApplication application = BoxPlayApplication.getBoxPlayApplication();
		
		Intent intent = new Intent(application, AdsTestActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		application.startActivity(intent);
	}
	
}