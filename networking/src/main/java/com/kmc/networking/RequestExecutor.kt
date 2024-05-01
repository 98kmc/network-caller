package com.kmc.networking

internal interface RequestExecutor {

    suspend fun RequestExecutor.executeRequest(request: Networking.DataRequest) =
        request.getServiceResponse()
}
