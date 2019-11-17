package ru.improvegroup.coroutine

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import ru.improvegroup.model.Envelope
import ru.improvegroup.utils.decorate
import ru.improvegroup.utils.wrapInto
import java.lang.reflect.Type

/**
 * If all responses are 200 we can use Converter to throw exceptions from error field
 *
 * For sample usage take a look at `CoroutineDecoratorConverterTest`
 */
class CoroutineDecoratorConverter : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {

        val newType = type.wrapInto(Envelope::class.java)
        val nextConverter = retrofit.nextResponseBodyConverter<Any>(this, newType, annotations)
                as Converter<ResponseBody, Envelope<*>>

        return nextConverter.decorate {
            if (it?.error != null) {
                throw it.error
            } else {
                it?.data
            }
        }
    }
}

