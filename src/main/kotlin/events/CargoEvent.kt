package ru.bashcony.events

import ru.bashcony.agents.CargoAgent
import ru.bashcony.agents.CargoType
import ru.bashcony.events.CargoEvent.ConfirmWagon.Companion

sealed class CargoEvent(
    eventType: String,
    eventData: Map<String, String>,
    cargoAgent: CargoAgent,
    handledEventId: Int? = null,
) : BaseEvent(eventType, eventData, cargoAgent, handledEventId) {

    class Appeared(
        cargoAgent: CargoAgent,
    ) : CargoEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_STATION to cargoAgent.stationId,
            CARGO_WEIGHT to cargoAgent.weight.toString(),
            CARGO_TYPE to cargoAgent.type.name,
        ),
        cargoAgent = cargoAgent,
    ) {
        companion object {
            val eventType = "cargo_appeared"
            const val CARGO_STATION = "cargo_station"
            const val CARGO_WEIGHT = "cargo_weight"
            const val CARGO_TYPE = "cargo_type"
        }
    }

    class ConfirmWagon(
        cargoAgent: CargoAgent,
        handledEventId: Int,
    ) : CargoEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoAgent.agentName,
            CARGO_WEIGHT to cargoAgent.weight.toString(),
            STATION_FROM to cargoAgent.stationId,
            STATION_TO to cargoAgent.toStationId,
        ),
        cargoAgent = cargoAgent,
        handledEventId = handledEventId
    ) {
        companion object {
            val eventType = "cargo_confirm_wagon"
            const val CARGO_ID = "cargo_id"
            const val CARGO_WEIGHT = "cargo_weight"
            const val STATION_FROM = "station_from"
            const val STATION_TO = "station_to"
        }
    }

    class RequestWagon(
        cargoAgent: CargoAgent,
        handledEventId: Int,
    ) : CargoEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoAgent.agentName,
            CARGO_WEIGHT to cargoAgent.weight.toString(),
            CARGO_TYPE to cargoAgent.type.name,
            STATION_FROM to cargoAgent.stationId,
            STATION_TO to cargoAgent.toStationId,
        ),
        cargoAgent = cargoAgent,
        handledEventId = handledEventId
    ) {
        companion object {
            val eventType = "cargo_request_wagon"
            const val CARGO_ID = "cargo_id"
            const val CARGO_WEIGHT = "cargo_weight"
            const val CARGO_TYPE = "cargo_type"
            const val STATION_FROM = "station_from"
            const val STATION_TO = "station_to"
        }
    }

    class DenyWagon(
        cargoAgent: CargoAgent,
        handledEventId: Int,
    ) : CargoEvent(
        eventType = eventType,
        eventData = mapOf(),
        cargoAgent = cargoAgent,
        handledEventId = handledEventId
    ) {
        companion object {
            val eventType = "cargo_deny_wagon"
        }
    }

    class SendSchedule(
        cargoAgent: CargoAgent,
        cargoId: String,
        stationFromId: String,
        stationToId: String,
        wagonId: String,
        cargoType: CargoType,
        cargoWeight: Int,
        locomotiveId: String,
        scheduledTime: Int,
        handledEventId: Int,
    ) : CargoEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoId,
            CARGO_TYPE to cargoType.name,
            CARGO_WEIGHT to cargoWeight.toString(),
            STATION_FROM_ID to stationFromId,
            STATION_TO_ID to stationToId,
            WAGON_ID to wagonId,
            LOCOMOTIVE_ID to locomotiveId,
            SCHEDULED_TIME to scheduledTime.toString(),
        ),
        cargoAgent = cargoAgent,
        handledEventId = handledEventId
    ) {
        companion object {
            val eventType = "cargo_send_schedule"
            const val CARGO_ID = "cargo_id"
            const val CARGO_TYPE = "cargo_type"
            const val CARGO_WEIGHT = "cargo_weight"
            const val STATION_FROM_ID = "station_from_id"
            const val STATION_TO_ID = "station_to_id"
            const val WAGON_ID = "wagon_id"
            const val LOCOMOTIVE_ID = "locomotive_id"
            const val SCHEDULED_TIME = "scheduled_time"
        }
    }
}