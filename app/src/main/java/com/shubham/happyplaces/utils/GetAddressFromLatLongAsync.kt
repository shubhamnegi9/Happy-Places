package com.shubham.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import java.util.Locale

/**
 * A AsyncTask class to get the address from latitude and longitude.
 * This is a deprecated way and should be replaced with Coroutines.
 * Use GetAddressFromLatLong class
 */
class GetAddressFromLatLongAsync(context: Context, private val latitude: Double, private val longitude: Double) :
    AsyncTask<Void, String, String>(){

    /**
     * Constructs a Geocoder whose responses will be localized for the
     * given Locale.
     *
     * @param context the Context of the calling Activity
     * @param locale the desired Locale for the query results
     *
     * @throws NullPointerException if Locale is null
     */
    private val geocoder = Geocoder(context, Locale.getDefault())

    /**
     * A variable of address listener interface.
     */
    private lateinit var addressListener: AddressListener

    /**
     * Background method of AsyncTask where the background operation will be performed.
     */
    override fun doInBackground(vararg params: Void?): String {
        try {
            /**
             * Returns an array of Addresses that are known to describe the
             * area immediately surrounding the given latitude and longitude.
             */
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)  // maxResults – max number of addresses to return. Smaller numbers (1 to 5) are recommended
            if(addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]

                // We need to get the address as String. So creating StringBuilder to convert Address into String
                val sb = StringBuilder()
                for(i in 0..address.maxAddressLineIndex) {
                    Log.d("GetAddressFromLAtLongAsync", "doInBackground: address.getAddressLine(i): " + address.getAddressLine(i))
                    sb.append(address.getAddressLine(i)).append(" ")    // Appending all the address lines using space " " in between
                }
                sb.deleteCharAt(sb.length-1)    // Removes empty space at the end
                return sb.toString()
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * onPostExecute method of AsyncTask where the result will be received from doInBackground and assigned to the interface accordingly.
     */
    override fun onPostExecute(resultString: String?) {
        super.onPostExecute(resultString)

        if(resultString == null || resultString.isEmpty()) {
            addressListener.onError()
        } else {
            addressListener.onAddressFound(resultString)
        }
    }

    /**
     * A public function to execute the AsyncTask from the class is it called.
     */
    fun getAddress() {
        execute()
    }

    /**
     * A public function to set the AddressListener.
     */
    fun setAddressListener(addressListener: AddressListener) {
        this.addressListener = addressListener
    }

    /**
     * A interface for AddressListener which contains the function like success and error.
     */
    interface AddressListener {
        fun onAddressFound(address: String)
        fun onError()
    }
}