package io.github.wulkanowy.sdk.scrapper.messages

import io.github.wulkanowy.sdk.scrapper.adapter.CustomDateAdapter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Message(

    @SerialName("apiGlobalKey")
    val apiGlobalKey: String,

    @SerialName("data")
    @Serializable(with = CustomDateAdapter::class)
    val date: LocalDateTime,

    @SerialName("id")
    val id: Int,

    @SerialName("nadawca")
    val sender: String,

    @SerialName("odbiorcy")
    val receivers: List<String>,

    @SerialName("odczytana")
    val isRead: Boolean,

    @SerialName("temat")
    val subject: String,

    @SerialName("tresc")
    val content: String,

    @SerialName("zalaczniki")
    val attachments: List<MessageAttachment>,
)
