package ru.bashcony.agents

import io.github.evanrupert.excelkt.workbook
import kotlinx.coroutines.*
import ru.bashcony.api.EventResponse
import ru.bashcony.events.CargoEvent
import ru.bashcony.events.ScheduleEvent
import java.util.*

class ScheduleAgent(
    agentId: Int,
) : BaseAgent(agentId = agentId, agentType = "schedule") {

    private val collectedSchedule = mutableListOf<ScheduleItem>()

    private fun println(text: String) {
        System.out.println("---\n$text")
    }

    init {
        sendEvent(
            event = ScheduleEvent(
                scheduleAgent = this,
            )
        )

        CoroutineScope(Job()).launch {
            delay(7000L)

            val tableName = "${UUID.randomUUID()}.xlsx"

            workbook {
                sheet("Schedule") {
                    row {
                        cell("Cargo ID")
                        cell("Cargo Type")
                        cell("Cargo Weight")
                        cell("From Station")
                        cell("To Station")
                        cell("Wagon ID")
                        cell("Locomotive ID")
                        cell("Schedule Time")
                    }
                    collectedSchedule.forEach {
                        row {
                            cell(it.cargoId)
                            cell(it.cargoType)
                            cell(it.cargoWeight)
                            cell(it.fromStationId)
                            cell(it.toStationId)
                            cell(it.wagonId)
                            cell(it.locomotiveId)
                            cell(it.scheduleTime)
                        }
                    }
                }
            }.write(tableName)

            println("Exported to $tableName")
        }
    }

    override fun handleBroadcast(event: EventResponse, fromMyEvents: Boolean) {
        if (event.type == CargoEvent.SendSchedule.eventType) {
            if (!fromMyEvents) return

            println("Arrived new cargo schedule.")
            collectedSchedule.add(
                ScheduleItem(
                    cargoId = event.data[CargoEvent.SendSchedule.CARGO_ID].orEmpty(),
                    cargoType = CargoType.valueOf(event.data[CargoEvent.SendSchedule.CARGO_TYPE].orEmpty()),
                    cargoWeight = event.data[CargoEvent.SendSchedule.CARGO_WEIGHT]?.toInt() ?: -1,
                    fromStationId = event.data[CargoEvent.SendSchedule.STATION_FROM_ID].orEmpty(),
                    toStationId = event.data[CargoEvent.SendSchedule.STATION_TO_ID].orEmpty(),
                    wagonId = event.data[CargoEvent.SendSchedule.WAGON_ID].orEmpty(),
                    locomotiveId = event.data[CargoEvent.SendSchedule.LOCOMOTIVE_ID].orEmpty(),
                    scheduleTime = event.data[CargoEvent.SendSchedule.SCHEDULED_TIME].orEmpty(),
                )
            )
        }
    }
}

data class ScheduleItem(
    val cargoId: String,
    val cargoType: CargoType,
    val cargoWeight: Int,
    val fromStationId: String,
    val toStationId: String,
    val wagonId: String,
    val locomotiveId: String,
    val scheduleTime: String,
)