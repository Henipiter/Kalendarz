package com.example.kaledarz

class Note(
    var id: String?,
    var date: String?,
    var time: String?,
    var interval: Int?,
    var content: String?,
    var done: Boolean?
) {
    constructor() : this("", "", "", 0, "", true)
}
