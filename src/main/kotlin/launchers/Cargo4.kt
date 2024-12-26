package ru.bashcony.launchers

import ru.bashcony.agents.CargoAgent
import ru.bashcony.agents.CargoType

fun main() {
    CargoAgent(
        agentId = 4,
        type = CargoType.BULK,
        weight = 100,
        stationId = "station_1",
        toStationId = "station_2"
    ).run()
}