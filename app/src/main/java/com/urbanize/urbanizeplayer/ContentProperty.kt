package com.urbanize.urbanizeplayer

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ContentProperty (
//    val id: String,
    val content: Content
)

data class Content (
    val img: String
)
