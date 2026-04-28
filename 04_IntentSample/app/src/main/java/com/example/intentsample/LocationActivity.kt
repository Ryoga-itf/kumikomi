package com.example.intentsample

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LocationActivity : AppCompatActivity(), LocationListener, View.OnClickListener {

    private lateinit var locationManager: LocationManager

    private lateinit var wifiLatitudeTextView: TextView
    private lateinit var wifiLongitudeTextView: TextView
    private lateinit var wifiAccuracyTextView: TextView
    private lateinit var wifiAltitudeTextView: TextView

    private lateinit var gpsLatitudeTextView: TextView
    private lateinit var gpsLongitudeTextView: TextView
    private lateinit var gpsAccuracyTextView: TextView
    private lateinit var gpsAltitudeTextView: TextView

    private var locationType: Int = WIFI

    companion object {
        private const val WIFI = 0
        private const val GPS = 1
        private const val TAG = "PlaceSample"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        wifiLatitudeTextView = findViewById(R.id.text_view_wifi_latitude_value)
        wifiLongitudeTextView = findViewById(R.id.text_view_wifi_longitude_value)
        wifiAccuracyTextView = findViewById(R.id.text_view_wifi_accuracy_value)
        wifiAltitudeTextView = findViewById(R.id.text_view_wifi_altitude_value)

        gpsLatitudeTextView = findViewById(R.id.text_view_gps_latitude_value)
        gpsLongitudeTextView = findViewById(R.id.text_view_gps_longitude_value)
        gpsAccuracyTextView = findViewById(R.id.text_view_gps_accuracy_value)
        gpsAltitudeTextView = findViewById(R.id.text_view_gps_altitude_value)

        val gpsButton: Button = findViewById(R.id.button_gps)
        gpsButton.setOnClickListener(this)

        val wifiButton: Button = findViewById(R.id.button_wifi)
        wifiButton.setOnClickListener(this)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onResume() {
        Log.d(TAG, "plog onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "plog onPause")
        if (::locationManager.isInitialized && hasLocationPermission()) {
            locationManager.removeUpdates(this)
        }
        super.onPause()
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "plog onLocationChanged")

        when (locationType) {
            GPS -> {
                gpsLatitudeTextView.text = location.latitude.toString()
                gpsLongitudeTextView.text = location.longitude.toString()
                gpsAccuracyTextView.text = location.accuracy.toString()
                gpsAltitudeTextView.text = location.altitude.toString()
            }

            WIFI -> {
                wifiLatitudeTextView.text = location.latitude.toString()
                wifiLongitudeTextView.text = location.longitude.toString()
                wifiAccuracyTextView.text = location.accuracy.toString()
                wifiAltitudeTextView.text = location.altitude.toString()
            }
        }

        if (hasLocationPermission()) {
            locationManager.removeUpdates(this)
        }
    }

    @Suppress("DEPRECATION")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        when (status) {
            LocationProvider.AVAILABLE -> Log.v(TAG, "AVAILABLE")
            LocationProvider.OUT_OF_SERVICE -> Log.v(TAG, "OUT_OF_SERVICE")
            LocationProvider.TEMPORARILY_UNAVAILABLE -> Log.v(TAG, "TEMPORARILY_UNAVAILABLE")
        }
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onClick(view: View) {
        if (!hasLocationPermission()) return

        when (view.id) {
            R.id.button_gps -> {
                locationType = GPS
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    this
                )
            }

            R.id.button_wifi -> {
                locationType = WIFI
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    this
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}