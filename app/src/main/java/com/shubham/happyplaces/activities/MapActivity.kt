package com.shubham.happyplaces.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.shubham.happyplaces.Constants.Companion.HAPPY_PLACE_MODEL_DATA
import com.shubham.happyplaces.R
import com.shubham.happyplaces.databinding.ActivityMapBinding
import com.shubham.happyplaces.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback  /* Implement the OnMapReadyCallback interface */ {

    private lateinit var binding: ActivityMapBinding
    private var happyPlaceModelData: HappyPlaceModel? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if(intent.hasExtra(HAPPY_PLACE_MODEL_DATA)) {
            // Handling getParcelableExtra() method properly as per version codes
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA, HappyPlaceModel::class.java)
            } else {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA)
            }
        }

        if(happyPlaceModelData != null) {

            // Set a Toolbar to act as the ActionBar for this Activity window
            setSupportActionBar(binding.mapToolBar)

            // supportActionBar: Retrieve a reference to this activity's ActionBar
            // This is to use the home back button.
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = happyPlaceModelData?.title     // Setting title of toolbar using the title of HappyPlaceModel

            // Setting the click event to the back button
            binding.mapToolBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            // To get a handle to the mapFragment, call the FragmentManager.findFragmentById method
            // and pass it the resource ID of the fragment in your layout file.
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(this)   // Call the getMapAsync method to set the callback on the fragment
        }
    }

    // Use the onMapReady callback method in OnMapReadyCallback Interface to get a handle to the GoogleMapobject.
    // The callback is triggered when the map is ready to receive user input.
    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(happyPlaceModelData?.latitude!!, happyPlaceModelData?.longitude!!)
        // Adding a marker to map at position
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(happyPlaceModelData?.location)
        )

        // Adding Animation to zoom to particular position
        // The Maps API allows you to create many different types of CameraUpdate using CameraUpdateFactory.
        // CameraUpdateFactory.newLatLngZoom(LatLng, float) gives you a CameraUpdate that changes the camera's latitude, longitude and zoom, while preserving all other properties.
        // Following move the camera instantly to position with a zoom value of 10
        val latLongZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(latLongZoom)
    }
}