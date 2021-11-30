package mx.tecnm.tepic.ladm_u4_practica1

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import mx.tecnm.tepic.ladm_u4_practica1.databinding.ActivityMain2Binding


class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    var db = FirebaseFirestore.getInstance()
    private var dataArray = ArrayList<String>()
    private var idArray = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.agregarContacto.setOnClickListener {
            if(requiredEditText())return@setOnClickListener
            val data = hashMapOf(
                "telefono" to binding.numeroTelefono.text.toString().toLong(),
                "nombre" to binding.nombreContacto.text.toString(),
                "deseado" to binding.radioButton.isChecked
            )
            db.collection("CONTEST").add(data as Any)
            clear()
        }
        binding.verContactos.setOnClickListener {
            getContacts()

        }
    }

    private fun getContacts() {

        db.collection("CONTEST").addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            dataArray.clear()
            idArray.clear()
            querySnapshot!!.documents
            for (doc in querySnapshot) {
                val cad = "Nombre: ${doc.getString("nombre")}\n" +
                        "Telefono: ${doc.get("telefono")}\n" +
                        "Llamada deseada: ${if(doc.getBoolean("deseado") == true) "si" else "no"}\n"
                dataArray.add(cad)
                idArray.add(doc.id)
            }
            if (dataArray.isEmpty()) dataArray.add("No se encontró ningún contacto")
            binding.listContact.adapter =
                ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataArray)
            this.registerForContextMenu(binding.listContact)
            binding.listContact.setOnItemClickListener { _, _, i, _ ->
                dialogDelUpt(i)
            }
        }
    }

    private fun dialogDelUpt(index: Int) {
        val id = this.idArray[index]
        AlertDialog.Builder(this).setTitle("Atencion!")
            .setMessage("¿Que desea hacer con \n ${dataArray[index]}?")
            .setPositiveButton("Cancelar") { _, _ -> }
            .setNegativeButton("Eliminar") { _, _ -> delete(id) }
            .show()
    }

    private fun delete(id: String) {
        db.collection("CONTEST").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Eliminado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
            }
    }

    private fun requiredEditText(): Boolean {
        if (binding.nombreContacto.text.toString().trim() == "") {
            binding.nombreContacto.error = "El nombre del contacto es requerido!"
            binding.nombreContacto.hint = "Ingresa el nombre del contacto"
            return true
        }
        if (binding.numeroTelefono.text.toString().trim() == "") {
            binding.numeroTelefono.error = "El telefono es requerido!"
            binding.numeroTelefono.hint = "Ingresa el numero de telefono"
            return true

        }

        return false
    }

    private fun clear() {
        binding.nombreContacto.setText("")
        binding.numeroTelefono.setText("")
    }
}