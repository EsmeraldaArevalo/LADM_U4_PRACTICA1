package mx.tecnm.tepic.ladm_u4_practica1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class dB(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Messages (idMessage INTEGER NOT NULL PRIMARY KEY, mensajeblanca VARCHAR(200), mensajenegra VARCHAR(200))")
        val data = ContentValues()
        data.put("idMessage", 1)
        data.put("mensajeblanca", "")
        data.put("mensajenegra", "")

        db.insert("Messages", null, data)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}