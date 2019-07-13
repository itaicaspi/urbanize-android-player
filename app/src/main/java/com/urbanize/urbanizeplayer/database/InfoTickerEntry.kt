package com.urbanize.urbanizeplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "info_ticker")
data class InfoTickerEntry (
    @PrimaryKey
    var id: String = "",

    @ColumnInfo(name = "position")
    var position: Int = 0,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "text")
    var text: String = ""

)