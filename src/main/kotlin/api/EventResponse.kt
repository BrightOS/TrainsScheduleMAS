package ru.bashcony.api

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("agent_type") val agentType: String,
    @SerializedName("creation_dt") val creationTime: String,
    @SerializedName("agent_id") val agentId: String,
    @SerializedName("data") val data: Map<String, String>
)