package com.example.util

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsManager {
    private const val TAG = "AdsManager"
    
    // IDs REALES DE JOAN - LANZARUS 💰
    val appId = "ca-app-pub-4903263409458961~1005307516"
    val bannerAdId = "ca-app-pub-4903263409458961/3784503774"
    var interstitialAdId = "ca-app-pub-4903263409458961/6261503620"

    private var mInterstitialAd: InterstitialAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Interstitial failed: ${adError.message}")
                mInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Interstitial loaded")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showInterstitial(context: Context, onAdClosed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial(context)
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    onAdClosed()
                }
            }
            mInterstitialAd?.show(context as android.app.Activity)
        } else {
            onAdClosed()
        }
    }
}