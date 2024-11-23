package com.shubham.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.shubham.happyplaces.Constants.Companion.GOOGLE_MAPS_API_KEY
import com.shubham.happyplaces.Constants.Companion.HAPPY_PLACE_MODEL_DATA
import com.shubham.happyplaces.Constants.Companion.IMAGE_DIRECTORY
import com.shubham.happyplaces.R
import com.shubham.happyplaces.database.DatabaseHandler
import com.shubham.happyplaces.databinding.ActivityAddHappyPlacesBinding
import com.shubham.happyplaces.models.HappyPlaceModel
import com.shubham.happyplaces.utils.GetAddressFromLatLong
import com.shubham.happyplaces.utils.GetAddressFromLatLongAsync
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Calendar
import java.util.UUID

class AddHappyPlacesActivity : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityAddHappyPlacesBinding
    private var TAG = "AddHappyPlaces"
    var savedImagePath: Uri? = null
    var mLatitude: Double? = 0.0
    var mLongitude: Double? = 0.0
    private var happyPlaceModelData: HappyPlaceModel? = null
    // A fused location provider client variable which is used to get the user's current location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddHappyPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the Fused location variable
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the Google Places sdk if it is not initialized earlier using the api key.
        if(!Places.isInitialized()) {
            Places.initialize(this@AddHappyPlacesActivity, GOOGLE_MAPS_API_KEY)
        }

        // Set a Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(binding.addHappyPlacesToolBar)

        // supportActionBar: Retrieve a reference to this activity's ActionBar
        // This is to use the home back button.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_happy_places)

        // Setting the click event to the back button
        binding.addHappyPlacesToolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // If we get HappyPlacesModel data using Intent from MainActivity for editing the details
        // then filling the UIs using this data
        if(intent.hasExtra(HAPPY_PLACE_MODEL_DATA)) {
            // Handling getParcelableExtra() method properly as per version codes
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA, HappyPlaceModel::class.java)
            } else {
                happyPlaceModelData =
                    intent.getParcelableExtra(HAPPY_PLACE_MODEL_DATA)
            }

            // Setting the title of Toolbar
            supportActionBar?.title = getString(R.string.edit_happy_place)

            binding.etTitle.setText(happyPlaceModelData?.title)
            binding.etDescription.setText(happyPlaceModelData?.description)
            binding.etDate.setText(happyPlaceModelData?.date)
            binding.etLocation.setText(happyPlaceModelData?.location)
            binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceModelData?.image))
            savedImagePath = Uri.parse(happyPlaceModelData?.image)
            mLatitude = happyPlaceModelData?.latitude
            mLatitude = happyPlaceModelData?.latitude

            // Changing btn text to "UPDATE"
            binding.btnSave.text = getString(R.string.btn_update_text)
        }

//        binding.etDate.setOnClickListener {
//            showDatePickerDialog()
//        }
        binding.etDate.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)
        binding.tvSelectCurrentLocation.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.btnToolbar.setOnClickListener(this)
    }

    private fun isLocationEnabled(): Boolean {
        // locationManager provides access to system location services
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.et_date
                    -> showDatePickerDialog()
            R.id.tv_add_image
                    -> showAddImageDialog()
            R.id.btn_save -> {
                when {
                    binding.etTitle.text.isNullOrEmpty()
                        -> Toast.makeText(this, "Please Enter Title", Toast.LENGTH_SHORT).show()
                    binding.etDescription.text.isNullOrEmpty()
                        -> Toast.makeText(this, "Please Enter Description", Toast.LENGTH_SHORT).show()
                    binding.etDate.text.isNullOrEmpty()
                    -> Toast.makeText(this, "Please Select Date", Toast.LENGTH_SHORT).show()
                    binding.etLocation.text.isNullOrEmpty()
                        -> Toast.makeText(this, "Please Enter Location", Toast.LENGTH_SHORT).show()
                    savedImagePath == null
                        -> Toast.makeText(this, "Please Add Image", Toast.LENGTH_SHORT).show()
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            happyPlaceModelData?.id ?: 0,               // Pass happyPlaceModelData?.id in case of updating else pass 0 in case of adding
                            binding.etTitle.text.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            savedImagePath.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val databaseHandler = DatabaseHandler(this)
                        if(happyPlaceModelData == null) {
                            val status = databaseHandler.addHappyPlace(happyPlaceModel)
                            if(status > -1) {
                                Toast.makeText(this, "The happy place details are added successfully", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)   // Set the result that this activity will return to its caller i.e., MainActivity
                                finish()    // Finish this activity after inserting in Database
                            }
                        } else {
                            val status = databaseHandler.updateHappyPlace(happyPlaceModel)
                            if(status > -1) {
                                Toast.makeText(this, "The happy place details are updated successfully", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)   // Set the result that this activity will return to its caller i.e., MainActivity
                                finish()    // Finish this activity after inserting in Database
                            }
                        }
                    }
                }
            }
            R.id.et_location -> {
                try {
                    // These are the list of fields which we required is passed to Autocomplete Intent
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    // Start the autocomplete intent with a unique request code.
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddHappyPlacesActivity)
                    placesAutoCompleteLauncher.launch(intent)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                // Checking if location is enabled on the user's device
                if(!isLocationEnabled()) {
                    Toast.makeText(this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT).show()

                    // Intent to open the location settings of device
                    // This will redirect you to settings from where you need to turn on the location provider
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    // Toast.makeText(this, "Your location provider is already turned on.", Toast.LENGTH_SHORT).show()

                    // Asking the location permission on runtime using Dexter Library
                    Dexter.withContext(this)
                        .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(object : MultiplePermissionsListener {                                // ctrl+shift+enter
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                Log.d(TAG, "onPermissionsChecked: areAllPermissionsGranted: " + report?.areAllPermissionsGranted())
                                if(report?.areAllPermissionsGranted() == true) {
                                    requestLocationData()
                                }

                                Log.d(TAG, "onPermissionsChecked: report?.isAnyPermissionPermanentlyDenied: " + report?.isAnyPermissionPermanentlyDenied)
                                if(report?.isAnyPermissionPermanentlyDenied==true) {
                                    Toast.makeText(this@AddHappyPlacesActivity,
                                        "You have denied location permission. Please allow as it is mandatory",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationaleDialogForPermission()
                            }
                        })
                        .withErrorListener { error -> Log.i("Dexter: ", "onError: " + error.toString()) }
                        .onSameThread()
                        .check()
                }
            }
            R.id.btn_toolbar -> {
                binding.etTitle.text?.clear()
                binding.etDescription.text?.clear()
                binding.etDate.text?.clear()
                binding.etLocation.text?.clear()
                binding.ivPlaceImage.setImageResource(R.drawable.add_screen_image_placeholder)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val date = calendar.get(Calendar.DATE)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val dpd = DatePickerDialog(this,
            // DateSetListener
            { view, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth+1}/$selectedYear"
                binding.etDate.setText(selectedDate)
            },
            year, month, date)
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAddImageDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems) {
            _, which ->
            when(which) {
                0 -> choosePhotoFromGallery()
                1 -> capturePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    /**
     * A method is used for image selection from GALLERY / PHOTOS of phone storage.
     */

    private fun choosePhotoFromGallery() {
        // Requesting permission to access user's storage using Dexter Library
        var dexterPermissionListener: DexterBuilder.MultiPermissionListener
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dexterPermissionListener = Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
        } else {
            dexterPermissionListener = Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        }

        dexterPermissionListener
            .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
//                        Toast.makeText(this@AddHappyPlacesActivity,
//                            "Storage Access permissions are granted",
//                            Toast.LENGTH_SHORT).show()

                            // Here after all the permission are granted launch the gallery to select and image
                            // Intent to open the gallery and pick image from there
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            galleryLauncher.launch(intent)
                        }

                        if (report?.isAnyPermissionPermanentlyDenied == true) {
                            Toast.makeText(
                                this@AddHappyPlacesActivity,
                                "You have denied storage permission. Please allow as it is mandatory",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationaleDialogForPermission()
                    }

                })
                .withErrorListener { error -> Log.e("Dexter: ", "Error: $error") }
                .onSameThread()
                .check()

    }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationaleDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage(("It looks like you have turned off permissions required for this feature. " +
                    "It can be enabled under Application Settings"))
            .setPositiveButton("GO TO SETTINGS") {
                _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
            }
            .setNegativeButton("CANCEL") {
                dialog, _ ->
                    dialog.dismiss()
            }
            .show()
    }

    /**
     * A method is used asking the permission for camera capturing and selection from Camera.
     */
    private fun capturePhotoFromCamera(){
//        Toast.makeText(this, "Capture Photo from camera selected", Toast.LENGTH_SHORT).show()

        // Requesting permission to access camera using Dexter Library
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA
            )
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report?.areAllPermissionsGranted() == true) {
                        // Here after all the permission are granted launch the CAMERA to capture an image.
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(intent)
                    }

                    if(report?.isAnyPermissionPermanentlyDenied == true) {
                        Toast.makeText(this@AddHappyPlacesActivity,
                            "You have denied camera permission. Please allow as it is mandatory",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationaleDialogForPermission()
                }

            })
            .withErrorListener { error -> Log.e("Dexter: ", "Error: $error") }
            .onSameThread()
            .check()
    }

    val galleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
                if(result.resultCode == RESULT_OK && result.data != null) {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data?.data)
                    savedImagePath = saveImageToInternalStorage(imageBitmap)
                    Log.d(TAG, "Saved Image Path: $savedImagePath")
                    binding.ivPlaceImage.setImageURI(result.data?.data)
                }
        }

    val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
                if(result.resultCode == RESULT_OK && result.data != null) {
                    val thumbnail = result.data?.extras?.get("data") as Bitmap
                    savedImagePath = saveImageToInternalStorage(thumbnail)
                    Log.d(TAG, "Saved Image Path: $savedImagePath")
                    saveImageToInternalStorage(thumbnail)
                    binding.ivPlaceImage.setImageBitmap(thumbnail)
                }
        }

    /**
     * A function to save a copy of an image to internal storage for HappyPlaceApp to use
     * and return the saved image uri.
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The below line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        val fileObj = wrapper.getDir(IMAGE_DIRECTORY, MODE_PRIVATE)

        // Create a file to save the image
        val file = File(fileObj, "${UUID.randomUUID()}.jpg")        // UUID.randomUUID() -> generates random Unique UserID

        // Creating OutputStream for storing/output the image file to device
        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flush the stream
            stream.flush()
            // Close stream
            stream.close()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        // Return the saved image uri
        return Uri.parse(file.absoluteFile.toString())
    }

    val placesAutoCompleteLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
        if(result.resultCode == RESULT_OK && result.data != null) {
            val data: Intent = result.data!!
            val place: Place? = Autocomplete.getPlaceFromIntent(data)
            binding.etLocation.setText(place?.address)
            mLatitude = place?.latLng?.latitude
            mLongitude = place?.latLng?.longitude
        }
    }

    /**
     * A function to request the current location updates using the fused location provider client.
     */
    @SuppressLint("MissingPermission")      // Suppressed the lint warning for handling missing permission here,
    // as we are already handling it before calling this function
    private fun requestLocationData() {
//        // Showing progress dialog before requesting location data
//        showCustomProgressDialog()
        binding.tilLocation.setHint(R.string.edit_text_hint_please_wait)

        // Create a LocationRequest object and set the desired parameters, such as update intervals, priority, and accuracy.
        // time in milliseconds, how often you want to get location updates
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)   // Limit to one update
            .build()

        // Use the requestLocationUpdates() method of the FusedLocationProviderClient to start receiving location updates.
        // You'll need to provide a LocationCallback to handle the incoming location updates.
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    /**
     * A location callback object of fused location provider client where we will get the current location details.
     */
    private val mLocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLatitude = mLastLocation?.latitude
            Log.d(TAG, "Current Latitude: $mLatitude")

            mLongitude = mLastLocation?.longitude
            Log.d(TAG, "Current Longitude: $mLongitude")

            if(mLatitude != null && mLongitude != null) {
                // Getting Address using AsyncTask (Deprecated way):
                /*
                    val getAddressFromLatLongAsync = GetAddressFromLatLongAsync(this@AddHappyPlacesActivity, mLatitude!!, mLongitude!!)
                    getAddressFromLatLongAsync.setAddressListener(object: GetAddressFromLatLongAsync.AddressListener {
                        override fun onAddressFound(address: String) {
                            binding.etLocation.setText(address)
                        }

                        override fun onError() {
                            Log.e(TAG, "onError: Something went wrong!!")
                        }

                    })
                    getAddressFromLatLongAsync.getAddress()
                */


                // Getting Address using Coroutines:
                val getAddressFromLatLong = GetAddressFromLatLong(this@AddHappyPlacesActivity, mLatitude!!, mLongitude!!)
                getAddressFromLatLong.setAddressListener(object: GetAddressFromLatLong.AddressListener {
                    override fun onAddressFound(address: String) {
                        binding.tilLocation.setHint(R.string.edit_text_hint_location)
                        binding.etLocation.setText(address)
                    }

                    override fun onError() {
                        binding.tilLocation.setHint(R.string.edit_text_hint_location)
                        Log.e(TAG, "onError: Something went wrong!!")
                    }

                })
                // CoroutineScope tied to this LifecycleOwner's Lifecycle.
                // This scope will be cancelled when the Lifecycle is destroyed
                lifecycleScope.launch {
                    getAddressFromLatLong.launchBackgroundProcessForAddress()   //starts the task to get the address in text from the lat and lng values
                }
            }
        }
    }
}