package ru.improvegroup.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("userName") val name: String
)