package com.satyamthakur.bio_guardian.data.entity


import com.google.gson.annotations.SerializedName

class DiscoverEndangeredAnimalsResponse : ArrayList<DiscoverEndangeredAnimalsResponse.DiscoverEndangeredAnimalsResponseItem>(){
    data class DiscoverEndangeredAnimalsResponseItem(
        @SerializedName("animal_name")
        val animalName: String? = null,
        @SerializedName("animal_type")
        val animalType: String? = null,
        @SerializedName("biological_name")
        val biologicalName: String? = null,
        @SerializedName("conservation_status")
        val conservationStatus: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("image_url")
        val imageUrl: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null
    )
}