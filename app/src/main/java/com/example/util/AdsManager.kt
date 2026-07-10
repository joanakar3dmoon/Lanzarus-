package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsManager {

    private const val TAG = "AdsManager"

    // IDs DE PRUEBA (cambiar por IDs reales cuando subas a Play Store)
    // Los de prueba funcionan sin registro, solo para que puedas probar
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    // 👇 CUANDO TENGAS TUS IDs REALES DE AdMob, CAMBIA ESTO:
    // var interstitialAdId = "ca-app-pub-TU_ID_AQUI/XXXXXXXXXX"
    var interstitialAdId = TEST_INTERSTITIAL_ID
    var rewardedAdId = TEST_REWARDED_ID
    var bannerAdId = TEST_BANNER_ID

    // --- INTERSTITIAL (pantalla completa) ---
    private var interstitialAd: InterstitialAd? = null

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial cargado ✅")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Error interstitial: ${error.message}")
                }
            })
    }

    fun showInterstitial(activity: Activity) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity) // precargar el siguiente
                }
                override fun onAdFailedToShow(error: AdError) {
                    interstitialAd = null
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial mostrado")
                }
            }
            ad.show(activity)
        }
    }

    // --- REWARDED (anuncio con recompensa) ---
    private var rewardedAd: RewardedAd? = null

    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardedAdId, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded cargado ✅")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "Error rewarded: ${error.message}")
                }
            })
    }

    fun showRewarded(activity: Activity, onRewarded: (Int) -> Unit = {}) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                }
                override fun onAdFailedToShow(error: AdError) {
                    rewardedAd = null
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded mostrado")
                }
            }
            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount.toInt()
                Log.d(TAG, "Recompensa: $rewardAmount ${rewardItem.type}")
                onRewarded(rewardAmount)
            }
        }
    }

    // --- INICIALIZACIÓN ---
    fun init(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob inicializado ✅")
            // Precargar anuncios al iniciar
            loadInterstitial(context)
            loadRewarded(context)
        }
    }
}
