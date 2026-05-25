package com.example.cuartapp2026m

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Modelo de datos para manejar las citas de forma limpia en Compose
data class Cita(
    val id: Int = -1,
    val nombre: String,
    val telefono: String,
    val fecha: String,
    val hora: String
)

class DatabasOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CitasDB.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "citas"

        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_TELEFONO = "telefono"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_FECHA TEXT,
                $COLUMN_HORA TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // --- OPERACIONES CRUD ---

    // CREATE
    fun insertCita(cita: Cita): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, cita.nombre)
            put(COLUMN_TELEFONO, cita.telefono)
            put(COLUMN_FECHA, cita.fecha)
            put(COLUMN_HORA, cita.hora)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    // READ ALL
    fun getAllCitas(): List<Cita> {
        val lista = mutableListOf<Cita>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_FECHA ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val cita = Cita(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)),
                    hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA))
                )
                lista.add(cita)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // READ SINGLE (Para edición)
    fun getCitaById(id: Int): Cita? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var cita: Cita? = null
        if (cursor.moveToFirst()) {
            cita = Cita(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA)),
                hora = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HORA))
            )
        }
        cursor.close()
        db.close()
        return cita
    }

    // UPDATE
    fun updateCita(cita: Cita): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, cita.nombre)
            put(COLUMN_TELEFONO, cita.telefono)
            put(COLUMN_FECHA, cita.fecha)
            put(COLUMN_HORA, cita.hora)
        }
        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(cita.id.toString()))
        db.close()
        return result > 0
    }

    // DELETE
    fun deleteCita(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}