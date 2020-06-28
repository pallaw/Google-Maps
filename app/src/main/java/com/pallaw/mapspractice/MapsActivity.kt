package com.pallaw.mapspractice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mLocationPermissionGranted: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 678
    private val ERROR_DIALOG_REQUEST: Int = 123
    private lateinit var mMap: GoogleMap
    private val TAG = "MapsActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (isServiceAvailable()) {
            getLocationPermission()
        } else {
            Toast.makeText(
                this,
                "Your device doesn't have google play service, so you can not use google maps",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (mLocationPermissionGranted) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "getDeviceLocation: current location fetched ")
                    val result = it.result
                    result?.let {
                        moveCameraTo(LatLng(it.latitude, it.longitude))
                    }
                } else
                    Log.d(TAG, "getDeviceLocation: current location is not fetched ")
            }
        }
    }

    private fun moveCameraTo(result: LatLng) {
        result.let {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10.5f))
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        val permissionList = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionGranted = false
                ActivityCompat.requestPermissions(
                    this,
                    permissionList,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                mLocationPermissionGranted = true
                initMap()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                    initMap()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isServiceAvailable(): Boolean {
        val googlePlayServicesAvailable =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServiceAvailable: Google play service is working")
            return true
        } else if (GoogleApiAvailability.getInstance()
                .isUserResolvableError(googlePlayServicesAvailable)
        ) {
            Log.d(TAG, "isServiceAvailable: Error but can be tackeled")
            val errorDialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, googlePlayServicesAvailable, ERROR_DIALOG_REQUEST)
            errorDialog.show()
        } else {
            Log.d(TAG, "isServiceAvailable: You can't make map request")
        }
        return false
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        getDeviceLocation()
    }
}