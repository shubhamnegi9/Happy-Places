package com.shubham.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.shubham.happyplaces.Constants.Companion.HAPPY_PLACE_MODEL_DATA
import com.shubham.happyplaces.activities.AddHappyPlacesActivity
import com.shubham.happyplaces.database.DatabaseHandler
import com.shubham.happyplaces.databinding.ItemHappyPlaceBinding
import com.shubham.happyplaces.models.HappyPlaceModel


open class HappyPlacesAdapter(private val happyPlaceModelList: ArrayList<HappyPlaceModel>,
                              private val itemClickListener: (happyPlaceModel: HappyPlaceModel) -> Unit):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return  happyPlaceModelList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val happyPlaceModel = happyPlaceModelList[position]
        if(holder is MyViewHolder) {
            holder.ivPlaceImage.setImageURI(Uri.parse(happyPlaceModel.image))
            holder.tvTitle.text = happyPlaceModel.title
            holder.tvDescription.text = happyPlaceModel.description
            holder.tvDate.text = happyPlaceModel.date
            holder.itemView.setOnClickListener {
                itemClickListener.invoke(happyPlaceModel)
            }
        }
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivPlaceImage = binding.ivPlaceImage
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvDate = binding.tvDate
    }

    /**
     * A function to edit the added happy place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity, position: Int,
                       startActivityLauncher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(activity, AddHappyPlacesActivity::class.java)
        intent.putExtra(HAPPY_PLACE_MODEL_DATA, happyPlaceModelList[position])
        startActivityLauncher.launch(intent)

        // Notify any registered observers that the item at position has changed.
        notifyItemChanged(position)
    }

    /**
     * A function to delete the added happy place detail from the local storage.
     */
    fun notifyDeleteItem(activity: Activity, position: Int) {

        val databaseHandler = DatabaseHandler(activity)
        val status = databaseHandler.deleteHappyPlace(happyPlaceModelList[position])
        if(status > -1) {
            Toast.makeText(activity, "Happy Place ${happyPlaceModelList[position].title} Deleted", Toast.LENGTH_SHORT).show()

            happyPlaceModelList.removeAt(position)

            // Notify any registered observers that the item at position has deleted.
            notifyItemRemoved(position)
        }
    }
}