package com.timecapsule.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.model.CapsuleStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 时间胶囊数据访问对象
 */
@Dao
interface CapsuleDao {

    @Insert
    suspend fun insert(capsule: CapsuleEntity): Long

    @Update
    suspend fun update(capsule: CapsuleEntity)

    @Delete
    suspend fun delete(capsule: CapsuleEntity)

    @Query("SELECT * FROM capsules WHERE id = :id")
    suspend fun getById(id: UUID): CapsuleEntity?

    @Query("SELECT * FROM capsules ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE status = 'SEALED'")
    suspend fun getSealedCapsules(): List<CapsuleEntity>

    @Query("SELECT * FROM capsules WHERE status = 'UNLOCKED' ORDER BY unlockAt DESC")
    fun getUnlockedCapsules(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE status = 'READ' ORDER BY createdAt DESC")
    fun getReadCapsules(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE status = 'DRAFT' LIMIT 1")
    suspend fun getDraft(): CapsuleEntity?

    @Query("SELECT COUNT(*) FROM capsules WHERE status = 'SEALED'")
    fun getSealedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM capsules WHERE status = 'UNLOCKED'")
    suspend fun getUnlockedCount(): Int

    @Query("DELETE FROM capsules")
    suspend fun deleteAll()

    /**
     * 解封：SEALED → UNLOCKED
     */
    @Query("UPDATE capsules SET status = 'UNLOCKED' WHERE id = :id")
    suspend fun markAsUnlocked(id: UUID)

    /**
     * 删除 markAsRead SQL，改用完整对象 @Update
     * 在 Repository 中组装完整对象后调用 update()
     */
}