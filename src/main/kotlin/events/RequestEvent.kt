package ru.bashcony.events

import com.google.gson.annotations.SerializedName

open class RequestEvent(
    @SerializedName("type") val type: String,
    @SerializedName("agent_id") val agentId: String,
    @SerializedName("agent_type") val agentType: String,
    @SerializedName("data") val data: Map<String, String>,
    @SerializedName("agent_ids_who_handled") val agentIdsWhoHandled: List<String> = listOf(),
)