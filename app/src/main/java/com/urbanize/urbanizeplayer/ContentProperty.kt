package com.urbanize.urbanizeplayer

data class ContentProperty (
    val id: String,
    @Transient val audience: String = "",
    val content: Content,
    @Transient val general: String = ""
)

data class Content (
    val img: String
)

data class General (
    val budget: Int,
    val campaignName: String,
    val startDate: Int,
    val endDate: Int,
    val view: Int
)