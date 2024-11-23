package com.shubham.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.shubham.happyplaces.models.HappyPlaceModel

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        val DATABASE_NAME = "HappyPlacesDatabase"
        val DATABASE_VERSION = 1
        val HAPPY_PLACES_TABLE = "HappyPlacesTable"

        // Column names
        val KEY_ID = "_id"
        val KEY_TITLE = "title"
        val KEY_DESCRIPTION = "description"
        val KEY_DATE = "date"
        val KEY_LOCATION = "location"
        val KEY_IMAGE = "image"
        val KEY_LATITUDE = "latitude"
        val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create table with fields
        val CREATE_HAPPY_PLACES_TABLE = ("CREATE TABLE $HAPPY_PLACES_TABLE(" +
                "$KEY_ID INTEGER PRIMARY KEY, " +
                "$KEY_TITLE TEXT, " +
                "$KEY_DESCRIPTION TEXT, " +
                "$KEY_DATE TEXT, " +
                "$KEY_LOCATION TEXT, " +
                "$KEY_IMAGE TEXT, " +
                "$KEY_LATITUDE TEXT, " +
                "$KEY_LONGITUDE TEXT" + ")")
        db?.execSQL(CREATE_HAPPY_PLACES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $HAPPY_PLACES_TABLE")
        onCreate(db)
    }

    /**
     *  Function to add a new place in database
     */
    fun addHappyPlace(happyPlaceModel: HappyPlaceModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, happyPlaceModel.title)
        contentValues.put(KEY_DESCRIPTION, happyPlaceModel.description)
        contentValues.put(KEY_DATE, happyPlaceModel.date)
        contentValues.put(KEY_LOCATION, happyPlaceModel.location)
        contentValues.put(KEY_IMAGE, happyPlaceModel.image)
        contentValues.put(KEY_LATITUDE, happyPlaceModel.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlaceModel.longitude)

        // Inserting row
        val success = db.insert(HAPPY_PLACES_TABLE, null, contentValues)
        db.close()  // Close database connection

        return success
    }

    /**
     *  Function to update the existing place in database
     */
    fun updateHappyPlace(happyPlaceModel: HappyPlaceModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, happyPlaceModel.title)
        contentValues.put(KEY_DESCRIPTION, happyPlaceModel.description)
        contentValues.put(KEY_DATE, happyPlaceModel.date)
        contentValues.put(KEY_LOCATION, happyPlaceModel.location)
        contentValues.put(KEY_IMAGE, happyPlaceModel.image)
        contentValues.put(KEY_LATITUDE, happyPlaceModel.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlaceModel.longitude)

        // Updating row
        val success = db.update(HAPPY_PLACES_TABLE, contentValues, "$KEY_ID = ${happyPlaceModel.id}", null)
        db.close()  // Close database connection

        return success
    }

    /**
     *  Function to delete the existing place in database
     */
    fun deleteHappyPlace(happyPlaceModel: HappyPlaceModel): Int {
        val db = this.writableDatabase

        val success = db.delete(HAPPY_PLACES_TABLE, "$KEY_ID = ${happyPlaceModel.id}", null)
        db.close()

        return success
    }
    /**
     *  Function to get list of Happy Places from database
     */
    fun getHappyPlacesList(): ArrayList<HappyPlaceModel> {
        val happyPlaceModelList = ArrayList<HappyPlaceModel>()

        val selectQuery = "SELECT * FROM $HAPPY_PLACES_TABLE"

        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        if(cursor.moveToFirst()) {
            do {
                var id: Int = 0
                var title: String = ""
                var description: String = ""
                var date: String = ""
                var location: String = ""
                var image: String = ""
                var latitude: Double = 0.0
                var longitude: Double = 0.0

                val idColumnIndex = cursor.getColumnIndex(KEY_ID)
                val titleColumnIndex = cursor.getColumnIndex(KEY_TITLE)
                val descriptionColumnIndex = cursor.getColumnIndex(KEY_DESCRIPTION)
                val dateColumnIndex = cursor.getColumnIndex(KEY_DATE)
                val locationColumnIndex = cursor.getColumnIndex(KEY_LOCATION)
                val imageColumnIndex = cursor.getColumnIndex(KEY_IMAGE)
                val latitudeColumnIndex = cursor.getColumnIndex(KEY_LATITUDE)
                val longitudeColumnIndex = cursor.getColumnIndex(KEY_LONGITUDE)

                if(idColumnIndex != -1)
                    id = cursor.getInt(idColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_ID does not exist in the cursor")
                if(titleColumnIndex != -1)
                    title = cursor.getString(titleColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_TITLE does not exist in the cursor")
                if(descriptionColumnIndex != -1)
                    description = cursor.getString(descriptionColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_DESCRIPTION does not exist in the cursor")
                if(dateColumnIndex != -1)
                    date = cursor.getString(dateColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_DATE does not exist in the cursor")
                if(locationColumnIndex != -1)
                    location = cursor.getString(locationColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_LOCATION does not exist in the cursor")
                if(imageColumnIndex != -1)
                    image = cursor.getString(imageColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_IMAGE does not exist in the cursor")
                if(latitudeColumnIndex != -1)
                    latitude = cursor.getDouble(latitudeColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_LATITUDE does not exist in the cursor")
                if(longitudeColumnIndex != -1)
                    longitude = cursor.getDouble(longitudeColumnIndex)
                else
                    Log.e("CursorError", "Column KEY_LONGITUDE does not exist in the cursor")
                val happyPlaceModel = HappyPlaceModel(id, title, description, date, location, image, latitude, longitude)
                happyPlaceModelList.add(happyPlaceModel)
            }
            while (cursor.moveToNext())
        }

        return happyPlaceModelList
    }

}