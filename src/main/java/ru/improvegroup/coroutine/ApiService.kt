package ru.improvegroup.coroutine

import retrofit2.http.GET
import retrofit2.http.Path
import ru.improvegroup.model.User

interface ApiService {

    @GET("user/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): User
}