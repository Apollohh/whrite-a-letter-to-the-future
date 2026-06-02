// TODO: 实现 SplashScreen（启动页）
package com.timecapsule.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 启动页
 *
 * PRD规范：
 * - 背景：纯白 #FFFFFF
 * - 动画：无
 * - 文案："你还有 X 封信在路上"（系统默认字体，黑色）
 * - 停留时间：1秒后自动进入编辑页
 */
@Composable
fun SplashScreen(
    sealedCount: Int,
    onFinished: () -> Unit
) {
    // 1秒后自动跳转，无动画
    LaunchedEffect(Unit) {
        delay(1000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (sealedCount > 0) {
                "你还有 ${sealedCount} 封信在路上"
            } else {
                "写一封信给未来的自己"
            },
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            // 系统默认字体，不指定fontFamily
        )
    }
}