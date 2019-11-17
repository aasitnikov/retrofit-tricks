package ru.improvegroup.handleerror

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import ru.improvegroup.model.User

interface ApiService {

    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Int): Single<User>
}