package com.example.kaledarz

class Note(
    var id: String?,
    var date: String?,
    var start_time: String?,
    var end_time: String?,
    var interval: Int?,
    var content: String?,
    var done: Boolean
) {
    constructor() : this("", "", "", "", 0, "", false)
}
