package ru.bashcony.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.bashcony.events.BaseEvent
import ru.bashcony.events.RequestEvent


object RestAPI {
    private val baseUrl = "https://api.cosmos.arpakit.com/api"
    private var listType = object : TypeToken<List<EventResponse?>?>() {}.type
    private val client = OkHttpClient()
    private val JSON: MediaType = "application/json".toMediaType()

    fun getEvents(): List<EventResponse>? {
        val request = Request.Builder()
            .url("$baseUrl/get_events")
            .get()
            .build()

        val response = client
            .newCall(request)
            .execute()

        return Gson().fromJson(response.body.string(), listType)
    }

    fun sendEvent(requestEvent: RequestEvent): EventResponse? {
        val request = Request.Builder()
            .url("$baseUrl/create_event")
            .post(
                Gson().toJson(RequestEvent(
                    requestEvent.type,
                    requestEvent.agentId,
                    requestEvent.agentType,
                    requestEvent.data,
                    requestEvent.agentIdsWhoHandled,
                )).toRequestBody(JSON)
            )
            .build()

        val response = client
            .newCall(request)
            .execute()

        return Gson().fromJson(response.body.string(), EventResponse::class.java)
    }
}