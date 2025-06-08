package com.satyamthakur.bio_guardian.data.entity

data class MistralRequest(
    val model: String = "pixtral-large-latest",
    val messages: List<Message>,
    val max_tokens: Int = 10000
)

data class Message(
    val role: String = "user",
    val content: List<Content>
)

data class Content(
    val type: String,
    val text: String? = null,
    val image_url: String? = null
)
