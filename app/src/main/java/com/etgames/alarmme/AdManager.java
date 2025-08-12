package com.etgames.alarmme;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class AdManager {

    private static final String TAG = "AdManager";

    private static String gameId = "5921685"; // From Unity Dashboard
    private static String interstitialPlacement = "Interstitial_Android"; // Set in dashboard
    private static String rewardedPlacement = "Rewarded_Android"; // Set in dashboard
    private static boolean testMode = false; // Set to false before release

    private static boolean isInterstitialLoaded = false;
    private static boolean isRewardedLoaded = false;

    // Call this once in onCreate of your first Activity
    public static void init(Activity activity) {
        if (!MainActivity.prefs.getBoolean("adsOn", false)) {

            Log.d(TAG, "ads disabled");
            return;
        }
        UnityAds.initialize(activity, gameId, testMode, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                Log.d(TAG, "Unity Ads initialized");
                loadInterstitial();
                loadRewarded();

            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                Log.e(TAG, "Unity Ads init failed: " + message);
            }
        });
    }

    public static void loadInterstitial() {
        UnityAds.load(interstitialPlacement, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                isInterstitialLoaded = true;
                Log.d(TAG, "Interstitial loaded");
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                isInterstitialLoaded = false;
                Log.e(TAG, "Failed to load interstitial: " + message);
            }
        });
    }

    public static void showInterstitial(Activity activity) {

        if (!MainActivity.prefs.getBoolean("adsOn", false)) {

            Log.d(TAG, "ads disabled");
            return;
        }
        if (!isInterstitialLoaded) {
            Log.d(TAG, "Interstitial not ready");
            return;
        }

        UnityAds.show(activity, interstitialPlacement, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.e(TAG, "Failed to show interstitial: " + message);
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d(TAG, "Interstitial started");
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                Log.d(TAG, "Interstitial clicked");
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                Log.d(TAG, "Interstitial closed");
                isInterstitialLoaded = false;
                loadInterstitial(); // Prepare next one
            }
        });
    }

    public static void loadRewarded() {
        UnityAds.load(rewardedPlacement, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                isRewardedLoaded = true;
                Log.d(TAG, "Rewarded loaded");
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                isRewardedLoaded = false;
                Log.e(TAG, "Failed to load rewarded: " + message);
            }
        });
    }

    public static void showRewarded(Activity activity) {
        if (!MainActivity.prefs.getBoolean("adsOn", false)) {

            Log.d(TAG, "ads disabled");
            return;
        }
        if (!isRewardedLoaded) {
            Log.d(TAG, "Rewarded ad not ready");
            return;
        }

        UnityAds.show(activity, rewardedPlacement, new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.e(TAG, "Failed to show rewarded: " + message);
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d(TAG, "Rewarded started");
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                Log.d(TAG, "Rewarded clicked");
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    Log.d(TAG, "Reward the user here!");
                }
                isRewardedLoaded = false;
                loadRewarded(); // Prepare next one
            }
        });
    }


    private static BannerView bannerView;

    static String bannerPlacement = "Banner_Android";

    public static void showBanner(Activity activity, FrameLayout bannerContainer) {
        if (!MainActivity.prefs.getBoolean("adsOn", false)) {

            Log.d(TAG, "ads disabled");
            return;
        }
        bannerView = new BannerView(activity, bannerPlacement, new UnityBannerSize(320, 50));

        bannerView.setListener(new BannerView.IListener() {
            @Override
            public void onBannerLoaded(BannerView bannerAdView) {
                Log.d("UnityAds", "Banner loaded");
                bannerContainer.removeAllViews();
                bannerContainer.addView(bannerAdView);
            }

            @Override
            public void onBannerClick(BannerView bannerAdView) {
                Log.d("UnityAds", "Banner clicked");
            }

            @Override
            public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
                Log.e("UnityAds", "Banner failed to load: " + errorInfo.errorMessage);
            }

            @Override
            public void onBannerLeftApplication(BannerView bannerAdView) {
                Log.d("UnityAds", "User left app from banner");
            }
        });

        bannerView.load();
    }

    public static void hideBanner(FrameLayout bannerContainer) {
        if (bannerView != null) {
            bannerContainer.removeAllViews();
            bannerView.destroy();
            bannerView = null;
            Log.d("UnityAds", "Banner destroyed");
        }
    }
}
