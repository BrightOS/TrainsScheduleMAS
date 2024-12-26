package ru.bashcony.events

import com.google.gson.annotations.SerializedName
import ru.bashcony.agents.BaseAgent

open class BaseEvent(
    val eventType: String,
    var eventData: Map<String, String>,
    val baseAgent: BaseAgent,
    val handledEventId: Int?
) : RequestEvent(type = eventType, agentId = baseAgent.agentName, agentType = baseAgent.agentType, data = eventData)