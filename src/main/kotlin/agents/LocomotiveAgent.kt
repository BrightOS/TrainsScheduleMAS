package ru.bashcony.agents

import ru.bashcony.api.EventResponse
import ru.bashcony.events.LocomotiveEvent
import ru.bashcony.events.WagonEvent

class LocomotiveAgent(
    agentId: Int,
    val maxWeight: Int,
    val stationId: String,
) : BaseAgent(agentId = agentId, agentType = "locomotive") {

    private val relocateEvents = mutableListOf<LocomotiveRelocateEvent>()

    private fun println(text: String) {
        System.out.println("---\n$text")
    }

    init {
        sendEvent(
            event = LocomotiveEvent.Appeared(
                locomotiveId = agentName,
                appearanceTime = 0,
                maxWeight = maxWeight,
                locomotiveAgent = this,
                stationId = stationId,
            ),
            changeTime = true
        )
    }

    override fun handleBroadcast(
        event: EventResponse,
        fromMyEvents: Boolean,
    ) {
        when (event.type) {

            // В вагон поместился груз и он оповещает об этом
            WagonEvent.Loaded.eventType -> {
                println("Loaded wagon appeared")

                val readyTime = event.data[WagonEvent.Loaded.READY_TIME]?.toInt() ?: -1
                val wagonWeight = event.data[WagonEvent.Loaded.WAGON_WEIGHT]?.toInt() ?: Integer.MAX_VALUE
                val fromStationId = event.data[WagonEvent.Loaded.FROM_STATION_ID].orEmpty()
                val toStationId = event.data[WagonEvent.Loaded.TO_STATION_ID].orEmpty()
                val cargoId = event.data[WagonEvent.Loaded.CARGO_ID].orEmpty()

                // Ищем рейсы, которые могут нас взять
                val neededRelocateEvent = relocateEvents.firstOrNull {
                    it.fromStationId == fromStationId
                            && it.toStationId == toStationId
                            && it.timeFrom >= readyTime
                            && (it.occupiedWeight + wagonWeight) <= maxWeight
                }

                if (neededRelocateEvent == null) {
                    val lastStationId = if (relocateEvents.isEmpty()) stationId else relocateEvents.last().toStationId

                    if (lastStationId != fromStationId || wagonWeight > maxWeight) {
                        println("We can't take it.")
                        sendEvent(
                            event = LocomotiveEvent.DenyCargoWagon(
                                locomotiveAgent = this,
                                handledEventId = event.id,
                            )
                        )
                    } else {
                        val timeFrom = maxOf(relocateEvents.lastOrNull()?.timeTo ?: -1, readyTime, 0)
                        // Предлагаем новый рейс
                        println("Sending request with new relocate event")
                        sendEvent(
                            event = LocomotiveEvent.RequestCargoWagon(
                                cargoId = cargoId,
                                timeFrom = timeFrom,
                                timeTo = timeFrom + 1,
                                locomotiveId = agentName,
                                locomotiveAgent = this,
                                handledEventId = event.id,
                            )
                        )
                    }
                } else {
                    // Предлагаем существующий рейс
                    println("Sending request with existing relocate event")
                    sendEvent(
                        event = LocomotiveEvent.RequestCargoWagon(
                            cargoId = cargoId,
                            timeFrom = neededRelocateEvent.timeFrom,
                            timeTo = neededRelocateEvent.timeTo,
                            locomotiveId = agentName,
                            locomotiveAgent = this,
                            handledEventId = event.id,
                        )
                    )
                }
            }

            // Вагон согласился прицепиться к локомотиву
            WagonEvent.AcceptLocomotive.eventType -> {
                if (!fromMyEvents) return

                println("Wagon sent accept request")
                val timeFrom = event.data[WagonEvent.AcceptLocomotive.TIME_FROM]?.toInt() ?: -1
                val wagonWeight = event.data[WagonEvent.AcceptLocomotive.WAGON_WEIGHT]?.toInt() ?: Integer.MAX_VALUE
                val fromStationId = event.data[WagonEvent.AcceptLocomotive.FROM_STATION_ID].orEmpty()
                val toStationId = event.data[WagonEvent.AcceptLocomotive.TO_STATION_ID].orEmpty()

                if (timeFrom == -1 || wagonWeight == Integer.MAX_VALUE) return

                val relocateEvent = relocateEvents.firstOrNull { it.timeFrom == timeFrom }
                if (relocateEvent != null) {
                    relocateEvent.occupiedWeight += wagonWeight
                } else {
                    println("Adding relocate event to schedule")
                    relocateEvents.add(
                        LocomotiveRelocateEvent(
                            timeFrom = timeFrom,
                            timeTo = timeFrom + 1,
                            fromStationId = fromStationId,
                            toStationId = toStationId,
                        )
                    )

                    println("Sending appear to $toStationId event")
                    sendEvent(
                        event = LocomotiveEvent.Appeared(
                            locomotiveId = agentName,
                            appearanceTime = timeFrom + 1,
                            maxWeight = maxWeight,
                            locomotiveAgent = this,
                            stationId = toStationId,
                        ),
                        changeTime = true
                    )
                }
            }

            // Вагон хочет прицепиться к локомотиву
            WagonEvent.RequestLocomotive.eventType -> {
                println("Wagon requests to book us")
                val wagonWeight = event.data[WagonEvent.RequestLocomotive.WAGON_WEIGHT]?.toInt() ?: Integer.MAX_VALUE
                val cargoId = event.data[WagonEvent.RequestLocomotive.CARGO_ID].orEmpty()
                val readyTime = event.data[WagonEvent.RequestLocomotive.READY_TIME]?.toInt() ?: -1
                val fromStationId = event.data[WagonEvent.RequestLocomotive.FROM_STATION_ID].orEmpty()
                val toStationId = event.data[WagonEvent.RequestLocomotive.TO_STATION_ID].orEmpty()

                val neededRelocateEvent = relocateEvents.firstOrNull {
                    it.fromStationId == fromStationId
                            && it.toStationId == toStationId
                            && it.timeFrom >= readyTime
                            && (it.occupiedWeight + wagonWeight) <= maxWeight
                }

                if (neededRelocateEvent == null) {
                    val lastStationId = if (relocateEvents.isEmpty()) stationId else relocateEvents.last().toStationId

                    if (lastStationId != fromStationId || wagonWeight > maxWeight) {
                        println("We can't accept this wagon")
                        sendEvent(
                            event = LocomotiveEvent.DenyCargoWagon(
                                locomotiveAgent = this,
                                handledEventId = event.id,
                            )
                        )
                    } else {
                        val timeFrom = maxOf(relocateEvents.lastOrNull()?.timeTo ?: -1, readyTime, 0)
                        // Предлагаем новый рейс
                        println("Sending accept with new relocate event")
                        sendEvent(
                            event = LocomotiveEvent.AcceptCargoWagon(
                                cargoId = cargoId,
                                timeFrom = timeFrom,
                                timeTo = timeFrom + 1,
                                locomotiveId = agentName,
                                locomotiveAgent = this,
                                handledEventId = event.id,
                            )
                        )

                        println("Adding relocate event to schedule")
                        relocateEvents.add(
                            LocomotiveRelocateEvent(
                                timeFrom = timeFrom,
                                timeTo = timeFrom + 1,
                                fromStationId = fromStationId,
                                toStationId = toStationId,
                            )
                        )

                        println("Sending appear to $toStationId event")
                        sendEvent(
                            event = LocomotiveEvent.Appeared(
                                locomotiveId = agentName,
                                appearanceTime = timeFrom + 1,
                                maxWeight = maxWeight,
                                locomotiveAgent = this,
                                stationId = toStationId,
                            ),
                            changeTime = true
                        )
                    }
                } else {
                    // Предлагаем существующий рейс
                    println("Sending accept with existing relocate event")
                    sendEvent(
                        event = LocomotiveEvent.AcceptCargoWagon(
                            cargoId = cargoId,
                            timeFrom = neededRelocateEvent.timeFrom,
                            timeTo = neededRelocateEvent.timeTo,
                            locomotiveId = agentName,
                            locomotiveAgent = this,
                            handledEventId = event.id,
                        )
                    )
                }
            }
        }
    }
}

class LocomotiveRelocateEvent(
    val timeFrom: Int,
    val timeTo: Int,
    val fromStationId: String,
    val toStationId: String,
) {
    var occupiedWeight = 0
}