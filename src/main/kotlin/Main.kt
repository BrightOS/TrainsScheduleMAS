package ru.bashcony

import java.time.LocalDateTime
import java.util.*

private val startTime = LocalDateTime.of(2010, 1, 1, 1, 0)

data class Cargo(
    val id: String,
    val type: CargoType,
    val weight: Double,
    val destinationStation: Station,
    var scheduled: Boolean = false,
    var occupancy: Pair<LocalDateTime, LocalDateTime>? = null,
)

enum class CargoType {
    BULK, LIQUID, DANGEROUS, PLATFORM
}

data class Wagon(
    val id: String,
    val type: CargoType,
    val capacity: Double,
    var isOperational: Boolean = true,
    val occupancyTimeList: MutableList<Triple<LocalDateTime, LocalDateTime, String>> = mutableListOf()
) {

    val availableFrom: LocalDateTime
        get() = occupancyTimeList.lastOrNull()?.second ?: startTime
}

data class Locomotive(
    val id: String,
    val maxWagons: Int,
    var isOperational: Boolean = true,
    val occupancyTimeList: MutableList<Triple<LocalDateTime, LocalDateTime, String>> = mutableListOf()
) {

    val availableFrom: LocalDateTime
        get() = occupancyTimeList.lastOrNull()?.second ?: startTime
}

data class Train(
    val id: String,
    val locomotive: Locomotive,
    val wagons: List<Wagon>,
    val cargos: List<Cargo>,
    val route: List<Station>, // Маршрут, включающий станции
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime
)

sealed class Event(val time: LocalDateTime) {
    data class TrainDepartureEvent(val train: Train, val at: LocalDateTime) : Event(at)
    data class ResourceFailureEvent(val resource: Any, val at: LocalDateTime) : Event(at)

    var valid = true
}

data class Station(val name: String) {
    private val cargos = mutableSetOf<Cargo>()
    val wagons = mutableSetOf<Wagon>()
    private val locomotives = mutableSetOf<Locomotive>()
    private val schedule = mutableListOf<Train>()
    private val eventQueue = PriorityQueue<Event>(compareBy { it.time })

    fun println(string: String) {
        kotlin.io.println("[$name] - $string")
    }

    private val connectedStations = mutableListOf<Station>()

    fun connectStation(station: Station) {
        connectedStations.add(station)
    }

    fun addCargo(cargo: Cargo) {
        println("Added cargo: $cargo. Proceeding train reschedule")
        cargos.add(cargo)
    }

    fun addWagon(wagon: Wagon) {
        wagons.add(wagon)
    }

    fun addLocomotive(locomotive: Locomotive) {
        locomotives.add(locomotive)
    }

    fun scheduleEvent(event: Event) {
        eventQueue.add(event)
    }

    fun processScheduledEvents() {
        eventQueue
            .filter { it.valid }
            .sortedBy { it !is Event.ResourceFailureEvent }
            .forEach { event ->
                when (event) {
                    is Event.TrainDepartureEvent -> {
                    }

                    is Event.ResourceFailureEvent -> {
                        handleFailure(event)
                    }
                }
            }

        println("Train scheduled")
        rescheduleCargos()
    }

    private fun markResourcesAsOccupied(train: Train) {
//        train.locomotive.occupancyTimeList.add(Pair(train.departureTime, train.arrivalTime))
//        train.wagons.forEach { wagon ->
//            wagon.occupancyTimeList.add(Pair(train.departureTime, train.arrivalTime))
//        }
    }

    private fun handleFailure(event: Event.ResourceFailureEvent) {
        val failedResource = event.resource
        when (failedResource) {
            is Wagon -> {
                println("Replacing failed wagon: ${failedResource.id}")
                wagons.find { it.id == failedResource.id }?.isOperational = false
            }

            is Locomotive -> {
                println("Replacing failed locomotive: ${failedResource.id}")
                failedResource.isOperational = false
            }
        }
    }

    fun rescheduleCargos(rescheduleFrom: LocalDateTime = startTime) {
        var cargos = cargos.filter { it.occupancy == null || (it.occupancy?.first ?: startTime) >= rescheduleFrom }
        var wagons = wagons.filter { (it.occupancyTimeList.lastOrNull()?.third ?: name) == name && (it.occupancyTimeList.lastOrNull()?.second ?: startTime) >= rescheduleFrom }
        var locomotives = locomotives.filter { (it.occupancyTimeList.lastOrNull()?.third ?: name) == name && (it.occupancyTimeList.lastOrNull()?.second ?: startTime) >= rescheduleFrom }

        while (cargos.any { !it.scheduled } && locomotives.isNotEmpty()) {

            val notScheduledCargos = cargos.filter { !it.scheduled }

            val mostPopularDestinationCargos = notScheduledCargos
                .groupBy { it.destinationStation.name }
                .maxByOrNull { it.value.size }?.value ?: emptyList()

            val availableLocomotive = locomotives.filter {
                it.isOperational && it.maxWagons > mostPopularDestinationCargos.size
            }.minByOrNull { it.availableFrom } ?: locomotives.maxByOrNull { it.maxWagons }

            if (availableLocomotive == null) {
                println("No available locomotives.")
                break
            }

            val allowedCargos = mostPopularDestinationCargos.take(availableLocomotive.maxWagons)

            val trainWagons = mutableListOf<Wagon>()
            val trainCargos = mutableListOf<Cargo>()
            var totalWeight = 0.0

            var maxAvailableFrom = availableLocomotive.availableFrom

            for (cargo in allowedCargos) {
//                println(wagons.map { "${it.id} ${it.occupancyTimeList} ${it.isOperational}" })
                val wagon = wagons.firstOrNull {
                    it.isOperational && it.type == cargo.type && it.capacity >= cargo.weight && it !in trainWagons
                }
                if (wagon != null && trainWagons.size < availableLocomotive.maxWagons) {
                    trainWagons.add(wagon)
                    trainCargos.add(cargo)
                    if (wagon.availableFrom > maxAvailableFrom)
                        maxAvailableFrom = wagon.availableFrom
                    cargo.scheduled = true
                    totalWeight += cargo.weight
                }
            }

            if (trainCargos.isNotEmpty()) {
                val departureTime = maxAvailableFrom
                val arrivalTime = departureTime.plusHours(5)

                val route = listOf(this, mostPopularDestinationCargos[0].destinationStation)

                trainWagons.forEach { it.occupancyTimeList.add(Triple(departureTime, arrivalTime, route.last().name)) }
                availableLocomotive.occupancyTimeList.add(Triple(departureTime, arrivalTime, route.last().name))

                val train = Train(
                    id = UUID.randomUUID().toString(),
                    locomotive = availableLocomotive,
                    wagons = trainWagons,
                    cargos = trainCargos,
                    route = route,
                    departureTime = departureTime,
                    arrivalTime = arrivalTime
                )

                schedule.add(train)
                markResourcesAsOccupied(train)

                mostPopularDestinationCargos[0].destinationStation.let { destination ->
                    trainWagons.forEach {
                        destination.addWagon(it)
                    }
                    destination.addLocomotive(availableLocomotive)
                    destination.rescheduleCargos(arrivalTime)
                }
            } else {
                println("Can't find wagons for remain cargos. Remained cargos: ${allowedCargos.count { !it.scheduled }}")
//                println("$wagons $locomotives")
                break
            }

            cargos = this.cargos.filter { (it.occupancy?.first ?: startTime) >= rescheduleFrom }
            wagons = this.wagons.filter { (it.occupancyTimeList.lastOrNull()?.third ?: name) == name && (it.occupancyTimeList.lastOrNull()?.second ?: startTime) >= rescheduleFrom }
            locomotives = this.locomotives.filter { (it.occupancyTimeList.lastOrNull()?.third ?: name) == name && (it.occupancyTimeList.lastOrNull()?.second ?: startTime) >= rescheduleFrom }
        }
    }

    fun printSchedule() {
        schedule.forEach { train ->
            println("Train ID: ${train.id}, Departure: ${train.departureTime}, Arrival: ${train.arrivalTime}, Locomotive: ${train.locomotive.id}, Wagons: ${train.wagons.map { it.id }}, Cargos: ${train.cargos.map { it.id }}, Route: ${train.route.joinToString { it.name }}")
        }
    }
}

fun main() {
    val stationA = Station("Station A")
    val stationB = Station("Station B")

    stationA.addWagon(Wagon("W1", CargoType.BULK, 15.0))
    stationA.addWagon(Wagon("W2", CargoType.LIQUID, 25.0))
    stationA.addWagon(Wagon("W3", CargoType.BULK, 15.0))
    stationA.addWagon(Wagon("W4", CargoType.LIQUID, 25.0))

    stationA.addLocomotive(Locomotive("L1", 3))

    stationA.addCargo(Cargo("C1", CargoType.BULK, 10.0, stationB))
    stationA.addCargo(Cargo("C2", CargoType.LIQUID, 20.0, stationB))
    stationB.addCargo(Cargo("C3", CargoType.LIQUID, 15.0, stationA))
    stationA.addCargo(Cargo("C4", CargoType.LIQUID, 20.0, stationB))
    stationA.addCargo(Cargo("C5", CargoType.LIQUID, 20.0, stationB))

    val failureTime = startTime.plusDays(2)
    stationA.scheduleEvent(Event.ResourceFailureEvent(stationA.wagons.random(), failureTime))

    stationA.processScheduledEvents()
//    stationB.processScheduledEvents()

    stationA.printSchedule()
    stationB.printSchedule()
}
