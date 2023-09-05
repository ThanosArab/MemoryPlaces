package com.smartath.memoryplaces

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.smartath.memoryplaces.databinding.ActivityAddMemoryPlaceBinding
import com.smartath.memoryplaces.handler.DatabaseHandler
import com.smartath.memoryplaces.model.MemoryPlaceModel
import com.smartath.memoryplaces.utils.GetAddressFromLatLng
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddMemoryPlace : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityAddMemoryPlaceBinding? = null

    private var calendar: Calendar = Calendar.getInstance()
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternally: Uri? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object{
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    private var memoryPlaceModel: MemoryPlaceModel? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val galleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == RESULT_OK && result.data != null){
            val selectedImage: ImageView = findViewById(R.id.imageIv)
            selectedImage.setImageURI(result.data?.data)

            saveImageToInternally = saveImageToInternalStorage(selectedImage.drawToBitmap(Bitmap.Config.ARGB_8888))
        }
    }

    private val galleryRequestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            val permission = it.key
            val isGranted = it.value

            if(isGranted){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }
            else{
                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this@AddMemoryPlace, "Permission denied!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == RESULT_OK && result.data!=null){
            val selectedImage = result.data?.extras!!.get("data") as Bitmap
            binding?.imageIv?.setImageBitmap(selectedImage)

            saveImageToInternally = saveImageToInternalStorage(selectedImage)
        }
    }

    private val cameraRequestPermission: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted ->
        if (isGranted){
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
        else{
            Toast.makeText(this@AddMemoryPlace, "Permission denied!", Toast.LENGTH_LONG).show()
        }
    }

    private val locationLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
            binding?.locationEt?.setText(place.address)
            latitude = place.latLng!!.latitude
            longitude = place.latLng!!.longitude
        }
    }

    private val locationRequestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            val permission = it.key
            val isGranted = it.value

            if (isGranted){
                requestNewLocation()
            }
            else{
                if(permission == Manifest.permission.ACCESS_FINE_LOCATION){
                    Toast.makeText(this@AddMemoryPlace, "Permission denied!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.png")

        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        }
        catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoryPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpToolbar()
        setUpDate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if(!Places.isInitialized()){
            Places.initialize(this, resources.getString(R.string.google_maps_api_key) )
        }

        if(intent.hasExtra(MainActivity.PLACE_DETAILS)){
            memoryPlaceModel = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as MemoryPlaceModel?
        }

        if (memoryPlaceModel != null){
            supportActionBar?.title = "Edit Memory Place"

            binding?.titleEt?.setText(memoryPlaceModel?.title)
            binding?.descriptionEt?.setText(memoryPlaceModel?.description)
            binding?.dateEt?.setText(memoryPlaceModel?.date)
            binding?.locationEt?.setText(memoryPlaceModel?.location)
            latitude = memoryPlaceModel!!.latitude
            longitude = memoryPlaceModel!!.longitude

            saveImageToInternally = Uri.parse(memoryPlaceModel?.image)
            binding?.imageIv?.setImageURI(saveImageToInternally)

            binding?.saveBtn?.text = "UPDATE"
        }

        binding?.dateEt?.setOnClickListener(this)
        binding?.addImageTv?.setOnClickListener(this)
        binding?.saveBtn?.setOnClickListener(this)
        binding?.locationEt?.setOnClickListener(this)
        binding?.currentLocationTv?.setOnClickListener(this)
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding?.addPlaceToolbar)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Add Memory Place"

            binding?.addPlaceToolbar?.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun setUpDate(){
        dateListener = DatePickerDialog.OnDateSetListener{_, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding?.dateEt?.setText(sdf.format(calendar.time).toString())
        }
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding?.dateEt?.setText(sdf.format(calendar.time).toString())
    }

    override fun onClick(view: View?) {
        when(view!!.id){
            R.id.dateEt -> {
                DatePickerDialog(this@AddMemoryPlace, dateListener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.addImageTv -> {
                imagePickerDialog()
            }
            R.id.saveBtn -> {
                when{
                    binding?.titleEt?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddMemoryPlace, "Please enter a title!", Toast.LENGTH_LONG).show()
                    }
                    binding?.descriptionEt?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddMemoryPlace, "Please enter a description!", Toast.LENGTH_LONG).show()
                    }
                    binding?.locationEt?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddMemoryPlace, "Please enter a location!", Toast.LENGTH_LONG).show()
                    }
                    saveImageToInternally == null ->{
                        Toast.makeText(this@AddMemoryPlace, "Please add an image!", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        val handler = DatabaseHandler(this)

                        val place = MemoryPlaceModel(if(memoryPlaceModel==null) 0 else memoryPlaceModel!!.id, binding?.titleEt?.text.toString(),
                            binding?.descriptionEt?.text.toString(), binding?.dateEt?.text.toString(), saveImageToInternally.toString(),
                            binding?.locationEt?.text.toString(), latitude, longitude)

                        if (memoryPlaceModel == null){
                            val memoryPlace = handler.addMemoryPlace(place)
                            if (memoryPlace > 0){
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                        else{
                            val memoryPlace = handler.updateMemoryPlace(place)
                            if (memoryPlace>0){
                                setResult(RESULT_OK)
                                finish()
                            }

                        }
                    }
                }
            }
            R.id.locationEt -> {
                giveLocation()
            }
            R.id.currentLocationTv -> {
                if(!isLocationEnabled()){
                    Toast.makeText(this@AddMemoryPlace, "Your location provider is turned off. Please turn it on.", Toast.LENGTH_LONG).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                else{
                    provideMapLocation()
                }
            }
        }
    }

    private fun imagePickerDialog(){
        AlertDialog.Builder(this).setTitle("Choose a method to upload an image")
            .setItems(arrayOf("Select an image from Gallery", "Capture a photo from Camera")){ _, item ->
                when(item){
                    0 -> choosePhotoFromGallery()
                    1 -> capturePhotoWithCamera()
                }

            }.create().show()
    }

    private fun showPermissionDialog(){
        AlertDialog.Builder(this).setMessage("It looks like you have turned off the permission required for this " +
                "feature. It can be enabled in Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("CANCEL"){ dialogInterface, _ ->
                dialogInterface.dismiss()
            }.show()
    }

    private fun choosePhotoFromGallery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager()){
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(pickIntent)
            }
            else{
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
        else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                showPermissionDialog()
            }
            else{
                galleryRequestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }
    }

    private fun capturePhotoWithCamera(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            showPermissionDialog()
        }
        else{
            cameraRequestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun provideMapLocation(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            showPermissionDialog()
        }
        else{
            locationRequestPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation(){
        var locationRequest= LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(false).setMaxUpdates(1).build()

        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val lastKnownLocation : Location? = locationResult.lastLocation
            latitude = lastKnownLocation!!.latitude
            longitude = lastKnownLocation.longitude
            val addressTask = GetAddressFromLatLng(this@AddMemoryPlace, latitude, longitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?){
                    binding?.locationEt?.setText(address)
                }
                override fun onError(){
                    Log.e("Get Address: ", "Something went wrong")
                }
            })

            lifecycleScope.launch{
                val returnString = addressTask.getLocation()
                addressTask.onPostExecute(returnString)
            }
        }
    }

    private fun giveLocation(){
        try{
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddMemoryPlace)
            locationLauncher.launch(intent)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(binding != null){
            binding = null
        }
    }
}