package com.satyamthakur.bio_guardian.data.api

import com.satyamthakur.bio_guardian.data.entity.MistralRequest
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MistralImageRecognitionApi {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer "
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: MistralRequest): Response<MistralResponse>
}