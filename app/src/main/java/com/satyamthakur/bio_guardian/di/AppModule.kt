package com.satyamthakur.bio_guardian.di

import com.satyamthakur.bio_guardian.data.api.DisoverEndangeredApi
import com.satyamthakur.bio_guardian.data.api.MistralImageRecognitionApi
import com.satyamthakur.bio_guardian.data.datasource.MistralData
import com.satyamthakur.bio_guardian.data.datasource.MistralDataImpl
import com.satyamthakur.bio_guardian.data.repository.MistralRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun providesRetrofitForMistralApi(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.mistral.ai/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    @Named("DiscoverEndangeredSpecies")
    fun provideDiscoverEndangeredSpeciesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://bioguardian-api.satyamthakur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    fun provideDiscoverEndangeredSpeciesAPIService(
        @Named("DiscoverEndangeredSpecies") retrofit: Retrofit
    ): DisoverEndangeredApi {
        return retrofit.create(DisoverEndangeredApi::class.java)
    }

    @Singleton
    @Provides
    fun providesMistralAPIService(retrofit: Retrofit): MistralImageRecognitionApi {
        return retrofit.create(MistralImageRecognitionApi::class.java)
    }

    @Singleton
    @Provides
    fun provideMistralData(apiService: MistralImageRecognitionApi): MistralData {
        return MistralDataImpl(apiService)
    }

    @Singleton
    @Provides
    fun provideMistralRepository(mistralData: MistralData): MistralRepository {
        return MistralRepository(mistralData)
    }
}