package ru.improvegroup.unwrapenvelope

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.CallAdapter
import retrofit2.Retrofit
import ru.improvegroup.model.Envelope
import ru.improvegroup.utils.captureType
import ru.improvegroup.utils.decorate
import ru.improvegroup.utils.wrapInto
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * This call adapter wraps responseType with [Envelope] and then discards it in [unwrapEnvelopeSingle].
 *
 * For sample usage take a look at `UnwrapEnvelopeDecoratorTest`
 */
class UnwrapEnvelopeDecorator : CallAdapter.Factory() {

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
    ): CallAdapter<Any, Single<*>> {
        val singleCallAdapter = retrofit.nextCallAdapter(this, returnType, annotations)
                as CallAdapter<Any, Single<Envelope<*>>>

        return singleCallAdapter.decorate(
            // Tell gson to parse Envelope instead of raw type
            transformType = { it.wrapInto(Envelope::class.java) },
            // Remove Envelope, as ApiService declared without it
            transformAdapted = { it.unwrapEnvelopeSingle() }
        )
    }

    // Main logic to process envelope and discard it
    private fun Single<Envelope<*>>.unwrapEnvelopeSingle(): Single<*> {
        return flatMap {
            if (it.error != null) {
                Single.error(it.error)
            } else {
                Single.just(it.data)
            }
        }
    }

    private fun completable(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, Completable> {
        // We don't care for returnType, but we need Single to get Envelope in case of error != null
        val newReturnType = captureType<Single<Envelope<Nothing>>>()

        val completableCallAdapter = retrofit.nextCallAdapter(this, newReturnType, annotations)
                as CallAdapter<Any, Single<Envelope<*>>>

        return completableCallAdapter.decorate { single ->
            single.unwrapEnvelopeCompletable()
        }
    }

    private fun Single<Envelope<*>>.unwrapEnvelopeCompletable(): Completable {
        return flatMapCompletable {
            if (it.error != null) {
                Completable.error(it.error)
            } else {
                Completable.complete()
            }
        }
    }

    // Other way is to edit returnType, RxJava2CallAdapterFactory will extract enveloped responseType itself
    private fun single2(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<Any, Single<*>> {
        val newReturnType = returnType.wrapParameterWithEnvelope()
        val nextCallAdapter = retrofit.nextCallAdapter(this, newReturnType, annotations)
                as CallAdapter<Any, Single<Envelope<*>>>

        return nextCallAdapter.decorate {
            it.unwrapEnvelopeSingle()
        }
    }

    private fun Type.wrapParameterWithEnvelope(): Type {
        val rawType = getRawType(this)
        val parameter = getParameterUpperBound(0, this as ParameterizedType)
        val enveloped = parameter.wrapInto(Envelope::class.java)
        return enveloped.wrapInto(rawType)

        // $receiver = Single<User>
        // rawType   = Single
        // parameter = User
        // enveloped = Envelope<User>
        // return    = Single<Envelope<User>>
    }
}

