package io.github.goquati.service

import io.ktor.http.*
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class EnvironmentService(
    private val environment: Environment,
) {
    private val host get() = if (isDevMode) "host.docker.internal" else "localhost"

    val isDevMode get() = environment.activeProfiles.contains("dev")

    fun getLocalServerUrl(path: String) = Url("http://$host:8080/$path")
}