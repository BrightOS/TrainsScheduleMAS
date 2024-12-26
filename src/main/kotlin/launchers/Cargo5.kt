package ru.bashcony.launchers

import ru.bashcony.agents.CargoAgent
import ru.bashcony.agents.CargoType

fun main() {
    CargoAgent(
        agentId = 5,
        type = CargoType.BULK,
        weight = 100,
        stationId = "station_2",
        toStationId = "station_1"
    ).run()
}