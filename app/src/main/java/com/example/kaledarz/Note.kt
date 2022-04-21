package com.example.kaledarz

class Note(
    var id: String?,
    var start_date: String?,
    var end_date: String?,
    var start_time: String?,
    var end_time: String?,
    var interval: Int?,
    var content: String?,
    var done: Boolean,
    var creation_date: String
) {
    constructor() : this("", "", "", "", "", 0, "", false, "")
}
