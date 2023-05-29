package com.subhamgupta.httpserver.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.subhamgupta.httpserver.utils.Channel.internalScope
import com.subhamgupta.httpserver.utils.Channel.sharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object Channel {
    var sharedFlow = MutableSharedFlow<ChannelEvent<Any>>()
    internal val internalScope = ChannelScope()
}

fun sendEvent(event: Any) = internalScope.launch {
    sharedFlow.emit(ChannelEvent(event))
}

inline fun <reified T> LifecycleOwner.receiveEvent(
    lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
    noinline block: suspend CoroutineScope.(event: T) -> Unit
): Job {
    return ChannelScope(this, lifeEvent).launch {
        sharedFlow.collect {
            if (it.event is T) {
                block(it.event)
            }
        }
    }
}

inline fun <reified T> LifecycleOwner.receiveEvent(): Flow<T> {
    return sharedFlow.filter { it.event is T }
        .map { it.event as T }
}

inline fun <reified T> LifecycleOwner.receiveEventLive(
    lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_START,
    noinline block: suspend CoroutineScope.(event: T) -> Unit
): Job {
    return lifecycleScope.launch {
        sharedFlow.flowWithLifecycle(lifecycle, lifeEvent.targetState).collect {
            if (it.event is T) {
                block(it.event)
            }
        }
    }
}

inline fun <reified T> receiveEventHandler(
    noinline block: suspend CoroutineScope.(event: T) -> Unit
): Job {
    return ChannelScope().launch {
        sharedFlow.collect {
            if (it.event is T) {
                block(it.event)
            }
        }
    }
}
