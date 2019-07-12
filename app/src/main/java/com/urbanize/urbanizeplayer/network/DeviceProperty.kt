package com.urbanize.urbanizeplayer.network

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DeviceVersionProperty (
    val version: String
)

@IgnoreExtraProperties
data class DeviceStatusProperty (
    val status: String
)
