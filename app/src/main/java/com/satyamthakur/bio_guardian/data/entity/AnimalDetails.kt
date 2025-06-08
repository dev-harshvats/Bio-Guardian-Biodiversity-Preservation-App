package com.satyamthakur.bio_guardian.data.entity

import com.google.gson.annotations.SerializedName

data class AnimalDetails(
    @SerializedName("Species") val species: String,
    @SerializedName("Scientific Name") val scientificName: String,
    @SerializedName("Habitat") val habitat: String,
    @SerializedName("Diet") val diet: String,
    @SerializedName("Lifespan") val lifespan: String,
    @SerializedName("Size & Weight") val sizeWeight: String,
    @SerializedName("Reproduction") val reproduction: String,
    @SerializedName("Behavior") val behavior: String,
    @SerializedName("Conservation Status") val conservationStatus: String,
    @SerializedName("Special Adaptations") val specialAdaptations: String,
    @SerializedName("Habitat Coordinates") val habitatCoordinates: HabitatCoordinates
)

data class HabitatCoordinates(
    @SerializedName("Type") val type: String = "Polygon",
    @SerializedName("Coordinates") val coordinates: List<List<Double>>
)
