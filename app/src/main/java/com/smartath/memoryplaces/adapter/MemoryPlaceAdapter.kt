package com.smartath.memoryplaces.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smartath.memoryplaces.AddMemoryPlace
import com.smartath.memoryplaces.MainActivity
import com.smartath.memoryplaces.databinding.ItemPlaceLayoutBinding
import com.smartath.memoryplaces.handler.DatabaseHandler
import com.smartath.memoryplaces.model.MemoryPlaceModel

class MemoryPlaceAdapter(val context: Context, private var places: ArrayList<MemoryPlaceModel>): RecyclerView.Adapter<MemoryPlaceAdapter.ViewHolder>() {

    interface OnClickListener{
        fun onClick(position: Int, model: MemoryPlaceModel)
    }

    private var onClickListener: OnClickListener? = null

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    fun updateItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(activity, AddMemoryPlace::class.java)
        intent.putExtra(MainActivity.PLACE_DETAILS, places[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun deleteItem(position: Int){
        val handler = DatabaseHandler(context)
        val isDelete = handler.deleteMemoryPlace(places[position])

        if (isDelete>0){
            places.removeAt(position)
            notifyItemRemoved(position)
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryPlaceAdapter.ViewHolder {
        return ViewHolder(ItemPlaceLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MemoryPlaceAdapter.ViewHolder, position: Int) {
        val model = places[position]

        holder.imageIv?.setImageURI(Uri.parse(model.image))
        holder.titleTv?.text = model.title
        holder.descriptionTv?.text = model.description

        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener?.onClick(position, model)
            }
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }

    inner class ViewHolder(binding: ItemPlaceLayoutBinding?): RecyclerView.ViewHolder(binding?.root!!){
        val imageIv = binding?.imageIv
        val titleTv = binding?.titleTv
        val descriptionTv = binding?.descriptionTv
    }
}