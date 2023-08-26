package ru.fivegst.speedtest.repository

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteException
import ru.fivegst.speedtest.domain.SpeedTestResultOld
import kotlin.jvm.Throws

interface SpeedTestResultRepository {
    fun save(result: SpeedTestResultOld)
    fun findAllByPageAndSizeOrderedById(page: Long, size: Long): List<SpeedTestResultOld>
    fun findAll(): List<SpeedTestResultOld>

    companion object {
        @Volatile
        private lateinit var INSTANCE: SpeedTestResultRepository

        @Throws(SQLException::class, SQLiteException::class)
        fun getInstance(context: Context): SpeedTestResultRepository {
            if (!this::INSTANCE.isInitialized) {
                synchronized(this) {
                    if (!this::INSTANCE.isInitialized) {
                        INSTANCE = SpeedTestResultRepositoryImpl(context)
                    }
                }
            }
            return INSTANCE
        }
    }
}
