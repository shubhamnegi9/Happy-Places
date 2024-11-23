package com.shubham.happyplaces.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class HappyPlaceModel (
    val id: Int,
    val title: String?,             // Need to make all Strings as nullables for making Parcelable
    val description: String?,
    val date: String?,
    val location: String?,
    val image: String?,
    val latitude: Double?,
    val longitude: Double?
)/*: Serializable     // Making data class as Serializable/Parcelable to send it using intent */
    : Parcelable {
    // Need to implement below Parcelable methods
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(image)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlaceModel> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceModel {
            return HappyPlaceModel(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlaceModel?> {
            return arrayOfNulls(size)
        }
    }

}