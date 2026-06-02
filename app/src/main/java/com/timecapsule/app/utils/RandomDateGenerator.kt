// TODO: 实现随机算法 RandomDateGenerator
package com.timecapsule.app.utils

import com.timecapsule.app.model.RandomMode
import java.security.SecureRandom
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 随机解封日期生成器
 * 默认模式下系统从三种算法中随机选择一种分配解封时间
 *
 * PRD要求：
 * - GENTLE: 90~180天，均匀分布
 * - SURPRISE: 90~730天，三角/正态加权（1年365天峰值）
 * - ADVENTURE: 365~1825天，三角/正态加权（2年730天峰值）
 */
object RandomDateGenerator {

    private const val MILLIS_PER_DAY = 86400000L

    /**
     * 根据模式生成解封时间戳
     * @param mode 随机模式
     * @return 解封时间戳（毫秒）
     */
    fun generate(mode: RandomMode): Long {
        val now = System.currentTimeMillis()
        val days = when (mode) {
            RandomMode.GENTLE -> uniform(90, 180)
            RandomMode.SURPRISE -> triangular(90, 730, 365)
            RandomMode.ADVENTURE -> triangular(365, 1825, 730)
        }
        return now + days * MILLIS_PER_DAY
    }

    /**
     * 均匀分布：在[min, max]区间内等概率随机
     */
    private fun uniform(min: Int, max: Int): Int {
        return min + SecureRandom().nextInt(max - min + 1)
    }

    /**
     * 三角/正态近似分布：在[min, max]区间内，peak附近概率最高
     * 使用Box-Muller变换生成正态分布，再截断到区间
     *
     * @param min 最小天数
     * @param max 最大天数
     * @param peak 概率峰值位置（最可能出现的天数）
     */
    private fun triangular(min: Int, max: Int, peak: Int): Int {
        // 使用SecureRandom保证密码学安全
        val secureRandom = SecureRandom()
        val u = secureRandom.nextDouble()
        val v = secureRandom.nextDouble()

        // Box-Muller变换：将均匀分布转换为标准正态分布
        val normal = sqrt(-2 * ln(u)) * cos(2 * Math.PI * v)

        // 标准差：取peak到两端较小距离的1/3
        // 这样约99.7%的数据会落在[min, max]内
        val scale = (peak - min).coerceAtMost(max - peak) / 3.0

        // 正态分布 + 截断到区间
        return (peak + normal * scale).toInt().coerceIn(min, max)
    }

    /**
     * 系统随机选择一种模式（默认模式用）
     * 三种模式等概率选择
     */
    fun randomMode(): RandomMode {
        val modes = RandomMode.entries.toTypedArray()
        return modes[SecureRandom().nextInt(modes.size)]
    }
}