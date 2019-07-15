package com.urbanize.urbanizeplayer.network

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LayoutProperty(
    val layoutName: String,
    val layoutParams: Map<String, String>
)

