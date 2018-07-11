package io.github.wulkanowy.sdk.dictionaries

import com.google.gson.annotations.SerializedName

data class NoteCategory(

        @SerializedName("Id")
        val id: Int,

        @SerializedName("Kod")
        val code: String,

        @SerializedName("Nazwa")
        val name: String
)
