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
        const val START_DATE_COLUMN = "start_date"
        const val END_DATE_COLUMN = "end_date"
        const val START_TIME_COLUMN = "start_time"
        const val END_TIME_COLUMN = "end_time"
        const val INTERVAL_COLUMN = "interval"
        const val CONTENT_COLUMN = "content"
        const val DONE_MARK_COLUMN = "done_mark"
        const val CREATION_DATE = "creation_date"

        const val ID_CURSOR_POSITION = 0
        const val START_DATE_CURSOR_POSITION = 1
        const val END_DATE_CURSOR_POSITION = 2
        const val START_TIME_CURSOR_POSITION = 3
        const val END_TIME_CURSOR_POSITION = 4
        const val INTERVAL_CURSOR_POSITION = 5
        const val CONTENT_CURSOR_POSITION = 6
        const val DONE_MARK_CURSOR_POSITION = 7
        const val CREATION_DATE_CURSOR_POSITION = 8
    }

    var dateFormatHelper = DateFormatHelper()

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = (
                "CREATE TABLE $TABLE_NAME ( " +
                        "$ID_COLUMN  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "$START_DATE_COLUMN DATE, " +
                        "$END_DATE_COLUMN DATE, " +
                        "$START_TIME_COLUMN  TEXT, " +
                        "$END_TIME_COLUMN  TEXT, " +
                        "$INTERVAL_COLUMN  INTEGER, " +
                        "$CONTENT_COLUMN  TEXT, " +
                        "$DONE_MARK_COLUMN  INTEGER," +
                        "$CREATION_DATE DATE );")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun deleteEvent(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$ID_COLUMN=$id", null) > 0
        return result
    }

    fun addGame(note: Note) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(START_DATE_COLUMN, note.start_date)
        contentValues.put(END_DATE_COLUMN, note.end_date)
        contentValues.put(START_TIME_COLUMN, note.start_time)
        contentValues.put(END_TIME_COLUMN, note.end_time)
        contentValues.put(INTERVAL_COLUMN, note.interval)
        contentValues.put(CONTENT_COLUMN, note.content)
        contentValues.put(DONE_MARK_COLUMN, note.done)
        contentValues.put(CREATION_DATE, dateFormatHelper.getCurrentDateTimeForDatabase())

        val result = db.insert(TABLE_NAME, null, contentValues)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDone(id: String, value: Boolean) {
        var bool = 0
        if (value)
            bool = 1
        val db = this.writableDatabase
        val query1 = "UPDATE $TABLE_NAME SET $DONE_MARK_COLUMN = $bool where $ID_COLUMN = '$id'"
        db.execSQL(query1)
        Log.e("aa", query1)
    }

    fun readAllDataByDate(data: String): ArrayList<Note> {
        val query =
            "Select * from $TABLE_NAME where $START_DATE_COLUMN='$data' ORDER BY $START_TIME_COLUMN, $CONTENT_COLUMN;"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNotes(cursor)
    }

    fun readAllData(): ArrayList<Note> {
        val query = "Select * from $TABLE_NAME ORDER BY $START_TIME_COLUMN, $CONTENT_COLUMN;"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNotes(cursor)
    }

    fun readOneData(id: String): Note {
        val query = "Select * from $TABLE_NAME where $ID_COLUMN='$id';"
        Log.e("query", query)
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNote(cursor, id)
    }

    fun readLastRow(): Note {
        val query = "Select * from $TABLE_NAME order by $CREATION_DATE LIMIT 1;"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        if (db != null) {
            cursor = db.rawQuery(query, null)
        }
        return cursorToNotes(cursor)[0]
    }

    private fun cursorToNotes(cursor: Cursor?): ArrayList<Note> {
        val notes = ArrayList<Note>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                notes.add(getNoteFromCursor(cursor))
            }
        }
        return notes
    }

    private fun cursorToNote(cursor: Cursor?, id: String): Note {
        var note = Note()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == id) {
                    note = getNoteFromCursor(cursor)
                    break
                }
            }
        }
        return note
    }

    private fun strToBool(str: String): Boolean {
        var bool = false
        if (str == "1")
            bool = true
        return bool
    }

    private fun getNoteFromCursor(cursor: Cursor): Note {
        return Note(
            cursor.getString(ID_CURSOR_POSITION),
            cursor.getString(START_DATE_CURSOR_POSITION),
            cursor.getString(END_DATE_CURSOR_POSITION),
            cursor.getString(START_TIME_CURSOR_POSITION),
            cursor.getString(END_TIME_CURSOR_POSITION),
            cursor.getString(INTERVAL_CURSOR_POSITION).toInt(),
            cursor.getString(CONTENT_CURSOR_POSITION),
            strToBool(cursor.getString(DONE_MARK_CURSOR_POSITION)),
            cursor.getString(CREATION_DATE_CURSOR_POSITION),
            Status.UNDONE
        )
    }
}

