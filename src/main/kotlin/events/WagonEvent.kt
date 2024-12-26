package ru.bashcony.events

import ru.bashcony.agents.CargoType
import ru.bashcony.agents.WagonAgent
import ru.bashcony.events.WagonEvent.Loaded.Companion

sealed class WagonEvent(
    eventType: String,
    eventData: Map<String, String>,
    wagonAgent: WagonAgent,
    handledEventId: Int? = null,
) : BaseEvent(eventType, eventData, wagonAgent, handledEventId) {


    class Appeared(
        wagonId: String,
        appearanceTime: Int,
        wagonType: CargoType,
        wagonWeight: Int,
        stationId: String,
        wagonAgent: WagonAgent,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            WAGON_ID to wagonId,
            WAGON_TYPE to wagonType.name,
            WAGON_WEIGHT to wagonWeight.toString(),
            APPEARANCE_TIME to appearanceTime.toString(),
            STATION_ID to stationId,
        ),
        wagonAgent = wagonAgent,
    ) {
        companion object {
            val eventType = "wagon_appeared"

            const val WAGON_ID = "wagon_id"
            const val WAGON_TYPE = "wagon_type"
            const val WAGON_WEIGHT = "wagon_weight"
            const val APPEARANCE_TIME = "appearance_time"
            const val STATION_ID = "station_id"
        }
    }

    class Loaded(
        wagonId: String,
        cargoId: String,
        weight: Int,
        readyTime: Int,
        fromStationId: String,
        toStationId: String,
        wagonAgent: WagonAgent,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            WAGON_ID to wagonId,
            CARGO_ID to cargoId,
            WAGON_WEIGHT to weight.toString(),
            READY_TIME to readyTime.toString(),
            FROM_STATION_ID to fromStationId,
            TO_STATION_ID to toStationId,
        ),
        wagonAgent = wagonAgent,
    ) {
        companion object {
            val eventType = "wagon_loaded"
            const val WAGON_ID = "wagon_id"
            const val CARGO_ID = "cargo_id"
            const val WAGON_WEIGHT = "wagon_weight"
            const val READY_TIME = "ready_time"
            const val FROM_STATION_ID = "from_station_id"
            const val TO_STATION_ID = "to_station_id"
        }
    }

    class AcceptLocomotive(
        wagonWeight: Int,
        scheduleTimeFrom: Int,
        fromStationId: String,
        toStationId: String,
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            WAGON_WEIGHT to wagonWeight.toString(),
            TIME_FROM to scheduleTimeFrom.toString(),
            FROM_STATION_ID to fromStationId,
            TO_STATION_ID to toStationId,
        ),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_accept_locomotive"
            const val WAGON_WEIGHT = "wagon_weight"
            const val TIME_FROM = "time_from"
            const val FROM_STATION_ID = "from_station_id"
            const val TO_STATION_ID = "to_station_id"
        }
    }

    class RequestLocomotive(
        cargoId: String,
        wagonWeight: Int,
        readyTime: Int,
        fromStationId: String,
        toStationId: String,
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoId,
            WAGON_WEIGHT to wagonWeight.toString(),
            READY_TIME to readyTime.toString(),
            FROM_STATION_ID to fromStationId,
            TO_STATION_ID to toStationId,
        ),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_request_locomotive"
            const val CARGO_ID = "cargo_id"
            const val WAGON_WEIGHT = "wagon_weight"
            const val READY_TIME = "ready_time"
            const val FROM_STATION_ID = "from_station_id"
            const val TO_STATION_ID = "to_station_id"
        }
    }

    class DenyLocomotive(
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_deny_locomotive"
        }
    }

    class AcceptCargo(
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            WAGON_ID to wagonAgent.agentName,
        ),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_accept_cargo"
            const val WAGON_ID = "wagon_id"
        }
    }

    class RequestCargo(
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_request_cargo"
        }
    }

    class DenyCargo(
        wagonAgent: WagonAgent,
        handledEventId: Int,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(),
        wagonAgent = wagonAgent,
        handledEventId = handledEventId,
    ) {
        companion object {
            val eventType = "wagon_deny_cargo"
        }
    }

    class SendCargoSchedule(
        scheduleTime: Int,
        cargoId: String,
        wagonId: String,
        locomotiveId: String,
        wagonAgent: WagonAgent,
    ) : WagonEvent(
        eventType = eventType,
        eventData = mapOf(
            CARGO_ID to cargoId,
            SCHEDULE_TIME to scheduleTime.toString(),
            WAGON_ID to wagonId,
            LOCOMOTIVE_ID to locomotiveId,
        ),
        wagonAgent = wagonAgent,
    ) {
        companion object {
            val eventType = "wagon_send_cargo_schedule"
            const val SCHEDULE_TIME = "schedule_time"
            const val WAGON_ID = "wagon_id"
            const val CARGO_ID = "cargo_id"
            const val LOCOMOTIVE_ID = "locomotive_id"
        }
    }

}