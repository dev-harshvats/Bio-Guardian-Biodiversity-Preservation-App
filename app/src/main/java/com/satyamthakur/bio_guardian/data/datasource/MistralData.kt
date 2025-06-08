package com.satyamthakur.bio_guardian.data.datasource

import com.satyamthakur.bio_guardian.data.entity.MistralRequest
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import retrofit2.Response

interface MistralData {
    suspend fun getMistralData(mistralRequest: MistralRequest): Response<MistralResponse>
}
