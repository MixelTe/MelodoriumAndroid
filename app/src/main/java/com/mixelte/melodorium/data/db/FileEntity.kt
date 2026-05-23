package com.mixelte.melodorium.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var rpath: String,
    var uri: String
)
