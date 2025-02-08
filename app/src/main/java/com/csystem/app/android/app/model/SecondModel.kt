package com.csystem.app.android.app.model

data class SecondModel (val seconds: Long,
val dateTime: String
) {
    override fun toString(): String {
        val hour = seconds / 60 / 60
        val minute = seconds / 60 % 60
        val second = seconds % 60
        return "$hour:$minute:$second $dateTime".format(hour, minute, second, dateTime)
    }
}