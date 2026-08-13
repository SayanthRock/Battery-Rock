package dev.sayanthrock.batteryrock.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Insert
    suspend fun insertChargingSession(session: ChargingSession): Long

    @Update
    suspend fun updateChargingSession(session: ChargingSession)

    @Query("SELECT * FROM charging_sessions WHERE id = :id")
    suspend fun getChargingSessionById(id: Long): ChargingSession?

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllChargingSessions(): Flow<List<ChargingSession>>

    @Query("SELECT * FROM charging_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveChargingSession(): ChargingSession?

    @Query("SELECT * FROM charging_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveChargingSessionFlow(): Flow<ChargingSession?>

    @Insert
    suspend fun insertBatteryHistory(history: BatteryHistory)

    @Query("SELECT * FROM battery_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getBatteryHistorySince(startTime: Long): Flow<List<BatteryHistory>>
}
