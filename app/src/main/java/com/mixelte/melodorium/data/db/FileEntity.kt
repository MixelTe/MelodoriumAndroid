package com.mixelte.melodorium.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file")
data class FileEntity(
    @PrimaryKey
    var rpath: String,
    var uri: String,
    val artworkPath: String? = null
)
