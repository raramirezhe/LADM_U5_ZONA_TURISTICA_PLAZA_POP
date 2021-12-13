package mx.tecnm.tepic.ldm_u5_ejercicio4_geomapatec

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ldm_u5_ejercicio4_geomapatec.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var marcadores = ArrayList<Data>()
    var lat=0.0
    var long=0.0
    var localPlaza = ""
    lateinit var ubicacionCliente: FusedLocationProviderClient
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var extra = intent.extras
        localPlaza = extra!!.get("data").toString()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)

        }
        ubicacionCliente = LocationServices.getFusedLocationProviderClient(this)


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
        baseRemota.collection("plazaPop")
            .whereEqualTo("nombre",localPlaza)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    setTitle("Error: " + firebaseFirestoreException.message)
                    return@addSnapshotListener
                }//if
                var resultado = ""
                marcadores.clear()
                for (document in querySnapshot!!) {
                    var data = Data()

                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!
                    data.pos3 = document.getGeoPoint("pos3")!!
                    resultado += data.toString() + "\n" + "\n"
                    marcadores.add(data)
                    /*lat=data.posicion1.latitude
                    long=data.posicion1.longitude*/
                    lat=data.pos3.latitude
                    long=data.pos3.longitude
                }
                Toast.makeText(this, resultado, Toast.LENGTH_LONG).show()
                mMap = googleMap
                setTitle(localPlaza)

                // Add a marker in Sydney and move the camera
                val sydney = LatLng(lat,long)
                mMap.addMarker(MarkerOptions().position(sydney).title(localPlaza))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                mMap.uiSettings.isZoomControlsEnabled=true
                mMap.uiSettings.isMyLocationButtonEnabled=true
                mMap.isMyLocationEnabled = true
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@addSnapshotListener
                }
                ubicacionCliente.lastLocation.addOnSuccessListener {

                    if(it!=null){
                        val posMark=LatLng(lat,long)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posMark,18f))
                    }
                }//addonsucc
            }//add snap
    }//onMap
}