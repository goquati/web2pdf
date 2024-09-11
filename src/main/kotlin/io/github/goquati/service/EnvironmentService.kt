package io.github.goquati.service

import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class EnvironmentService(
    private val environment: Environment,
) {
    private val host get() = if (isDevMode) "host.docker.internal" else "localhost"

    val isDevMode get() = environment.activeProfiles.contains("dev")

    fun getLocalServerUrl(path: String) = "http://$host:8080/$path"
}