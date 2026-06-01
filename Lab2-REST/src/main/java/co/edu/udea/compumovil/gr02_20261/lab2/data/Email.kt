package co.edu.udea.compumovil.gr02_20261.lab2.data

import com.google.gson.annotations.SerializedName

data class Email(
    @SerializedName("id") val id: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("body") val body: String,
    @SerializedName("date") val date: String,
    @SerializedName("isRead") val isRead: Boolean
)