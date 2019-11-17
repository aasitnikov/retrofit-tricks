package ru.improvegroup.handleerror

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ru.improvegroup.model.ErrorDto
import ru.improvegroup.utils.decorate
import ru.improvegroup.utils.responseBodyConverter
import java.lang.reflect.Type

/**
 * This call adapter transforms error with [transformError] if HttpException thrown.
 * Note: next call adapter factory should be [RxJava2CallAdapterFactory].
 *
 * For sample usage take a look at `HandleErrorDecoratorTest`
 */
class HandleErrorDecorator : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        // Return null if you want to skip this adapter for current call - check annotations or returnType
        return when (getRawType(returnType)) {
            Single::class.java -> single(returnType, annotations, retrofit)
            Completable::class.java -> completable(returnType, annotations, retrofit)
            // Add other types such as Observable and Maybe, if you need them
            else -> null
        }
    }

    private fun single(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, Single<*>> {
        val singleCallAdapter = retrofit.nextCallAdapter(this, returnType, annotations)
                as CallAdapter<Any, Single<*>>
        return singleCallAdapter.decorate { single ->
            single.handleHttpError(retrofit)
        }
    }

    private fun completable(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, Completable> {
        val completableCallAdapter = retrofit.nextCallAdapter(this, returnType, annotations)
                as CallAdapter<Any, Completable>
        return completableCallAdapter.decorate { completable ->
            completable.handleHttpError(retrofit)
        }
    }

    private fun <T> Single<T>.handleHttpError(retrofit: Retrofit): Single<T> {
        return onErrorResumeNext { throwable ->
            Single.error(transformError(throwable, retrofit))
        }
    }

    private fun Completable.handleHttpError(retrofit: Retrofit): Completable {
        return onErrorResumeNext { throwable ->
            Completable.error(transformError(throwable, retrofit))
        }
    }

    private fun transformError(ex: Throwable, retrofit: Retrofit): Throwable {
        // HttpException occurs if there was non-200 response
        if (ex !is HttpException) return ex
        val errorBody: ResponseBody = ex.response()!!.errorBody()!!
        // ErrorDto is returned by server in non-200 response
        val converter = retrofit.responseBodyConverter(ErrorDto::class.java)
        val convert: ErrorDto = converter.convert(errorBody)!!
        return convert
    }
}