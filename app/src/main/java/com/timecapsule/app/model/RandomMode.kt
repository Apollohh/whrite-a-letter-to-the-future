// TODO: 实现随机模式枚举 RandomMode
package com.timecapsule.app.model

/**
 * 解封时间随机模式
 * 默认模式下系统从三种算法中随机选择一种，用户无感知
 */
enum class RandomMode {
    /** 温柔模式：90~180天均匀分布 */
    GENTLE,

    /** 惊喜模式：90~730天加权分布，1年附近概率最高 */
    SURPRISE,

    /** 冒险模式：365~1825天加权分布，2年附近概率最高 */
    ADVENTURE
}