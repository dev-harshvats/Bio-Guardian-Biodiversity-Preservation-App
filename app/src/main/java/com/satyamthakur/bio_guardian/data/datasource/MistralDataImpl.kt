package com.satyamthakur.bio_guardian.data.datasource

import com.satyamthakur.bio_guardian.data.api.MistralImageRecognitionApi
import com.satyamthakur.bio_guardian.data.entity.MistralRequest
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import retrofit2.Response
import javax.inject.Inject

class MistralDataImpl @Inject constructor(
    private val apiService: MistralImageRecognitionApi
) : MistralData {
    override suspend fun getMistralData(mistralRequest: MistralRequest): Response<MistralResponse> {
        return apiService.getChatCompletion(mistralRequest)
    }
}
