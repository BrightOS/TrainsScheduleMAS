package ru.bashcony.launchers

import ru.bashcony.agents.LocomotiveAgent

fun main() {
    LocomotiveAgent(
        agentId = 1,
        maxWeight = 500,
        stationId = "station_1"
    ).run()
}