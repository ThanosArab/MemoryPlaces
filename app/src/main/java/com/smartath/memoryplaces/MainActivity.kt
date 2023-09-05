package com.smartath.memoryplaces

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartath.memoryplaces.adapter.MemoryPlaceAdapter
import com.smartath.memoryplaces.databinding.ActivityMainBinding
import com.smartath.memoryplaces.handler.DatabaseHandler
import com.smartath.memoryplaces.model.MemoryPlaceModel
import com.smartath.memoryplaces.utils.SwipeToDeleteCallback
import com.smartath.memoryplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    companion object{
        var PLACE_REQUEST_CODE = 1
        var PLACE_DETAILS = "place_details"
    }

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpToolbar()

        binding?.fabBtn?.setOnClickListener {
            val intent = Intent(this, AddMemoryPlace::class.java)
            startActivityForResult(intent, PLACE_REQUEST_CODE)
        }

        getPlacesFromDatabase()
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding?.mainToolbar)

        if (supportActionBar != null){
            supportActionBar?.title = "Memory Places"
        }
    }

    private fun setUpPlacesRecyclerView(memoryPlaceList: ArrayList<MemoryPlaceModel>){
        binding?.placesRv?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val adapter = MemoryPlaceAdapter(this, memoryPlaceList)
        binding?.placesRv?.adapter = adapter

        adapter.setOnClickListener(object : MemoryPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: MemoryPlaceModel) {
                val intent = Intent(this@MainActivity, MemoryPlaceDetails::class.java)
                intent.putExtra(PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editCallback = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapterEdit = binding?.placesRv?.adapter as MemoryPlaceAdapter
                adapterEdit.updateItem(this@MainActivity, viewHolder.adapterPosition, PLACE_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editCallback)
        editItemTouchHelper.attachToRecyclerView(binding?.placesRv)

        val deleteCallback = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapterDelete = binding?.placesRv?.adapter as MemoryPlaceAdapter
                adapterDelete.deleteItem(viewHolder.adapterPosition)

                getPlacesFromDatabase()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteCallback)
        deleteItemTouchHelper.attachToRecyclerView(binding?.placesRv)
    }

    private fun getPlacesFromDatabase(){
        val handler = DatabaseHandler(this)
        val memoryPlacesList = handler.getMemoryPlacesList()

        if(memoryPlacesList.size>0){
            binding?.placesRv?.visibility = View.VISIBLE
            binding?.emptyTv?.visibility = View.GONE

            setUpPlacesRecyclerView(memoryPlacesList)
        }
        else{
            binding?.placesRv?.visibility = View.GONE
            binding?.emptyTv?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getPlacesFromDatabase()
            }
        }
    }
}