package ru.improvegroup.coroutine

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.converter.gson.GsonConverterFactory
import ru.improvegroup.handleerror.PORT
import ru.improvegroup.model.ErrorDto
import ru.improvegroup.utils.retrofit

class CoroutineDecoratorCallAdapterTest {
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
    fun test() = runBlocking {
        val apiService: ApiService = retrofit("http://localhost:$PORT") {
            addCallAdapterFactory(CoroutineDecoratorCallAdapter())
            addConverterFactory(GsonConverterFactory.create())
        }

        webServer.enqueue(MockResponse().setBody(SUCCESS_JSON_2))
        webServer.enqueue(MockResponse().setBody(ERROR_JSON_2).setResponseCode(400))

        println(apiService.getUser(userId = 1))
        try {
            println(apiService.getUser(userId = 1))
        } catch (error: ErrorDto) {
            println(error)
        }
    }
}

const val ERROR_JSON_2 = """
{
        "errorCode":"NON_AUTHORIZED", 
        "errorMessage": "No auth token"
}"""


const val SUCCESS_JSON_2 = """{"userName":"Johnny"}"""