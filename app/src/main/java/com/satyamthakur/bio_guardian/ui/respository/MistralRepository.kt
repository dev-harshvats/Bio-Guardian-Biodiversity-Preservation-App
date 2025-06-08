package com.satyamthakur.bio_guardian.data.repository

import com.satyamthakur.bio_guardian.data.datasource.MistralData
import com.satyamthakur.bio_guardian.data.entity.MistralRequest
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import retrofit2.Response
import javax.inject.Inject

class MistralRepository @Inject constructor(
    private val mistralData: MistralData
) {
    suspend fun getAnimalInfoFromImage(mistralRequest: MistralRequest): Response<MistralResponse> {
        return mistralData.getMistralData(mistralRequest)
    }
}
