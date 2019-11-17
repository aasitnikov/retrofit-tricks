package ru.improvegroup.utils

import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import java.lang.reflect.Type
import kotlin.reflect.typeOf

/**
 * Returns new CallAdapter that applies [transformAdapted] and [transformType] to result of
 * receiver CallAdapter adapt() and responseType() methods respectively
 */
inline fun <In, Out> CallAdapter<Any, In>.decorate(
    crossinline transformType: (Type) -> Type = { it },
    crossinline transformAdapted: (future: In) -> Out
): CallAdapter<Any, Out> {
    return object : CallAdapter<Any, Out> {
        override fun adapt(call: Call<Any>): Out {
            return transformAdapted(this@decorate.adapt(call))
        }

        override fun responseType(): Type {
            return transformType(this@decorate.responseType())
        }
    }
}

/**
 * Returns new Converter that composes [transform] with result of the given Converter.
 */
inline fun <F, T, U> Converter<F, T>.decorate(
    crossinline transform: (T?) -> U
): Converter<F, U> {
    return Converter { transform(convert(it)) }
}

/**
 * Syntactic sugar for Retrofit.Builder
 */
inline fun <reified T> retrofit(
    baseUrl: String,
    setup: Retrofit.Builder.() -> Unit = {}
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .apply(setup)
        .build()
        .create()
}

/**
 * Syntactic sugar for getting typed converter with class instead of type
 */
inline fun <T> Retrofit.responseBodyConverter(clazz: Class<T>): Converter<ResponseBody, T> {
    return responseBodyConverter<T>(clazz, arrayOf())
}

/**
 * Syntactic sugar for capturing reified type
 */
inline fun <reified T> captureType(): Type {
    return object : TypeToken<T>() {}.type
}

/**
 * Syntactic sugar for getting parametrized type. For example: `String.wrapInto(Single) = Single<String>`
 */
// String.wrapInto(Single) = Single<String>
fun Type.wrapInto(type: Type): Type {
    return TypeToken.getParameterized(type, this).type
}

/**
 * Transforms call to intercept response with passed [onResponse]
 */
inline fun Call<Any>.doOnResponse(
    crossinline onResponse: (Callback<Any>, Call<Any>, Response<Any>) -> Unit
): Call<Any> {
    val originalCall = this
    return object : Call<Any> by originalCall {

        inline val decoratedCall get() = this

        override fun enqueue(callback: Callback<Any>) {
            originalCall.enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    callback.onFailure(decoratedCall, t)
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    onResponse(callback, decoratedCall, response)
                }
            })
        }

        override fun clone(): Call<Any> = throw UnsupportedOperationException()
        override fun execute(): Response<Any> = throw UnsupportedOperationException()
    }
}