package mx.tecnm.tepic.ldm_u5_ejercicio4_geomapatec

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    lateinit var locacion : LocationManager
    var listaID = ArrayList<String>()
    var dataLista = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        cargarLista()
        /*baseRemota.collection("plazaPop")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    txtUbicaciones.setText("ERROR: "+firebaseFirestoreException.message)
                    return@addSnapshotListener
                }

                var resultado = ""
                posicion.clear()
                for(document in querySnapshot!!){
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!
                    resultado += data.toString()+"\n\n"
                    posicion.add(data)
                }
                txtUbicaciones.text = resultado
            }*/

        acerca.setOnClickListener{
            AlertDialog.Builder(this).setTitle("Atención")
                .setMessage("RAMON RAMIREZ HERNANDEZ \n" +
                        "  DAMARIS YAQUELIN GALLEGOS NAVARRO \n" +
                        " ESMERALDA ANAHI AREVALO VALENZUELA\n" +
                        " MARIO ALBERTO AVALOS RODRIGUEZ ")

                .setNeutralButton("Cerrar"){d,w->}
                .show()
        }

        button.setOnClickListener {
            miUbicación()
        }

        btnBuscar.setOnClickListener {
            if(edtBuscar.text.isEmpty()){
                cargarLista()
            }else{
                cargarListaBuscador(edtBuscar.text.toString())
                edtBuscar.setText("")
            }
        }


        locacion = getSystemService(Context.LOCATION_SERVICE)as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,01f,oyente)
    }

    private fun miUbicación(){
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
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPosicion = GeoPoint(it.latitude,it.longitude)
                textView2.setText("${it.latitude},${it.longitude}")
                for(item in posicion){
                    if(item.estoyEn(geoPosicion)==true){
                        AlertDialog.Builder(this)
                            .setMessage("USTED SE ENCUENTRA EN: "+item.nombre)
                            .setTitle("ATENCION")
                            .setPositiveButton("DETALLES"){a,b->
                                var nueva = Intent(this,MainActivity2::class.java)
                                nueva.putExtra("ubicacion",item.nombre)
                                startActivity(nueva)
                            }
                            .setNegativeButton("CANCELAR"){a,b->}
                            .show()
                    }
                }
            }.addOnSuccessListener {  }
    }

    fun cargarLista(){
        baseRemota.collection("plazaPop")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    mensaje("Error: " + firebaseFirestoreException.message)
                    return@addSnapshotListener
                }
                var resultado = ""
                var listaCadena=""
                posicion.clear()
                dataLista.clear()
                listaID.clear()
                for (document in querySnapshot!!) {
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!

                    resultado += data.toString() + "\n" + "\n"
                    listaCadena=data.toString()
                    posicion.add(data)
                    dataLista.add(listaCadena)
                    listaID.add(document.id)
                }
                if (dataLista.size == 0) {
                    dataLista.add("No hay data")
                }
                var adaptador =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataLista)
                lista.adapter = adaptador
            }

        lista.setOnItemClickListener { parent, view, position, id ->
            if (listaID.size == 0) {
                return@setOnItemClickListener
            }
            AlertDialog.Builder(this).setTitle("Atención")
                .setMessage("¿Qué desea hacer con\n${dataLista[position]}?")
                .setPositiveButton("Ver en el mapa"){d,w->
                    abrirMapa(listaID[position])
                }
                .setNeutralButton("Cerrar"){d,w->}
                .show()
        }
    }


    fun mensaje(s:String){
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()
    }

    fun abrirMapa(idLugar:String){
        baseRemota.collection("plazaPop")
            .document(idLugar)
            .get()
            .addOnSuccessListener {
                var mapsAct= Intent(this,MapsActivity::class.java)
                mapsAct.putExtra("id",idLugar)
                mapsAct.putExtra("data",it.getString("nombre"))
                startActivity(mapsAct)
            }
            .addOnFailureListener { Toast.makeText(this, "Error no hay conexión de red", Toast.LENGTH_LONG)
                .show() }
    }

    fun cargarListaBuscador(localPlaza:String){
        baseRemota.collection("plazaPop")
            .whereEqualTo("nombre",localPlaza)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    mensaje("Error: " + firebaseFirestoreException.message)
                    return@addSnapshotListener
                }
                var resultado = ""
                var listaCadena=""
                posicion.clear()
                dataLista.clear()
                listaID.clear()
                for (document in querySnapshot!!) {
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!

                    resultado += data.toString() + "\n" + "\n"
                    listaCadena=data.toString()
                    posicion.add(data)
                    dataLista.add(listaCadena)
                    listaID.add(document.id)
                }
                if (dataLista.size == 0) {
                    dataLista.add("No hay data")
                }
                var adaptador =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataLista)
                lista.adapter = adaptador
            }
        lista.setOnItemClickListener { parent, view, position, id ->
            if (listaID.size == 0) {
                return@setOnItemClickListener
            }
            AlertDialog.Builder(this).setTitle("Atención")
                .setMessage("¿Qué desea hacer con\n${dataLista[position]}?")
                .setPositiveButton("Ver en el mapa"){d,w->
                    abrirMapa(listaID[position])
                }
                .setNeutralButton("Detalles"){d,w->}
                .show()
        }
    }


}

class Oyente(puntero:MainActivity): LocationListener {
    var p = puntero

    override fun onLocationChanged(location: Location) {
        p.textView2.setText("${location.latitude},${location.longitude}")
        p.textView3.setText("")
        var geoPosicionGPS = GeoPoint(location.latitude,location.longitude)
        for(item in p.posicion){
            if(item.estoyEn(geoPosicionGPS)){
                p.textView3.setText("ESTAS EN: ${item.nombre.uppercase()}")
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }


}

