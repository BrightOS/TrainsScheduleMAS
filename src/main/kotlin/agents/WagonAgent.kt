package ru.bashcony.agents

import kotlinx.coroutines.delay
import ru.bashcony.api.EventResponse
import ru.bashcony.events.CargoEvent
import ru.bashcony.events.LocomotiveEvent
import ru.bashcony.events.WagonEvent

private const val NO_CARGO = "No cargo"
private const val NO_LOCOMOTIVE = "No locomotive"

class WagonAgent(
    agentId: Int,
    val maxWeight: Int,
    val cargoType: CargoType,
    val stationId: String,
) : BaseAgent(agentId = agentId, agentType = "wagon") {

    private val relocateEvents = mutableListOf<WagonRelocateEvent>()
    private val awaitingEvents = mutableListOf<WagonRelocateEvent>()

    private fun println(text: String) {
        System.out.println("---\n$text")
    }

    init {
        sendEvent(
            event = WagonEvent.Appeared(
                wagonId = agentName,
                appearanceTime = 0,
                wagonType = cargoType,
                wagonWeight = maxWeight,
                stationId = stationId,
                wagonAgent = this,
            ),
            changeTime = true
        )
    }

    override fun handleBroadcast(event: EventResponse, fromMyEvents: Boolean) {
        when (event.type) {
            // Появился новый груз
            CargoEvent.Appeared.eventType -> {
                val cargoStation = event.data[CargoEvent.Appeared.CARGO_STATION].orEmpty()
                val cargoWeight = event.data[CargoEvent.Appeared.CARGO_WEIGHT]?.toInt() ?: -1
                val cargoType = CargoType.valueOf(event.data[CargoEvent.Appeared.CARGO_TYPE].orEmpty())

                val lastStationId = if (relocateEvents.isNotEmpty()) relocateEvents.last().toStationId else stationId
                if (lastStationId != cargoStation) return

                if (cargoWeight > maxWeight || cargoType != this.cargoType) {
                    println("We're not interested in new cargo")
                    sendEvent(
                        event = WagonEvent.DenyCargo(
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                } else {
                    println("Sending request to the new cargo")
                    sendEvent(
                        event = WagonEvent.RequestCargo(
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                }
            }

            // Груз принял наше предложение
            CargoEvent.ConfirmWagon.eventType -> {
                if (!fromMyEvents) return
                println("Cargo accepted our booking")

                val cargoId = event.data[CargoEvent.ConfirmWagon.CARGO_ID].orEmpty()
                val cargoWeight = event.data[CargoEvent.ConfirmWagon.CARGO_WEIGHT]?.toInt() ?: 0
                val stationFrom = event.data[CargoEvent.ConfirmWagon.STATION_FROM].orEmpty()
                val stationTo = event.data[CargoEvent.ConfirmWagon.STATION_TO].orEmpty()

                val lastTimeTo = if (relocateEvents.isNotEmpty()) relocateEvents.last().timeTo else 0

                println("Adding cargo wagon to awaiting relocation events list")
                awaitingEvents.add(
                    WagonRelocateEvent(
                        timeFrom = lastTimeTo,
                        timeTo = -1,
                        fromStationId = stationFrom,
                        toStationId = stationTo,
                    ).apply {
                        this.cargoId = cargoId
                        this.cargoWeight = cargoWeight
                    }
                )

                println("Sending loaded wagon event")
                sendEvent(
                    event = WagonEvent.Loaded(
                        wagonId = agentName,
                        cargoId = cargoId,
                        weight = cargoWeight,
                        readyTime = lastTimeTo,
                        fromStationId = stationFrom,
                        toStationId = stationTo,
                        wagonAgent = this,
                    )
                )
            }

            // Локомотив прислал предложение
            LocomotiveEvent.RequestCargoWagon.eventType -> {
                if (!fromMyEvents) return
                println("Achieved request from locomotive")

                val cargoId = event.data[LocomotiveEvent.RequestCargoWagon.CARGO_ID].orEmpty()
                val locomotiveId = event.data[LocomotiveEvent.RequestCargoWagon.LOCOMOTIVE_ID].orEmpty()
                val timeFrom = event.data[LocomotiveEvent.RequestCargoWagon.TIME_FROM]?.toInt() ?: -1
                val timeTo = event.data[LocomotiveEvent.RequestCargoWagon.TIME_TO]?.toInt() ?: -1
                val awaitingEvent = awaitingEvents.firstOrNull { it.cargoId == cargoId }

                if (awaitingEvent == null) {
                    println("We can't accept locomotive: loaded wagon is already taken")
                    sendEvent(
                        event = WagonEvent.DenyLocomotive(
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                } else {
                    println("Accepting locomotive")
                    sendEvent(
                        event = WagonEvent.AcceptLocomotive(
                            wagonWeight = awaitingEvent.cargoWeight,
                            scheduleTimeFrom = timeFrom,
                            fromStationId = awaitingEvent.fromStationId,
                            toStationId = awaitingEvent.toStationId,
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                    println("Removing relocate event from awaiting schedule")
                    awaitingEvents.remove(awaitingEvent)
                    println("Adding relocate event to schedule")
                    relocateEvents.add(
                        WagonRelocateEvent(
                            timeFrom = timeFrom,
                            timeTo = timeTo,
                            fromStationId = awaitingEvent.fromStationId,
                            toStationId = awaitingEvent.toStationId,
                        ).apply {
                            this.cargoId = cargoId
                            this.locomotiveId = locomotiveId
                            this.cargoWeight = awaitingEvent.cargoWeight
                        }
                    )
                    println("Sending schedule to cargo $cargoId")
                    sendEvent(
                        event = WagonEvent.SendCargoSchedule(
                            scheduleTime = timeFrom,
                            wagonId = agentName,
                            locomotiveId = locomotiveId,
                            cargoId = cargoId,
                            wagonAgent = this,
                        )
                    )
                    println("Sending appear to ${awaitingEvent.toStationId} event")
                    sendEvent(
                        event = WagonEvent.Appeared(
                            wagonId = agentName,
                            appearanceTime = timeTo,
                            wagonType = cargoType,
                            wagonWeight = maxWeight,
                            stationId = awaitingEvent.toStationId,
                            wagonAgent = this,
                        ),
                        changeTime = true
                    )
                }
            }

            // Появился новый локомотив
            LocomotiveEvent.Appeared.eventType -> {
                println("New locomotive appeared")
                val maxWeight = event.data[LocomotiveEvent.Appeared.MAX_WEIGHT]?.toInt() ?: -1
                val stationId = event.data[LocomotiveEvent.Appeared.STATION_ID].orEmpty()
                val appearanceTime = event.data[LocomotiveEvent.Appeared.APPEARANCE_TIME]?.toInt() ?: -1

                awaitingEvents.forEach {
                    if (stationId != it.fromStationId) return@forEach

                    println("Locomotive is suitable for ${it.cargoId}. Sending request")
                    sendEvent(
                        event = WagonEvent.RequestLocomotive(
                            cargoId = it.cargoId,
                            wagonWeight = it.cargoWeight,
                            readyTime = it.timeFrom,
                            fromStationId = it.fromStationId,
                            toStationId = it.toStationId,
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                }
            }

            // Груз прислал предложение
            CargoEvent.RequestWagon.eventType -> {
                if (!fromMyEvents) return
                println("Achieved request from cargo")
                val cargoId = event.data[CargoEvent.RequestWagon.CARGO_ID].orEmpty()
                val cargoWeight = event.data[CargoEvent.RequestWagon.CARGO_WEIGHT]?.toInt() ?: -1
                val cargoType = CargoType.valueOf(event.data[CargoEvent.RequestWagon.CARGO_TYPE].orEmpty())
                val stationFrom = event.data[CargoEvent.RequestWagon.STATION_FROM].orEmpty()
                val stationTo = event.data[CargoEvent.RequestWagon.STATION_TO].orEmpty()

                val lastStationId = if (relocateEvents.isNotEmpty()) relocateEvents.last().toStationId else stationId
                if (lastStationId != stationFrom) return

                if (cargoWeight > maxWeight || cargoType != this.cargoType) {
                    println("We can't work with this cargo")
                    sendEvent(
                        event = WagonEvent.DenyCargo(
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )
                } else {
                    println("Accepting cargo")
                    sendEvent(
                        event = WagonEvent.AcceptCargo(
                            wagonAgent = this,
                            handledEventId = event.id,
                        )
                    )

                    val lastTimeTo = if (relocateEvents.isNotEmpty()) relocateEvents.last().timeTo else 0

                    println("Adding cargo wagon to awaiting relocation events list")
                    awaitingEvents.add(
                        WagonRelocateEvent(
                            timeFrom = lastTimeTo,
                            timeTo = -1,
                            fromStationId = stationFrom,
                            toStationId = stationTo,
                        ).apply {
                            this.cargoId = cargoId
                            this.cargoWeight = cargoWeight
                        }
                    )

                    println("Sending loaded wagon event")
                    sendEvent(
                        event = WagonEvent.Loaded(
                            wagonId = agentName,
                            cargoId = cargoId,
                            weight = cargoWeight,
                            readyTime = lastTimeTo,
                            fromStationId = stationFrom,
                            toStationId = stationTo,
                            wagonAgent = this,
                        )
                    )
                }
            }

            // Локомотив принял наше предложение
            LocomotiveEvent.AcceptCargoWagon.eventType -> {
                if (!fromMyEvents) return
                println("Achieved accept from locomotive")
                val cargoId = event.data[LocomotiveEvent.AcceptCargoWagon.CARGO_ID].orEmpty()
                val locomotiveId = event.data[LocomotiveEvent.AcceptCargoWagon.LOCOMOTIVE_ID].orEmpty()
                val timeFrom = event.data[LocomotiveEvent.AcceptCargoWagon.TIME_FROM]?.toInt() ?: -1
                val timeTo = event.data[LocomotiveEvent.AcceptCargoWagon.TIME_TO]?.toInt() ?: -1
                val awaitingEvent = awaitingEvents.firstOrNull { it.cargoId == cargoId }

                if (awaitingEvent == null) return

                println("Removing relocate event from awaiting schedule")
                awaitingEvents.remove(awaitingEvent)
                println("Adding relocate event to schedule")
                relocateEvents.add(
                    WagonRelocateEvent(
                        timeFrom = timeFrom,
                        timeTo = timeTo,
                        fromStationId = awaitingEvent.fromStationId,
                        toStationId = awaitingEvent.toStationId,
                    ).apply {
                        this.cargoId = cargoId
                        this.locomotiveId = locomotiveId
                        this.cargoWeight = awaitingEvent.cargoWeight
                    }
                )
                println("Sending schedule to cargo $cargoId")
                sendEvent(
                    event = WagonEvent.SendCargoSchedule(
                        scheduleTime = timeFrom,
                        wagonId = agentName,
                        locomotiveId = locomotiveId,
                        cargoId = cargoId,
                        wagonAgent = this,
                    )
                )
                println("Sending appear to ${awaitingEvent.toStationId} event")
                sendEvent(
                    event = WagonEvent.Appeared(
                        wagonId = agentName,
                        appearanceTime = timeTo,
                        wagonType = cargoType,
                        wagonWeight = maxWeight,
                        stationId = awaitingEvent.toStationId,
                        wagonAgent = this,
                    )
                )
            }
        }
    }

}

class WagonRelocateEvent(
    val timeFrom: Int,
    val timeTo: Int,
    val fromStationId: String,
    val toStationId: String,
) {
    var cargoId: String = NO_CARGO
    var cargoWeight = Integer.MAX_VALUE
    var locomotiveId: String = NO_LOCOMOTIVE
}