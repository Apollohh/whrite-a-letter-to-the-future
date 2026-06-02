// TODO: 实现业务模型 Capsule
package com.timecapsule.app.model

/**
 * 时间胶囊生命周期状态
 */
enum class CapsuleStatus {
    /** 草稿状态：可编辑、可删除 */
    DRAFT,

    /** 已封存：加密锁定，不可操作、不可预览、不可删除 */
    SEALED,

    /** 已解封：未阅读，置顶展示 */
    UNLOCKED,

    /** 已阅读：可导出为图片，永久保留 */
    READ
}