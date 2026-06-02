package com.timecapsule.app.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import android.provider.MediaStore
import com.timecapsule.app.data.local.entity.CapsuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 图片导出器
 * 将已读信件导出为PNG长图
 *
 * PRD规范：
 * - 格式：长图 .png
 * - 内容布局：米白纹理背景 + 黑色手写体排版
 * - 内容包含：原文 + 回信 + 回信时间 + 解封日期
 * - 画质：压缩至72dpi，单张≤500KB
 * - 生成方式：Android Canvas本地渲染
 */
object ImageExporter {

    private const val DPI = 72
    private const val WIDTH_PX = 1080  // 1080px宽，标准手机屏幕
    private const val PADDING = 60
    private const val LINE_HEIGHT = 56
    private const val TITLE_SIZE = 48f
    private const val BODY_SIZE = 36f
    private const val META_SIZE = 28f

    /**
     * 导出单封信件为PNG图片
     * 保存到系统相册
     */
    suspend fun exportCapsule(
        context: Context,
        capsule: CapsuleEntity,
        decryptedContent: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = createBitmap(capsule, decryptedContent)
            saveToGallery(context, bitmap, capsule.id.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 批量导出所有已读信件
     */
    suspend fun exportAll(
        context: Context,
        capsules: List<Pair<CapsuleEntity, String>>
    ): Int = withContext(Dispatchers.IO) {
        var successCount = 0
        for ((capsule, content) in capsules) {
            if (exportCapsule(context, capsule, content)) {
                successCount++
            }
        }
        successCount
    }

    /**
     * 创建Bitmap
     */
    private fun createBitmap(
        capsule: CapsuleEntity,
        content: String
    ): Bitmap {
        // 计算内容高度
        val lines = content.split("\n")
        val contentHeight = lines.size * LINE_HEIGHT + 400  // 额外空间给标题和元信息

        val height = (contentHeight + PADDING * 4).coerceAtLeast(800)

        val bitmap = Bitmap.createBitmap(WIDTH_PX, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 米白背景
        canvas.drawColor(Color.parseColor("#F5F5F0"))

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        var y = PADDING.toFloat()

        // 标题：解封日期
        paint.textSize = TITLE_SIZE
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("时间胶囊", PADDING.toFloat(), y, paint)
        y += TITLE_SIZE + 20

        paint.textSize = META_SIZE
        canvas.drawText("解封日期：${capsule.unlockDateDisplay}", PADDING.toFloat(), y, paint)
        y += META_SIZE + 40

        // 分隔线
        paint.strokeWidth = 2f
        canvas.drawLine(
            PADDING.toFloat(), y,
            (WIDTH_PX - PADDING).toFloat(), y,
            paint
        )
        y += 40

        // 原文内容
        paint.textSize = BODY_SIZE
        paint.typeface = Typeface.DEFAULT

        for (line in lines) {
            val textLine = if (line.length > 26) line.substring(0, 26) + "..." else line
            canvas.drawText(textLine, PADDING.toFloat(), y, paint)
            y += LINE_HEIGHT
        }

        // 回信区
        if (!capsule.comment.isNullOrEmpty()) {
            y += 40
            paint.strokeWidth = 1f
            canvas.drawLine(
                PADDING.toFloat(), y,
                (WIDTH_PX - PADDING).toFloat(), y,
                paint
            )
            y += 40

            paint.textSize = META_SIZE
            canvas.drawText("我的回信", PADDING.toFloat(), y, paint)
            y += META_SIZE + 20

            paint.textSize = BODY_SIZE
            val comment = capsule.comment
            val commentLines = comment?.split("\n") ?: emptyList() 
            for (line in commentLines) {
                val textLine = if (line.length > 26) line.substring(0, 26) + "..." else line
                canvas.drawText(textLine, PADDING.toFloat(), y, paint)
                y += LINE_HEIGHT
            }

            // 回信时间
            y += 20
            paint.textSize = META_SIZE
            val commentTime = capsule.commentAt?.let {
                SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(Date(it))
            } ?: ""
            canvas.drawText("回信时间：$commentTime", PADDING.toFloat(), y, paint)
        }

        // 压缩质量
        val compressed = Bitmap.createScaledBitmap(bitmap, WIDTH_PX, height, true)
        bitmap.recycle()

        return compressed
    }

    /**
     * 保存到系统相册
     */
    private fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "timecapsule_${fileName}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TimeCapsule")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            var outputStream: OutputStream? = null
            try {
                outputStream = resolver.openOutputStream(it)
                outputStream?.let { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
                }
            } finally {
                outputStream?.close()
            }
        }

        bitmap.recycle()
    }
}