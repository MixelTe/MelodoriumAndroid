package com.mixelte.melodorium.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file")
data class File(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var rpath: String,
    var uri: String
)
