package ru.bashcony.events

import ru.bashcony.agents.LocomotiveAgent
import kotlin.math.max

sealed class LocomotiveEvent(
    eventType: String,
    eventData: Map<String, String>,
    locomotiveAgent: LocomotiveAgent,
    handledEventId: Int? = null,
) : BaseEvent(eventType, eventData, locomotiveAgent, handledEventId) {

    class Appeared(
        locomotiveId: String,
        appearanceTime: Int,
        maxWeight: Int,
        locomotiveAgent: LocomotiveAgent,
        stationId: String,
    ) : LocomotiveEvent(
        eventType = eventType,
        eventData = mapOf(
            LOCOMOTIVE_ID to locomotiveId,
            APPEARANCE_TIME to appearanceTime.toString(),
            MAX_WEIGHT to maxWeight.toString(),
            STATION_ID to stationId,
        ),
        locomotiveAgent = locomotiveAgent,
    ) {
        companion object {
            val eventType = "locomotive_appeared"
            const val LOCOMOTIVE_ID = "locomotive_id"
            const val MAX_WEIGHT = "max_weight"
            const val APPEARANCE_TIME = "appearance_time"
            const val STATION_ID = "station_id"
        }
    }

    class AcceptCargoWagon(
        cargoId: String,
        timeFrom: Int,
        timeTo: Int,
        locomotiveId: String,
        locomotiveAgent: LocomotiveAgent,
        handledEventId: Int,
    ) : LocomotiveEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoId,
            TIME_FROM to timeFrom.toString(),
            TIME_TO to timeTo.toString(),
            LOCOMOTIVE_ID to locomotiveId,
        ),
        locomotiveAgent = locomotiveAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "locomotive_accept_cargo_wagon"
            const val CARGO_ID = "cargo_id"
            val TIME_FROM = "time_from"
            val TIME_TO = "time_to"
            val LOCOMOTIVE_ID = "locomotive_id"
        }
    }

    class RequestCargoWagon(
        timeFrom: Int,
        timeTo: Int,
        cargoId: String,
        locomotiveId: String,
        locomotiveAgent: LocomotiveAgent,
        handledEventId: Int,
    ) : LocomotiveEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoId,
            TIME_FROM to timeFrom.toString(),
            TIME_TO to timeTo.toString(),
            LOCOMOTIVE_ID to locomotiveId,
        ),
        locomotiveAgent = locomotiveAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "locomotive_request_cargo_wagon"
            val CARGO_ID = "cargo_id"
            val TIME_FROM = "time_from"
            val TIME_TO = "time_to"
            val LOCOMOTIVE_ID = "locomotive_id"
        }
    }

    class DenyCargoWagon(
        locomotiveAgent: LocomotiveAgent,
        handledEventId: Int,
    ) : LocomotiveEvent(
        eventType = eventType,
        eventData = mapOf(),
        locomotiveAgent = locomotiveAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "locomotive_deny_cargo_wagon"
        }
    }

}