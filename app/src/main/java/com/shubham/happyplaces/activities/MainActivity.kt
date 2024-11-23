package com.shubham.happyplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.shubham.happyplaces.Constants.Companion.HAPPY_PLACE_MODEL_DATA
import com.shubham.happyplaces.R
import com.shubham.happyplaces.adapters.HappyPlacesAdapter
import com.shubham.happyplaces.database.DatabaseHandler
import com.shubham.happyplaces.databinding.ActivityMainBinding
import com.shubham.happyplaces.models.HappyPlaceModel
import com.shubham.happyplaces.utils.SwipeToDeleteCallback
import com.shubham.happyplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var TAG = "AddHappyPlaces"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this@MainActivity)
        // Load Banner Ad
        val adRequest = AdRequest.Builder().build()
        binding.bannerAdView.loadAd(adRequest)

        // Set a Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(binding.happyPlacesToolBar)
        supportActionBar?.title = getString(R.string.app_name)


        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlacesActivity::class.java)
            startActivityResult.launch(intent)      // Replacement for deprecated method: startActivityForResult()
        }

        getHappyPlacesListFromDB()
    }

    // Gets the latest list from the local database
    private fun getHappyPlacesListFromDB() {
        val databaseHandler = DatabaseHandler(this)
        val happyPlacesList = databaseHandler.getHappyPlacesList()
//        for(i in happyPlacesList) {
//            Log.d(TAG, "getHappyPlacesListFromDB: title: ${i.title}")
//            Log.d(TAG, "getHappyPlacesListFromDB: description: ${i.description}")
//        }
        if(happyPlacesList.size > 0) {
            setRecyclerViewForHappyPlaces(happyPlacesList)
            binding.rvHappyPlacesList.visibility = View.VISIBLE
            binding.tvNoRecordsAvailable.visibility = View.GONE
        } else {
            binding.rvHappyPlacesList.visibility = View.GONE
            binding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    private fun setRecyclerViewForHappyPlaces(happyPlacesList: ArrayList<HappyPlaceModel>) {
        val happyPlacesAdapter = HappyPlacesAdapter(happyPlacesList) {
                happyPlaceModel ->
                    // Triggered Intent on clicking any recyclerView Item to open HappyPlaceDetailsActivity
                    val intent = Intent(this@MainActivity, HappyPlaceDetailsActivity::class.java)
                    // Making HappyPlaceModel as Serializable/Parcelable:
                    intent.putExtra(HAPPY_PLACE_MODEL_DATA, happyPlaceModel)
                    startActivity(intent)
            }
        binding.rvHappyPlacesList.setHasFixedSize(true)     // hasFixedSize – true if adapter changes cannot affect the size of the RecyclerView
        binding.rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        binding.rvHappyPlacesList.adapter = happyPlacesAdapter


        // Adding Swipe to Edit functionality to individual recycler view items
        // Creating swipeToEditCallback object for ItemTouchHelper class
        val swipeToEditCallback = object: SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.layoutPosition, startActivityResult)
            }

        }
        // Passing the callback object to ItemTouchHelper and attaching it to binding.rvHappyPlacesList RecyclerView
        val editItemTouchHelper = ItemTouchHelper(swipeToEditCallback)
        editItemTouchHelper.attachToRecyclerView(binding.rvHappyPlacesList)


        // Adding Swipe to Delete functionality to individual recycler view items
        // Creating swipeToDeleteCallback object for ItemTouchHelper class
        val swipeToDeleteCallback = object: SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.notifyDeleteItem(this@MainActivity, viewHolder.layoutPosition)
//                getHappyPlacesListFromDB()        // Not needed do as happyPlaceModelList.removeAt(position) and notifyItemRemoved() inside notifyDeleteItem will take care of this
                val databaseHandler = DatabaseHandler(this@MainActivity)
                val happyPlacesList = databaseHandler.getHappyPlacesList()
                if(happyPlacesList.isEmpty()) {
                    binding.rvHappyPlacesList.visibility = View.GONE
                    binding.tvNoRecordsAvailable.visibility = View.VISIBLE
                }
            }

        }
        // Passing the callback object to ItemTouchHelper and attaching it to binding.rvHappyPlacesList RecyclerView
        val deleteItemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvHappyPlacesList)
    }

    val startActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if(result.resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromDB()
            } else {
                Log.d(TAG, "Cancelled or Back Press")
            }
    }
}