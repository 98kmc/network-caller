package com.kmc.networking

import com.google.gson.JsonElement
import okhttp3.Response

internal interface RequestExecutor {

    suspend fun RequestExecutor.executeRequest(request:  Networking.DataRequest): Pair<JsonElement?, Response> {
        return request.getServiceResponse()
    }
}
