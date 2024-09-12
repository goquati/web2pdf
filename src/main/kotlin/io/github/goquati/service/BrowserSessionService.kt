package io.github.goquati.service

import io.github.goquati.LoggerDelegate
import io.github.goquati.Web2PdfException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.runBlocking
import org.hildan.chrome.devtools.protocol.ChromeDPClient
import org.hildan.chrome.devtools.protocol.RequestNotSentException
import org.hildan.chrome.devtools.sessions.BrowserSession
import org.hildan.chrome.devtools.sessions.PageSession
import org.hildan.chrome.devtools.sessions.newPage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.ConnectException
import kotlin.time.Duration.Companion.seconds

@Service
class BrowserSessionService {
    private suspend fun browserSessionBuilder() =
        ChromeDPClient("http://localhost:9222").webSocket()

    private suspend fun initBrowserSession(): BrowserSession {
        for (i in 1..20) {
            try {
                return browserSessionBuilder().also {
                    log.info("try to connect to chrome...connected")
                }
            } catch (_: ConnectException) {
                log.info("try to connect to chrome...failed")
            }
            delay(1.seconds)
        }
        throw ConnectException("cannot connect to chrome")
    }

    private val browserSession =
        MutableStateFlow(0L to runBlocking { initBrowserSession() })

    private suspend fun createNewBrowserSession(latestId: Long) =
        browserSession.updateAndGet { (currentId, currentSession) ->
            if (latestId < currentId) currentId to currentSession
            else (currentId + 1) to browserSessionBuilder()
        }.second

    suspend fun getPageSession(): PageSession {
        val (sessionId, session) = browserSession.value
        return try {
            session.newPage()
        } catch (e: RequestNotSentException) {
            try {
                createNewBrowserSession(sessionId).newPage().also {
                    log.info("new websocket connection to browser created")
                }
            } catch (e: Exception) {
                log.error("cannot connect to chromium, restart server!")
                throw Web2PdfException("Internal Server Error", status = HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    companion object {
        private val log by LoggerDelegate()
    }
}