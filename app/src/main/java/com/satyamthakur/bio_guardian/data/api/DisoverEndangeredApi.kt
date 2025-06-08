package com.satyamthakur.bio_guardian.data.api

import com.satyamthakur.bio_guardian.data.entity.DiscoverEndangeredAnimalsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DisoverEndangeredApi {
    @GET("{animal_type}")
    suspend fun getEndangeredAnimals(
        @Path("animal_type") animalType: String)
    : Response<DiscoverEndangeredAnimalsResponse>
}