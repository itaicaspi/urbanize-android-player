package com.urbanize.urbanizeplayer.network

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ContentProperty (
    val content: Content
)

data class Content (
    val img: String
)
