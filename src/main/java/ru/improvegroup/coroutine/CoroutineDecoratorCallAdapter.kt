package ru.improvegroup.coroutine

import okhttp3.Request
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import ru.improvegroup.model.ErrorDto
import ru.improvegroup.utils.decorate
import ru.improvegroup.utils.doOnResponse
import ru.improvegroup.utils.responseBodyConverter
import java.lang.reflect.Type


class CoroutineDecoratorCallAdapter : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val nextCallAdapter = retrofit.nextCallAdapter(this, returnType, annotations)
                as CallAdapter<Any, Call<Any>>

        return nextCallAdapter.decorate { originalCall ->
            originalCall.doOnResponse { callback, call, response ->
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    val converter = retrofit.responseBodyConverter(ErrorDto::class.java)
                    val error = converter.convert(response.errorBody()!!)!!
                    callback.onFailure(call, error)
                }
            }
        }

        // Verbose way
        return nextCallAdapter.decorate { originalCall ->
            object : Call<Any> {
                private val adaptedCall = this

                override fun enqueue(callback: Callback<Any>) {
                    originalCall.enqueue(object : Callback<Any> {
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.isSuccessful) {
                                callback.onResponse(adaptedCall, response)
                            } else {
                                val converter = retrofit.responseBodyConverter(ErrorDto::class.java)
                                val error = converter.convert(response.errorBody()!!)!!
                                callback.onFailure(adaptedCall, error)
                            }
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            callback.onFailure(adaptedCall, t)
                        }
                    })
                }

                override fun isExecuted(): Boolean = originalCall.isExecuted
                override fun clone(): Call<Any> = throw UnsupportedOperationException()
                override fun isCanceled(): Boolean = originalCall.isCanceled
                override fun cancel() = originalCall.cancel()
                override fun execute(): Response<Any> = throw UnsupportedOperationException()
                override fun request(): Request = originalCall.request()
            }
        }
    }
}
