package com.shubham.happyplaces.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.shubham.happyplaces.Constants
import com.shubham.happyplaces.Constants.Companion.HAPPY_PLACE_MODEL_DATA
import com.shubham.happyplaces.databinding.ActivityHappyPlaceDetailsBinding
import com.shubham.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailsBinding
    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "AddHappyPlaces"

    @SuppressWarnings("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Interstitial Ad
        // https://developers.google.com/admob/android/interstitial
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, Constants.INTERSTITIAL_AD_UNIT, adRequest, object: InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d(TAG, "onAdLoaded: INTERSTITIAL AD FAILED TO LOADED")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                Log.d(TAG, "onAdLoaded: INTERSTITIAL AD LOADED")
                mInterstitialAd = interstitialAd
                setInterstitialAdListener()
            }
        })

        val happyPlaceModelData: HappyPlaceModel?

        if(intent.hasExtra(HAPPY_PLACE_MODEL_DATA)) {
            // By making HappyPlaceModel as Serializable:
            // Handling getSerializableExtra() method properly as per version codes:
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                happyPlaceModelData =
//                    intent.getSerializableExtra(HAPPY_PLACE_MODEL_DATA, HappyPlaceModel::class.java)
//            } else {
//                happyPlaceModelData =
//                    intent.getSerializableExtra(HAPPY_PLACE_MODEL_DATA) as HappyPlaceModel
//            }

            // By making HappyPlaceModel as Parcelable:
            // Handling getParcelableExtra() method properly as per version codes:
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA, HappyPlaceModel::class.java)
            } else {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA)
            }

            if(happyPlaceModelData != null) {
                // Set a Toolbar to act as the ActionBar for this Activity window
                setSupportActionBar(binding.happyPlaceDetailsToolBar)

                // supportActionBar: Retrieve a reference to this activity's ActionBar
                // This is to use the home back button.
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = happyPlaceModelData.title     // Setting title of toolbar using the title of HappyPlaceModel

                // Setting the click event to the back button
                binding.happyPlaceDetailsToolBar.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }

                binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceModelData.image))
                binding.tvDescription.text = happyPlaceModelData.description
                binding.tvLocation.text = happyPlaceModelData.location

                // Adding click event to btnViewOnMap for sending to MapActivity
                binding.btnViewOnMap.setOnClickListener {
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this)
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.")
                        val intent = Intent(this@HappyPlaceDetailsActivity, MapActivity::class.java)
                        intent.putExtra(HAPPY_PLACE_MODEL_DATA, happyPlaceModelData)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun setInterstitialAdListener() {
        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }
}