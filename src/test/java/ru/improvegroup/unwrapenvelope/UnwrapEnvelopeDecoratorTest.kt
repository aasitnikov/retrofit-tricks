package ru.improvegroup.unwrapenvelope

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.improvegroup.model.User
import ru.improvegroup.utils.retrofit

class UnwrapEnvelopeDecoratorTest {

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
            addCallAdapterFactory(UnwrapEnvelopeDecorator())
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
        }

        webServer.enqueue(MockResponse().setBody(SUCCESS_JSON))
        webServer.enqueue(MockResponse().setBody(ERROR_JSON))

        println(apiService.getUser(userId = 1).blockingGet())
        println(apiService.postUser(User(name = "Strygwyr")).blockingGet())
    }
}


const val PORT = 32957
const val ERROR_JSON = """
{
    "error":{
        "errorCode":"NON_AUTHORIZED", 
        "errorMessage": "No auth token"
    }
}"""

const val SUCCESS_JSON = """{"data":{"userName":"Johnny"}}"""
