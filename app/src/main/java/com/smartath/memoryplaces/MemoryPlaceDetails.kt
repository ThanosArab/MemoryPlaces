package com.smartath.memoryplaces

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smartath.memoryplaces.databinding.ActivityMainBinding
import com.smartath.memoryplaces.databinding.ActivityMemoryPlaceDetailsBinding
import com.smartath.memoryplaces.model.MemoryPlaceModel

class MemoryPlaceDetails : AppCompatActivity() {

    private var binding: ActivityMemoryPlaceDetailsBinding? = null
    private var memoryPlaceModel: MemoryPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoryPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(MainActivity.PLACE_DETAILS)){
            memoryPlaceModel = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as MemoryPlaceModel?
        }

        if (memoryPlaceModel!=null){
            binding?.titleTv?.text = memoryPlaceModel?.title
            binding?.descriptionTv?.text = memoryPlaceModel?.description
            binding?.dateTv?.text = memoryPlaceModel?.date
            binding?.locationTv?.text = memoryPlaceModel?.location

            binding?.imageIv?.setImageURI(Uri.parse(memoryPlaceModel?.image))
            setUpToolbar()

            binding?.mapViewBtn?.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.PLACE_DETAILS, memoryPlaceModel)
                startActivity(intent)
            }
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding?.placeDetailsToolbar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = memoryPlaceModel?.title

            binding?.placeDetailsToolbar?.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }
}