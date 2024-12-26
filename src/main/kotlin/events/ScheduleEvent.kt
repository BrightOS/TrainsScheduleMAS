package ru.bashcony.events

import ru.bashcony.agents.ScheduleAgent

class ScheduleEvent(
    scheduleAgent: ScheduleAgent,
    handledEventId: Int? = null,
) : BaseEvent(eventType, mapOf(), scheduleAgent, handledEventId) {
    companion object {
        val eventType = "schedule"
    }
}