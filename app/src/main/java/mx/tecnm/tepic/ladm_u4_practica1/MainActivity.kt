package mx.tecnm.tepic.ladm_u4_practica1

import android.Manifest
import android.R
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u4_practica1.databinding.ActivityMainBinding
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var listCalls =
        listOf<String>(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.TYPE).toTypedArray()
    private var dataArray = ArrayList<String>()
    var idArray = ArrayList<String>()
    var db = FirebaseFirestore.getInstance()
    var numbers = ArrayList<String>()
    var Inicio =true
    var ID_Llamada = ""
    public var Datos = ArrayList<Contacto>()
    //var hilo = Hilo(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        checkPerm()
        getnumber()
        loadMissedCalls()



        binding.llamadas.setOnClickListener {
            loadMissedCalls()
            binding.missedCallsList.adapter =
                ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataArray)
            this.registerForContextMenu(binding.missedCallsList)
        }
        binding.agregarContacto.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
        binding.btnActivar.setOnClickListener {
            /*PERMISO - LEER LLAMADAS*/
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_CALL_LOG)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), 2)
            }
            //hilo.start()
        }


    }

    private fun checkPerm() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                369
            )
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                1
            )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(
            this,
            Array(1) { Manifest.permission.READ_CALL_LOG }, 101
        ) else displayLog()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) displayLog()
    }

    private fun displayLog() {

    }

    private fun getnumber() {
        try {
            db.collection("CONTEST").addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (doc in querySnapshot!!) {
                    val cad = doc.get("telefono")
                    numbers.add(cad.toString())
                }
            }
        } catch (e: Exception) {
            Log.w("Error", e.message!!)
        }
    }

    private fun loadMissedCalls() {
        dataArray.clear()
        val test = contentResolver.query(CallLog.Calls.CONTENT_URI, listCalls, null, null, null)
        if (test != null) {
            if (test.moveToLast()) {
                do {
                    if (test.getInt(2) == 3) {

                        if (numbers.contains(test.getString(1))) {
                            var cad ="#"+ test.getString(0)
                            cad += "\nTelefono: " + test.getString(1)
                            dataArray.add(cad)

                        }
                    }
                } while (test.moveToPrevious())
            }
        }
    }
    public fun Accion(){
        var Cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,null,null,null,"date DESC")

        if(Cursor!!.moveToFirst()){
            var Num_Telefono = Cursor.getColumnIndex(CallLog.Calls.NUMBER)
            var Tipo_Llamada = Cursor.getColumnIndex(CallLog.Calls.TYPE)
            var ID = Cursor.getColumnIndex(CallLog.Calls._ID)

            if(Inicio){ID_Llamada = Cursor.getString(ID)
                Inicio = false}

            if (Cursor.getString(ID) != ID_Llamada){
                // El Tipo "3" corresponde a las llamadas perdidas o no contestadas
                if (Cursor.getInt(Tipo_Llamada) == 3){
                    for (i in 0..(Datos.size-1)){
                        //Si el numero de telefono coincide con alguno de nuestras listas
                        if(Cursor.getString(Num_Telefono) == Datos.get(i).Telefono){
                            Enviar_SMS(Datos.get(i))
                            break
                        }//if
                    }//for
                }//if
                ID_Llamada = Cursor.getString(ID)
            }
        }
    }//Accion
    private fun Enviar_SMS(persona: Contacto){
        var Mensaje = "NO DEVOLVERE TU LLAMADA, POR FAVOR NO INSISTAS"

        if (persona.Tipo.equals("LISTA BLANCA")){
            Mensaje = "POR EL MOMENTO NO ESTOY DISPONIBLE, ME COMUNICARE EN CUANTO ME SEA POSIBLE"
        } else {
            var datosInsertar = hashMapOf(
                "deseado" to "NO DESEADOS",
                "nombre" to persona.Nombre,
                "telefono" to persona.Telefono
            )

            db.collection("CONTEST").add(datosInsertar as Any)

                .addOnSuccessListener {}

        }

        SmsManager.getDefault().sendTextMessage(persona.Telefono,null,
            Mensaje,null,null)
    }//Enviar_SMS
}
class Hilo(p:MainActivity) : Thread(){

    val Puntero = p

    override fun run() {
        super.run()
        while (true){
            sleep(1000)
            Puntero.runOnUiThread {
                Puntero.Accion()
            }
        }
    }
}//class Hilo