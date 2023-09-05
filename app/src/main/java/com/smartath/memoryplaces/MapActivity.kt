package com.smartath.memoryplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.smartath.memoryplaces.databinding.ActivityMapBinding
import com.smartath.memoryplaces.model.MemoryPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityMapBinding? = null

    private var memoryPlaceModel: MemoryPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(MainActivity.PLACE_DETAILS)){
            memoryPlaceModel = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as MemoryPlaceModel?
        }

        if (memoryPlaceModel!=null){
            setUpToolbar()

            val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding?.mapToolbar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = memoryPlaceModel?.title
        }
        binding?.mapToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(memoryPlaceModel!!.latitude, memoryPlaceModel!!.longitude)

        googleMap.addMarker(MarkerOptions().position(position).title(memoryPlaceModel!!.title))

        val zoomView = CameraUpdateFactory.newLatLngZoom(position, 10f)
        googleMap.animateCamera(zoomView)
    }
}