package com.kmc.networking

internal interface RequestExecutor {

    suspend fun RequestExecutor.executeRequest(request: DataRequest) =
        request.getServiceResponse()
}
