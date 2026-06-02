// TODO: 实现 CapsuleEntity（PRD里的Capsule数据结构）
package com.timecapsule.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timecapsule.app.model.CapsuleStatus
import com.timecapsule.app.model.RandomMode
import java.util.UUID

/**
 * 时间胶囊数据库实体
 * 对应Room表 "capsules"
 *
 * 存储策略：
 * - 元数据（id, createdAt, unlockAt, status等）：明文存储，用于查询排序
 * - content（加密后的信件内容）：AES-256-GCM密文
 * - salt：加密盐值，解密必需参数
 * - comment（回信）：解封后写入，明文存储
 */
@Entity(tableName = "capsules")
data class CapsuleEntity(
    /** 唯一标识，自动生成 */
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),

    /** AES-256-GCM加密后的信件内容（密文） */
    val content: ByteArray,

    /** 加密盐值，解密时需要 */
    val salt: ByteArray,

    /** 创建时间戳，毫秒 */
    val createdAt: Long,

    /** 解封时间戳，仅比较年月日（时分秒归零） */
    val unlockAt: Long,

    /** 解封日期展示文本，格式 "YYYY年MM月DD日" */
    val unlockDateDisplay: String,

    /** 当前状态：DRAFT/SEALED/UNLOCKED/READ */
    var status: CapsuleStatus,

    /** 回信内容，仅一次，保存后锁定 */
    var comment: String? = null,

    /** 回信时间戳，毫秒 */
    var commentAt: Long? = null,

    /** 是否为默认随机模式（true=默认，false=自定义日期） */
    val isRandom: Boolean,

    /** 默认模式下系统选择的随机算法，用户不可见 */
    val randomMode: RandomMode? = null
) {
    /**
     * ByteArray需要手动实现equals，否则比较的是数组引用而非内容
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CapsuleEntity

        if (id != other.id) return false
        if (!content.contentEquals(other.content)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (createdAt != other.createdAt) return false
        if (unlockAt != other.unlockAt) return false
        if (unlockDateDisplay != other.unlockDateDisplay) return false
        if (status != other.status) return false
        if (comment != other.comment) return false
        if (commentAt != other.commentAt) return false
        if (isRandom != other.isRandom) return false
        if (randomMode != other.randomMode) return false

        return true
    }

    /**
     * 对应equals，ByteArray使用contentHashCode()
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + unlockAt.hashCode()
        result = 31 * result + unlockDateDisplay.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (commentAt?.hashCode() ?: 0)
        result = 31 * result + isRandom.hashCode()
        result = 31 * result + (randomMode?.hashCode() ?: 0)
        return result
    }
}