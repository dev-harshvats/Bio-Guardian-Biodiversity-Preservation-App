package com.satyamthakur.bio_guardian.data.entity

data class MistralResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageContent
)

data class MessageContent(
    val content: String
)
