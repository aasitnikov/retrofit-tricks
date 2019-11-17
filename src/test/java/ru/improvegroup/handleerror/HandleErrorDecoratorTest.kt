package ru.improvegroup.handleerror

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.improvegroup.utils.retrofit

class HandleErrorDecoratorTest {

    private val webServer = MockWebServer()

    @Before
    fun before() {
        webServer.start(PORT)
    }

    @After
    fun after() {
        webServer.shutdown()
    }

    @Test
    fun test() {
        val apiService: ApiService = retrofit("http://localhost:$PORT") {
            addCallAdapterFactory(HandleErrorDecorator())
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
        }

        webServer.enqueue(MockResponse().setBody(SUCCESS_JSON))
        webServer.enqueue(MockResponse().setBody(ERROR_JSON).setResponseCode(400))

        println(apiService.getUser(1).blockingGet())
        println(apiService.getUser(1).ignoreElement().blockingGet())
    }
}

const val PORT = 32957
const val ERROR_JSON = """
{
    "errorCode":"NON_AUTHORIZED", 
    "errorMessage": "No auth token"
}"""

const val SUCCESS_JSON = """{"userName":"Johnny"}"""
