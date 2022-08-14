package io.github.wulkanowy.sdk.scrapper.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recipient(

    @SerialName("skrzynkaGlobalKey")
    val mailboxGlobalKey: String,

    @SerialName("nazwa")
    val name: String,
)
