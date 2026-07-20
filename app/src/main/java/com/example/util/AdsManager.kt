package com.example.util

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

object AdsManager {
    private const val TAG = "AdsManager"

    // ══════════════════════════════════════════════════
    // IDs REALES — LANZARUS (ca-app-pub-4903263409458961~1005307516)
    // ══════════════════════════════════════════════════
    const val APP_ID                   = "ca-app-pub-4903263409458961~1005307516"
    const val BANNER_ID                = "ca-app-pub-4903263409458961/9076841407"
    const val INTERSTITIAL_REWARDED_ID = "ca-app-pub-4903263409458961/4992723799"
    const val APP_OPEN_ID              = "ca-app-pub-4903263409458961/9914867664"
    const val NATIVE_ADVANCED_ID       = "ca-app-pub-4903263409458961/1433742415"
    const val REWARDED_ID              = "ca-app-pub-4903263409458961/9120660740"

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob SDK inicializado")
            loadInterstitialRewarded(context)
            loadRewarded(context)
        }
    }

    // ── Intersticial Bonificado ────────────────────────────────────────
    fun loadInterstitialRewarded(context: Context) {
        RewardedInterstitialAd.load(
            context, INTERSTITIAL_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "Intersticial bonificado: ${err.message}")
                    mRewardedInterstitialAd = null
                }
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Intersticial bonificado cargado")
                    mRewardedInterstitialAd = ad
                }
            }
        )
    }

    fun showInterstitialRewarded(context: android.app.Activity, onRewarded: (Int) -> Unit, onClosed: () -> Unit) {
        val ad = mRewardedInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedInterstitialAd = null
                    loadInterstitialRewarded(context)
                    onClosed()
                }
                override fun onAdFailedToShowFullScreenContent(err: AdError) {
                    mRewardedInterstitialAd = null
                    onClosed()
                }
            }
            ad.show(context) { reward -> onRewarded(reward.amount) }
        } else {
            onClosed()
        }
    }

    // ── Bonificado ────────────────────────────────────────────────────
    fun loadRewarded(context: Context) {
        RewardedAd.load(
            context, REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "Bonificado: ${err.message}")
                    mRewardedAd = null
                }
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Bonificado cargado")
                    mRewardedAd = ad
                }
            }
        )
    }

    fun showRewarded(context: android.app.Activity, onRewarded: (Int) -> Unit, onClosed: () -> Unit) {
        val ad = mRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedAd = null
                    loadRewarded(context)
                    onClosed()
                }
                override fun onAdFailedToShowFullScreenContent(err: AdError) {
                    mRewardedAd = null
                    onClosed()
                }
            }
            ad.show(context) { reward -> onRewarded(reward.amount) }
        } else {
            onClosed()
        }
    }

    // ── Intersticial simple (fallback) ────────────────────────────────
    fun loadInterstitial(context: Context) {
        InterstitialAd.load(
            context, INTERSTITIAL_REWARDED_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(err: LoadAdError) {
                    Log.e(TAG, "Intersticial: ${err.message}")
                    mInterstitialAd = null
                }
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Intersticial cargado")
                    mInterstitialAd = ad
                }
            }
        )
    }

    fun showInterstitial(context: Context, onAdClosed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial(context)
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(err: AdError) {
                    mInterstitialAd = null
                    onAdClosed()
                }
            }
            ad.show(context as android.app.Activity)
        } else {
            onAdClosed()
        }
    }

    // ── Nativo avanzado ID (disponible para uso en composables) ───────
    fun getNativeAdId(): String = NATIVE_ADVANCED_ID

    // ── App Open ID (disponible para AppOpenAdManager) ─────────────
    fun getAppOpenId(): String = APP_OPEN_ID
}
