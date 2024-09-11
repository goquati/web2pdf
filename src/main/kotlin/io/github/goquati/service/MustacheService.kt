package io.github.goquati.service

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.FileReader
import java.io.StringWriter

@Service
class MustacheService(
    private val environmentService: EnvironmentService,
) {
    private val mustacheFactory = DefaultMustacheFactory()

    suspend fun load(path: String): Mustache = withContext(Dispatchers.IO) {
        if (environmentService.isDevMode)
            mustacheFactory.compile(FileReader("src/main/resources/$path"), path)
        else
            mustacheFactory.compile(path)
    }

    suspend fun process(path: String, data: Any): String = withContext(Dispatchers.IO) {
        val writer = StringWriter() // TODO use OutputStream
        val mustache = load(path)
        mustache.execute(writer, data).flush()
        writer.toString()
    }
}