package com.example.kaledarz

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class MyDatabaseHelper(val context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Events.db"
        const val TABLE_NAME = "events"
        const val COLUMN_ID = "_id"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_INTERVAL = "interval"
        const val COLUMN_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PRODUCTS_TABLE = (
                "CREATE TABLE $TABLE_NAME ( " +
                        "$COLUMN_ID  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$COLUMN_DATE DATE, " +
                        "$COLUMN_TIME  TEXT, " +
                        "$COLUMN_INTERVAL  INTEGER, " +
                        "$COLUMN_CONTENT  TEXT );")
        db?.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        //onCreate(db)
    }

    fun deleteEvent(id: String): Boolean {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID=$id", null) > 0
    }

    fun addGame(note: Note) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_DATE, note.date)
        cv.put(COLUMN_TIME, note.time)
        cv.put(COLUMN_INTERVAL, note.interval)
        cv.put(COLUMN_CONTENT, note.content)

        val result = db.insert(TABLE_NAME, null, cv)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    fun readAllData(data: String): ArrayList<Note> {
        val query = "Select * from $TABLE_NAME where $COLUMN_DATE='$data';"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNotes(cursor)
    }

    fun readOneData(id: String): Note {
        val query = "Select * from $TABLE_NAME where $COLUMN_ID='$id';"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNote(cursor, id)
    }

    private fun cursorToNotes(cursor: Cursor?): ArrayList<Note> {
        val notes = ArrayList<Note>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val note = Note(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3).toInt(),
                    cursor.getString(4),
                    false
                )
                notes.add(note)
            }
        }
        return notes
    }

    private fun cursorToNote(cursor: Cursor?, id: String): Note {
        var note = Note()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == id) {
                    note = Note(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3).toInt(),
                        cursor.getString(4),
                        false
                    )
                    break
                }
            }
        }
        return note
    }
}
