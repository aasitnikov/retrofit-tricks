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

class CoroutineDecoratorConverterTest {
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
            addConverterFactory(CoroutineDecoratorConverter())
            addConverterFactory(GsonConverterFactory.create())
        }

        webServer.enqueue(MockResponse().setBody(SUCCESS_JSON))
        webServer.enqueue(MockResponse().setBody(ERROR_JSON))

        println(apiService.getUser(userId = 1))
        try {
            println(apiService.getUser(userId = 1))
        } catch (error: ErrorDto) {
            println(error)
        }
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