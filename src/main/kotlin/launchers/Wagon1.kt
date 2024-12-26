package ru.bashcony.launchers

import ru.bashcony.agents.CargoType
import ru.bashcony.agents.WagonAgent

fun main() {
    WagonAgent(
        agentId = 1,
        maxWeight = 100,
        cargoType = CargoType.BULK,
        stationId = "station_1",
    ).run()
}