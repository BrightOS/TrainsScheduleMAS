package ru.bashcony.agents

import ru.bashcony.api.EventResponse
import ru.bashcony.events.CargoEvent
import ru.bashcony.events.ScheduleEvent
import ru.bashcony.events.WagonEvent

class CargoAgent(
    agentId: Int,
    val type: CargoType,
    val weight: Int,
    val stationId: String,
    val toStationId: String,
) : BaseAgent(agentId = agentId, agentType = "cargo") {

    private fun println(text: String) {
        System.out.println("---\n$text")
    }

    var scheduleTime = -1
    var wagonId: String = NO_WAGON
    var locomotiveId: String = NO_LOCOMOTIVE

    init {
        sendEvent(
            event = CargoEvent.Appeared(this),
            changeTime = true
        )
    }

    override fun handleBroadcast(
        event: EventResponse,
        fromMyEvents: Boolean,
    ) {
        when (event.type) {
            // Вагон появился. Если мы можем его забрать, спрашиваем его об этом
            WagonEvent.Appeared.eventType -> {
                println("Wagon appeared. Cargo schedule time: $scheduleTime")
                if (scheduleTime > 0
                    || event.data[WagonEvent.Appeared.WAGON_TYPE] != type.name
                    || (weight > (event.data[WagonEvent.Appeared.WAGON_WEIGHT]?.toInt() ?: 0))
                    || event.data[WagonEvent.Appeared.STATION_ID].orEmpty() != stationId) return

                println("Trying to book the wagon")

                sendEvent(
                    event = CargoEvent.RequestWagon(
                        cargoAgent = this,
                        handledEventId = event.id,
                    )
                )
            }

            // Вагон спрашивает, может ли он нас забрать
            WagonEvent.RequestCargo.eventType -> {
                if (!fromMyEvents) return

                println("Cargo send the request")

                if (scheduleTime > 0) { // Если мы уже заняты
                    sendEvent(
                        event = CargoEvent.DenyWagon(
                            cargoAgent = this,
                            handledEventId = event.id,
                        )
                    )
                    println("Sending deny response")
                } else { // Если мы свободны
                    sendEvent(
                        event = CargoEvent.ConfirmWagon(
                            cargoAgent = this,
                            handledEventId = event.id,
                        )
                    )
                    println("Sending confirm response")
                }
            }

            // Вагон нас забрал
            WagonEvent.AcceptCargo.eventType -> {
                if (!fromMyEvents) return

                println("Cargo accepted")
                wagonId = event.data[WagonEvent.AcceptCargo.WAGON_ID] ?: NO_WAGON
            }

            // Вагон сообщает грузу о времени отбытия и о ID локомотива (надо для расписания)
            WagonEvent.SendCargoSchedule.eventType -> {
                val wagonId = event.data[WagonEvent.SendCargoSchedule.WAGON_ID].orEmpty()
                val cargoId = event.data[WagonEvent.SendCargoSchedule.CARGO_ID].orEmpty()
                if (agentName != cargoId) return

                println("Achieved schedule from wagon")
                scheduleTime = event.data[WagonEvent.SendCargoSchedule.SCHEDULE_TIME]?.toInt() ?: -1
                locomotiveId = event.data[WagonEvent.SendCargoSchedule.LOCOMOTIVE_ID] ?: NO_LOCOMOTIVE
            }

            // Сборщик расписания просит отдать расписание
            ScheduleEvent.eventType -> {
                println("Sending schedule to schedule agent")
                sendEvent(
                    event = CargoEvent.SendSchedule(
                        cargoAgent = this,
                        cargoId = agentName,
                        cargoType = this.type,
                        cargoWeight = this.weight,
                        stationFromId = stationId,
                        stationToId = toStationId,
                        wagonId = wagonId,
                        locomotiveId = locomotiveId,
                        scheduledTime = scheduleTime,
                        handledEventId = event.id
                    ),
                )
            }

        }
    }

    companion object {
        private const val NO_WAGON = "No wagon"
        private const val NO_LOCOMOTIVE = "No locomotive"
    }

}

enum class CargoType {
    BULK, LIQUID, SOLID
}