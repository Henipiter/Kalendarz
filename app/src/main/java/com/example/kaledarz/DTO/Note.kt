package com.example.kaledarz.DTO

import com.example.kaledarz.helpers.DateFormatHelper

class Note(
    var id: String?,
    var start_date: String?,
    var end_date: String?,
    var start_time: String?,
    var end_time: String?,
    var interval: Int?,
    var content: String?,
    var done: Boolean,
    var creation_date: String,
    var status: Status
) {
    constructor() : this("", "", "", "", "", 0, "", false, "", Status.UNDONE)


    companion object {
        fun computeStatusForNoteList(noteList: ArrayList<Note>) {
            for (note in noteList) {
                getRightStatusImage(note)
            }
        }

        private fun getRightStatusImage(note: Note) {

            if (note.done) {
                note.status = Status.DONE
            } else {
                checkStatusIfNoteNotDone(note)
            }
        }

        private fun checkStatusIfNoteNotDone(note: Note) {
            when {
                checkStatusPast(note) -> {
                    note.status = Status.PAST
                }
                checkStatusFuture(note) -> {
                    note.status = Status.FUTURE
                }
                else -> {
                    note.status = Status.UNDONE
                }
            }
        }

        private fun checkStatusPast(note: Note): Boolean {
            return DateFormatHelper.isFirstDateGreaterThanSecond(
                DateFormatHelper.getCurrentDateTime(),
                note.end_date + " " + note.end_time + ":00"
            )
        }

        private fun checkStatusFuture(note: Note): Boolean {
            return DateFormatHelper.isFirstDateGreaterThanSecond(
                note.start_date + " " + note.start_time + ":00",
                DateFormatHelper.getCurrentDateTime()
            )
        }
    }
}
