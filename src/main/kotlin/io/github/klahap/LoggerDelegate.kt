package io.github.klahap

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.time.measureTimedValue

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, Logger> {
    override fun getValue(thisRef: R, property: KProperty<*>): Logger {
        val clazz = thisRef.javaClass.enclosingClass
            ?.takeIf { it.kotlin.companionObject?.java == thisRef.javaClass }
            ?: thisRef.javaClass
        return LoggerFactory.getLogger(clazz)
    }

    companion object {
        suspend inline fun <reified T> Logger.measureExecutionTime(
            message: String,
            crossinline f: suspend () -> T,
        ): T {
            val result = measureTimedValue { f() }
            info("$message in ${result.duration}")
            return result.value
        }
    }
}