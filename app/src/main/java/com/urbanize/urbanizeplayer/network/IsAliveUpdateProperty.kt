package com.urbanize.urbanizeplayer.network

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class IsAliveUpdateProperty (
    val time: Long,
    val memory: Long,
    val disk: Long
)

