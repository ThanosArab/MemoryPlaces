package com.smartath.memoryplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*

class GetAddressFromLatLng(context: Context, private val latitude: Double, private val longitude: Double) {

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    fun getLocation():String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()){
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for(i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length-1)
                return sb.toString()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return ""
    }

    fun onPostExecute(resultString: String?){
        if (resultString == null){
            mAddressListener.onError()
        }else{
            mAddressListener.onAddressFound(resultString)
        }
    }

    fun setAddressListener(addressListener: AddressListener){
        mAddressListener = addressListener
    }

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }
}