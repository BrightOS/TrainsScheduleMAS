package ru.bashcony.agents

import kotlinx.coroutines.*
import ru.bashcony.api.EventResponse
import ru.bashcony.api.RestAPI
import ru.bashcony.common.currentDateTime
import ru.bashcony.common.toDateTime
import ru.bashcony.events.BaseEvent
import java.util.concurrent.TimeUnit

abstract class BaseAgent(
    val agentId: Int,
    val agentType: String,
) {
    private val myEvents = mutableListOf<Int>()
    private var lastEventTime = currentDateTime()

    abstract fun handleBroadcast(event: EventResponse, fromMyEvents: Boolean)

    init {
        println("---\nInitialized agent.")
    }

    fun run() {
        println("---\nCoroutine context created.")
        println("---\nStarting to listen to server.")
        while (true) {
            val response = RestAPI.getEvents()
            response
                ?.filter { it.creationTime.toDateTime() > lastEventTime && it.id !in myEvents }
                ?.let {
                    if (it.isNotEmpty()) {
                        println("---\nNew ${it.size} events:")
                        it.map { println("${it.type} ${it.data}") }
                    }
                    it
                }
                ?.forEach {
//                    println("(!!!) ${it.id} ${it.data[HANDLED_EVENT_ID]?.toIntOrNull()} $myEvents")
                    handleBroadcast(
                        event = it,
                        fromMyEvents = (it.data[HANDLED_EVENT_ID]?.toIntOrNull() ?: -1) in myEvents,
                    )
                    lastEventTime = it.creationTime.toDateTime()
                }

            Thread.sleep(200)
        }
    }

    val agentName: String
        get() = "${agentType}_$agentId"

    fun sendEvent(event: BaseEvent, changeTime: Boolean = false) {
        println("---\nSending event: ${event::class.simpleName}")
        event.apply {
            eventData = eventData.plus(HANDLED_EVENT_ID to handledEventId.toString()).let {
                println(it)
                it
            }
        }

        val response = RestAPI.sendEvent(
            BaseEvent(
                eventType = event.eventType,
                eventData = event.data.plus(HANDLED_EVENT_ID to event.handledEventId.toString()),
                baseAgent = event.baseAgent,
                handledEventId = event.handledEventId,
            )
        )
        if (response == null) return

        val returnedEventId = response.id
        if (changeTime)
            lastEventTime = response.creationTime.toDateTime()
        myEvents.add(returnedEventId)
    }

    companion object {
        const val HANDLED_EVENT_ID = "handled_event_id"
    }
}
