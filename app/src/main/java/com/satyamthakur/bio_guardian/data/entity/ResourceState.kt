package com.satyamthakur.bio_guardian.data.entity

sealed class ResourceState<T> {

    class Loading<T> : ResourceState<T>()
    data class Success<T>(val data: T) : ResourceState<T>()
    data class Error<T>(val error: String) : ResourceState<T>()

}