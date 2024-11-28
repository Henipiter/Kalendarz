package com.example.kaledarz.DTO

import com.example.kaledarz.helpers.DateFormatHelper

class Note(
    var id: String?,
    var startDate: String,
    var endDate: String,
    var startTime: String,
    var endTime: String,
    var content: String?,
    var done: Boolean,
    var cyclic: Boolean,
    var status: Status
) {

    fun export(): String {
        return "$startDate`$endDate`$startTime`$endTime`$content`$done`$cyclic`$status``\n``"
    }

    constructor() : this("", "", "", "", "", "", false, false, Status.UNDONE)


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
                note.endDate + " " + note.endTime + ":00"
            )
        }

        private fun checkStatusFuture(note: Note): Boolean {
            return DateFormatHelper.isFirstDateGreaterThanSecond(
                note.startDate + " " + note.startTime + ":00",
                DateFormatHelper.getCurrentDateTime()
            )
        }
    }
}
