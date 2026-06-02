package com.timecapsule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 首次启动免责声明弹窗
 *
 * PRD 5.3节要求：
 * - 强制展示，用户必须勾选"我已理解并同意"后方可进入应用
 * - 文案包含：换机丢失、卸载丢失、设备损坏丢失、可导出留存、可主动清除
 */
@Composable
fun DisclaimerDialog(
    onAgree: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* 禁止点击外部关闭 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,  // 禁止返回键关闭
            dismissOnClickOutside = false  // 禁止点击外部关闭
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .padding(24.dp)
        ) {
            // 标题
            Text(
                text = "免责声明",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 内容（可滚动）
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "有些记忆只属于那部手机。",
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "换机、卸载后重装（若选择清除数据）、设备损坏或重置，未解封信件将永久丢失。",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "已解封信件可导出为图片留存。",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "您可以在设置页主动清除所有数据。",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 同意按钮
            Button(
                onClick = onAgree,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "我已理解并同意",
                    fontSize = 16.sp
                )
            }
        }
    }
}