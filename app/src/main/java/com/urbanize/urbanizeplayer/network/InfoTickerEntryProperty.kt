package com.urbanize.urbanizeplayer.network

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class InfoTickerEntryProperty(
    val position: Int,
    val title: String,
    val text: String
)

