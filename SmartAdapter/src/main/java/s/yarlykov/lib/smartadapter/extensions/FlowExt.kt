package s.yarlykov.lib.smartadapter.extensions

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.plus

val defaultExceptionHandler = CoroutineExceptionHandler { _, _ -> }

fun appMainScope(): CoroutineScope = MainScope() + defaultExceptionHandler

fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {

    return flow {
        var lastTime = 0L
        this@throttleFirst.collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}