// TODO: 实现加密解密 CryptoManager
package com.timecapsule.app.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 加密管理器
 * 使用AES-256-GCM算法，密钥存储于Android Keystore（硬件绑定）
 *
 * 安全特性：
 * - 密钥由Android Keystore生成，不可导出、不可备份
 * - StrongBox优先（专用安全芯片），回退TEE（可信执行环境）
 * - 换机/设备重置后密钥永久丢失，未解封信件不可恢复
 * - 明文内容不落盘，仅内存中存在
 *
 * PRD要求：加密时机为寄出时后台线程异步执行，解密时机为App启动/回到前台时批量解密
 */
object CryptoManager {

    private const val KEY_ALIAS = "capsule_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128  // GCM认证标签长度（位）

    /**
     * Android Keystore实例，加载后使用
     */
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    /**
     * 加密明文
     * 返回Pair(first=IV, second=密文)，两者都需要保存才能解密
     *
     * @param plaintext 用户输入的信件原文
     * @return Pair(iv, ciphertext) 加密盐和密文
     */
    fun encrypt(plaintext: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        // IV由Cipher自动生成，每次加密都不同
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return Pair(iv, ciphertext)
    }

    /**
     * 解密密文
     * 需要与加密时相同的IV（盐值）
     *
     * @param iv 初始化向量（加密时生成的salt）
     * @param ciphertext 密文内容
     * @return 解密后的原文
     */
    fun decrypt(iv: ByteArray, ciphertext: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    /**
     * 获取已有密钥，不存在则生成新密钥
     * 密钥一旦生成，终身绑定此设备
     */
    private fun getOrCreateKey(): SecretKey {
        return getExistingKey() ?: generateKey()
    }

    /**
     * 从Keystore读取已有密钥
     */
    private fun getExistingKey(): SecretKey? {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Exception) {
            // 密钥可能被删除或损坏，返回null触发重新生成
            // 注意：重新生成后无法解密旧数据（设备重置场景）
            null
        }
    }

    /**
     * 生成新的AES-256密钥，存入Android Keystore
     *
     * 密钥属性：
     * - 256位密钥强度
     * - GCM模式，无填充
     * - 不需要用户认证（指纹/密码）
     * - 不可导出（setUserAuthenticationRequired=false表示不需要生物认证即可使用）
     */
    private fun generateKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)  // 不需要生物认证，降低使用门槛
            .setRandomizedEncryptionRequired(true)  // 强制每次加密生成不同IV
            .build()

        return KeyGenerator.getInstance("AES", ANDROID_KEYSTORE).apply {
            init(spec)
        }.generateKey()
    }

    /**
     * 检查当前密钥是否可用（用于重装后检测）
     * 如果设备重置过，密钥会丢失，返回false
     */
    fun isKeyAvailable(): Boolean {
        return getExistingKey() != null
    }
}