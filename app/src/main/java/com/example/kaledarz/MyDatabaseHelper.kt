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
        const val ID_COLUMN = "_id"
        const val DATE_COLUMN = "date"
        const val START_TIME_COLUMN = "start_time"
        const val END_TIME_COLUMN = "end_time"
        const val INTERVAL_COLUMN = "interval"
        const val CONTENT_COLUMN = "content"
        const val DONE_MARK_COLUMN = "done_mark"

        const val ID_CURSOR_POSITION = 0
        const val DATE_CURSOR_POSITION = 1
        const val START_TIME_CURSOR_POSITION = 2
        const val END_TIME_CURSOR_POSITION = 3
        const val INTERVAL_CURSOR_POSITION = 4
        const val CONTENT_CURSOR_POSITION = 5
        const val DONE_MARK_CURSOR_POSITION = 6
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = (
                "CREATE TABLE $TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$DATE_COLUMN DATE, " +
                        "$START_TIME_COLUMN  TEXT, " +
                        "$END_TIME_COLUMN  TEXT, " +
                        "$INTERVAL_COLUMN  INTEGER, " +
                        "$CONTENT_COLUMN  TEXT, " +
                        "$DONE_MARK_COLUMN  INTEGER );")
        db?.execSQL(createTableQuery)
        db?.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        //onCreate(db)
    }

    fun deleteEvent(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$ID_COLUMN=$id", null) > 0
        db.close()
        return result
    }

    fun addGame(note: Note) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(DATE_COLUMN, note.date)
        contentValues.put(START_TIME_COLUMN, note.start_time)
        contentValues.put(END_TIME_COLUMN, note.end_time)
        contentValues.put(INTERVAL_COLUMN, note.interval)
        contentValues.put(CONTENT_COLUMN, note.content)
        contentValues.put(DONE_MARK_COLUMN, note.done)

        val result = db.insert(TABLE_NAME, null, contentValues)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
        db.close()
    }

    fun updateDone(id: String, value: Boolean) {
        var bool=0
        if(value)
            bool=1
        val db = this.writableDatabase
//        val values = ContentValues()
//
//        values.put(DONE_MARK_COLUMN, value)
//        val result = db.update(TABLE_NAME, values, "$ID_COLUMN='$id'", null)
//        Log.e("aa", result.toString() + result.toString())
//        if (result == (-1)) {
//            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
//        }

        val query1 = "UPDATE $TABLE_NAME SET $DONE_MARK_COLUMN = $bool where $ID_COLUMN = '$id'"
//        val query  = "UPDATE $TABLE_NAME SET $DONE_MARK_COLUMN = $value WHERE $ID_COLUMN=$id"
        db.execSQL(query1);
        Log.e("aa", query1)
        db.close()

    }

    fun readAllData(data: String): ArrayList<Note> {
        val query = "Select * from $TABLE_NAME where $DATE_COLUMN='$data' ORDER BY $START_TIME_COLUMN, $CONTENT_COLUMN;"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToNotes(cursor)
        db.close()
        return result
    }

    fun readOneData(id: String): Note {
        val query = "Select * from $TABLE_NAME where $ID_COLUMN='$id';"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        val result = cursorToNote(cursor, id)
        db.close()
        return result
    }

    private fun cursorToNotes(cursor: Cursor?): ArrayList<Note> {
        val notes = ArrayList<Note>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val note = Note(
                    cursor.getString(ID_CURSOR_POSITION),
                    cursor.getString(DATE_CURSOR_POSITION),
                    cursor.getString(START_TIME_CURSOR_POSITION),
                    cursor.getString(END_TIME_CURSOR_POSITION),
                    cursor.getString(INTERVAL_CURSOR_POSITION).toInt(),
                    cursor.getString(CONTENT_CURSOR_POSITION),
                    strToBool(cursor.getString(DONE_MARK_CURSOR_POSITION))
                )
                notes.add(note)
            }
        }
        return notes
    }

    private fun strToBool(str:String):Boolean{
        var bool = false
        if(str=="1")
            bool=true
        return bool
    }

    private fun cursorToNote(cursor: Cursor?, id: String): Note {
        var note = Note()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == id) {
                    note = Note(
                        cursor.getString(ID_CURSOR_POSITION),
                        cursor.getString(DATE_CURSOR_POSITION),
                        cursor.getString(START_TIME_CURSOR_POSITION),
                        cursor.getString(END_TIME_CURSOR_POSITION),
                        cursor.getString(INTERVAL_CURSOR_POSITION).toInt(),
                        cursor.getString(CONTENT_CURSOR_POSITION),
                        strToBool(cursor.getString(DONE_MARK_CURSOR_POSITION))
                    )
                    break
                }
            }
        }
        return note
    }
}
