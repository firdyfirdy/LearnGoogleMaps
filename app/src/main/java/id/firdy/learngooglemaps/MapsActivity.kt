package id.firdy.learngooglemaps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import id.firdy.learngooglemaps.databinding.ActivityMapsBinding

import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var m_Text = ""


    /* Default Long-Lat */
    private var currentLattitude = -6.315970
    private var currentLongitude = 106.966743

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*region Request Permission jika diperlukan */
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET
            )
        )
        /*endregion*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*region Button Listener*/
        binding.fabSearch.setOnClickListener {
            showDialog()
        }

        binding.fabRefresh.setOnClickListener {
            setMarker(
                lat = currentLattitude,
                long = currentLongitude
            )
        }
        /*endregion*/
    }

    private fun setMarker(lat: Double, long: Double) {
        if (mMap != null){
            val msg = "Saat ini: Latitude: $lat & Longitude:$long"
            val sydney = LatLng(lat, long)
            val zoomLevel = 16.0f
            mMap.addMarker(MarkerOptions().position(sydney).title(msg))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel))

            binding.txtCurrentLatitude.text = lat.toString()
            binding.txtCurrentLongitude.text = long.toString()
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLattitude = location.latitude
                    currentLongitude = location.longitude
                }
            }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                !== PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls<String>(0)),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (element in grantResults) {
            permissionsToRequest.add(element.toString())
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_CODE
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMarker(currentLattitude, currentLongitude)
    }

    private fun showDialog(){
        val customLayout = layoutInflater.inflate(R.layout.custom_dialog, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Search")

        val txtNewLat = customLayout.findViewById<AppCompatEditText>(R.id.txtLatitude)
        val txtNewLong = customLayout.findViewById<AppCompatEditText>(R.id.txtLongitude)

        builder.setPositiveButton("OK") { dialog, which ->
            /*Jika input text latitude dan longitude tidak kosong */
            if (txtNewLat.text.toString().isNotBlank() && txtNewLong.text.toString()
                    .isNotBlank()
            ) {
                setMarker(
                    txtNewLat.text.toString().toDouble(),
                    txtNewLong.text.toString().toDouble()
                )
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Harap masukan Latitude & Longitude", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        builder.setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })

        builder.setView(customLayout)
        builder.show()
    }
}