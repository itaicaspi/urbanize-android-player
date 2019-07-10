package com.urbanize.urbanizeplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign (
    @PrimaryKey
    var id: String = "",

    @ColumnInfo(name = "original_filename")
    var originalFilename: String = ""
)