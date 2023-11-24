package com.example.kaledarz.DTO

data class DateFilter(
    var lowerStartDate: String = "",
    var upperStartDate: String = "",
    var lowerEndDate: String = "",
    var upperEndDate: String = "",
    var content: String = ""
)