package ru.improvegroup.unwrapenvelope

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.improvegroup.model.User

interface ApiService {

    @GET("user/{userId}")
    fun getUser(@Path("userId") userId: Int): Single<User>

    @POST("user")
    fun postUser(@Body user: User): Completable
}