package ru.improvegroup.helpersample

import retrofit2.CallAdapter
import retrofit2.Retrofit
import ru.improvegroup.utils.captureType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun main() {
    val factory = object : CallAdapter.Factory() {
        override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
        ): CallAdapter<*, *>? {
            throw NotImplementedError()
        }

        // Demonstration of static helper methods
        fun example() {
            val type = captureType<Map<Int, List<String>>>() as ParameterizedType
            // Map<Integer, ? extends List<? extends String>>

            val mapType = getRawType(type)
            // interface Map

            val intType = getParameterUpperBound(0, type)
            // class Integer

            val listOfStringType = getParameterUpperBound(1, type) as ParameterizedType
            // List<? extends String>

            val stringType = getParameterUpperBound(0, listOfStringType)
            // class String

            println("""
                Map<Int, List<String> = $type
                                  Map = $mapType
                                  Int = $intType
                         List<String> = $listOfStringType
                               String = $stringType
            """.trimIndent())


        }
    }
    factory.example()
}