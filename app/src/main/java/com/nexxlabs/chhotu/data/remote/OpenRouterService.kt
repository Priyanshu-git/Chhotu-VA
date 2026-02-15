package com.nexxlabs.chhotu.data.remote

import com.nexxlabs.chhotu.data.remote.model.ChatCompletionRequest
import com.nexxlabs.chhotu.data.remote.model.ChatCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterService {
    @POST("api/v1/chat/completions")
    suspend fun getCompletions(
            @Header("Authorization") authorization: String,
            @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}
