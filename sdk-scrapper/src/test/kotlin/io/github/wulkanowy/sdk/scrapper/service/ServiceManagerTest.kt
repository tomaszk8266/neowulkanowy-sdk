package io.github.wulkanowy.sdk.scrapper.service

import io.github.wulkanowy.sdk.scrapper.BaseLocalTest
import io.github.wulkanowy.sdk.scrapper.OkHttpClientBuilderFactory
import io.github.wulkanowy.sdk.scrapper.Scrapper
import io.github.wulkanowy.sdk.scrapper.ScrapperException
import io.github.wulkanowy.sdk.scrapper.interceptor.ErrorInterceptorTest
import io.github.wulkanowy.sdk.scrapper.login.LoginTest
import io.github.wulkanowy.sdk.scrapper.notes.NotesTest
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.net.URL

class ServiceManagerTest : BaseLocalTest() {

    @Test
    fun interceptorTest() {
        val manager = ServiceManager(OkHttpClientBuilderFactory(), HttpLoggingInterceptor.Level.NONE,
            Scrapper.LoginType.STANDARD, "http", "fakelog.localhost:3000", "default", "email", "password",
            "schoolSymbol", 123, 101, 2019, false, "", ""
        )
        manager.setInterceptor({
            throw ScrapperException("Test")
        })

        try {
            runBlocking { manager.getStudentService().getNotes() }
        } catch (e: Throwable) {
            assertTrue(e is ScrapperException)
        }
    }

    @Test
    fun interceptorTest_prepend() {
        server.enqueue(MockResponse().setBody(NotesTest::class.java.getResource("UwagiIOsiagniecia.json").readText()))
        server.start(3000)
        val manager = ServiceManager(OkHttpClientBuilderFactory(), HttpLoggingInterceptor.Level.NONE,
            Scrapper.LoginType.STANDARD, "http", "fakelog.localhost:3000", "default", "email", "password",
            "schoolSymbol", 123, 101, 2019, false, "", ""
        )
        manager.setInterceptor({
            // throw IOException("Test")
            it.proceed(it.request())
        })
        manager.setInterceptor({
            throw ScrapperException("Test")
        }, false)

        try {
            runBlocking { manager.getStudentService().getNotes() }
        } catch (e: Throwable) {
            assertTrue(e is ScrapperException)
        }
    }

    @Test
    fun apiNormalizedSymbol_blank() {
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("LoginPage-standard.html").readText().replace("fakelog.cf", "fakelog.localhost:3000")))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-uonet.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(ErrorInterceptorTest::class.java.getResource("Offline.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.enqueue(MockResponse().setBody(LoginTest::class.java.getResource("Logowanie-brak-dostepu.html").readText()))
        server.start(3000)

        val api = Scrapper().apply {
            logLevel = HttpLoggingInterceptor.Level.BASIC
            ssl = false
            host = "fakelog.localhost:3000"
            email = "jan@fakelog.cf"
            password = "jan123"
            symbol = ""
        }

        try {
            runBlocking { api.getStudents() }
        } catch (e: Throwable) {
            assertTrue(e is ScrapperException)
        }

        server.takeRequest()
        // /Default/Account/LogOn <– default symbol set
        assertEquals("/Default/Account/LogOn", URL(server.takeRequest().requestUrl.toString()).path)
    }

    @Test
    fun autoLoginInterceptor() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.start(3000)
        val manager = ServiceManager(OkHttpClientBuilderFactory(), HttpLoggingInterceptor.Level.NONE,
            Scrapper.LoginType.STANDARD, "http", "fakelog.localhost:3000", "default", "email", "password",
            "schoolSymbol", 123, 101, 2019, true, "", ""
        )

        val res = runCatching {
            runBlocking { manager.getStudentService().getNotes() }
        }

        val exception = res.exceptionOrNull()!!

        assertEquals("HTTP 503 Server Error", exception.message)
        assertEquals(HttpException::class.java, exception::class.java)
    }
}
