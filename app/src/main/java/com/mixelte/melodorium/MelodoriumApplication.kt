package com.mixelte.melodorium

import androidx.room.Room
import com.mixelte.melodorium.data.db.AppDatabase
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.data.repository.SettingsRepository

class MelodoriumApplication : android.app.Application() {
    lateinit var settingsRepository: SettingsRepository
    lateinit var musicRepository: MusicRepository

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(this, AppDatabase::class.java, "melodorium-db").build()
        settingsRepository = SettingsRepository(this)
        musicRepository = MusicRepository(this, database)
    }
}