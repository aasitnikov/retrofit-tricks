package ru.improvegroup.model

import com.google.gson.annotations.SerializedName

data class ErrorDto(
    @SerializedName("errorCode") val errorCode: String,
    @SerializedName("errorMessage") val errorMessage: String
) : Throwable()