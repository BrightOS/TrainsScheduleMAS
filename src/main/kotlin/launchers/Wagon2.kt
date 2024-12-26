package ru.bashcony.launchers

import ru.bashcony.agents.CargoType
import ru.bashcony.agents.WagonAgent

fun main() {
    WagonAgent(
        agentId = 2,
        maxWeight = 100,
        cargoType = CargoType.LIQUID,
        stationId = "station_1",
    ).run()
}