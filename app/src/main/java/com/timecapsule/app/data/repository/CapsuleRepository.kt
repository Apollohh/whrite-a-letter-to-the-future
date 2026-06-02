package com.timecapsule.app.data.repository

import com.timecapsule.app.crypto.CryptoManager
import com.timecapsule.app.data.local.CapsuleDao
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.model.CapsuleStatus
import com.timecapsule.app.model.RandomMode
import com.timecapsule.app.utils.RandomDateGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

/**
 * 胶囊业务仓库
 */
class CapsuleRepository(
    private val dao: CapsuleDao,
    private val crypto: CryptoManager = CryptoManager
) {

    val allCapsules: Flow<List<CapsuleEntity>> = dao.getAll()
    val unlockedCapsules: Flow<List<CapsuleEntity>> = dao.getUnlockedCapsules()
    val readCapsules: Flow<List<CapsuleEntity>> = dao.getReadCapsules()
    val sealedCount: Flow<Int> = dao.getSealedCount()

    /**
     * 创建并封存新信件
     */
    suspend fun createAndSeal(
        content: String,
        isRandom: Boolean,
        customUnlockAt: Long? = null,
        customDisplay: String? = null
    ) = withContext(Dispatchers.IO) {
        val (iv, ciphertext) = crypto.encrypt(content)

        val (unlockAt, unlockDisplay, randomMode) = if (isRandom) {
            val mode = RandomDateGenerator.randomMode()
            val time = RandomDateGenerator.generate(mode)
            val display = formatDate(time)
            Triple(time, display, mode)
        } else {
            val time = customUnlockAt!!
            val display = customDisplay!!
            Triple(time, display, null)
        }

        val capsule = CapsuleEntity(
            content = ciphertext,
            salt = iv,
            createdAt = System.currentTimeMillis(),
            unlockAt = unlockAt,
            unlockDateDisplay = unlockDisplay,
            status = CapsuleStatus.SEALED,
            isRandom = isRandom,
            randomMode = randomMode
        )

        dao.insert(capsule)
    }

    /**
     * 解封检测
     */
    suspend fun checkAndUnlock() = withContext(Dispatchers.IO) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sealedList = dao.getSealedCapsules()

        for (capsule in sealedList) {
            val unlockDay = capsule.unlockAt.toStartOfDay()

            if (unlockDay <= today) {
                dao.markAsUnlocked(capsule.id)
            }
        }
    }

    /**
     * 解密原文
     */
    suspend fun decryptContent(capsule: CapsuleEntity): String = withContext(Dispatchers.IO) {
        crypto.decrypt(capsule.salt, capsule.content)
    }

    /**
     * 保存回信，状态改为READ
     * 修复：使用完整对象 @Update，而非 SQL 局部更新
     */
    suspend fun saveComment(capsuleId: UUID, comment: String) = withContext(Dispatchers.IO) {
        val capsule = dao.getById(capsuleId) ?: return@withContext

        // 组装完整新对象，只改需要变的字段
        val updated = capsule.copy(
            status = CapsuleStatus.READ,
            comment = comment,
            commentAt = System.currentTimeMillis()
        )

        dao.update(updated)
    }
    /**
     * 首次阅读：UNLOCKED → READ（无回信）
     */
    suspend fun markAsRead(capsuleId: UUID) = withContext(Dispatchers.IO) {
        val capsule = dao.getById(capsuleId) ?: return@withContext
        val updated = capsule.copy(status = CapsuleStatus.READ)
        dao.update(updated)
    }
    /**
     * 清除所有数据
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }

    suspend fun getById(id: UUID): CapsuleEntity? = dao.getById(id)

    suspend fun deleteDraft(capsule: CapsuleEntity) = dao.delete(capsule)

    // ==================== 工具方法 ====================

    private fun formatDate(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "${year}年${month}月${day}日"
    }

    private fun Long.toStartOfDay(): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = this@toStartOfDay }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}