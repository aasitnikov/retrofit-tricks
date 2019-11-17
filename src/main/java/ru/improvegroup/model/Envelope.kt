package ru.improvegroup.model

import com.google.gson.annotations.SerializedName

data class Envelope<T>(
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: ErrorDto?
) {
    companion object {
        fun <T> of(any: T) = Envelope(any, null)
    }
}