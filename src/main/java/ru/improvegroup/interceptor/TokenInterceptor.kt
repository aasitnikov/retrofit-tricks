package ru.improvegroup.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.*
import javax.inject.Inject

// You can inject interceptor as any regular class
class TokenInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader("AccessToken", sessionRepository.accessToken)
            .build()
        return chain.proceed(newRequest)
    }
}

class NotDiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader("AccessToken", UUID.randomUUID().toString())
            .build()
        return chain.proceed(newRequest)
    }
}

interface SessionRepository {
    val accessToken: String
}