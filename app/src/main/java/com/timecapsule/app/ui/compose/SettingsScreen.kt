package com.timecapsule.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 设置页（Tab 2）
 *
 * 布局调整：
 * - 去掉返回按钮（底部导航已提供）
 * - 顶部：标题居中
 */
@Composable
fun SettingsScreen(
    showClearConfirm: Boolean,
    onShowClearConfirm: () -> Unit,
    onDismissClearConfirm: () -> Unit,
    onConfirmClear: () -> Unit,
    onExportAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 顶部标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "设置",
                color = Color.Black,
                fontSize = 18.sp
            )
        }

        // 设置项列表
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                title = "备份信件",
                subtitle = "批量导出所有已读信件为图片到相册",
                onClick = onExportAll
            )

            Divider(color = Color(0xFFF5F5F0), thickness = 1.dp)

            SettingsItem(
                title = "清除所有数据",
                subtitle = "删除所有胶囊数据，此操作不可逆",
                isDestructive = true,
                onClick = onShowClearConfirm
            )

            Divider(color = Color(0xFFF5F5F0), thickness = 1.dp)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "关于「时间胶囊」",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "有些记忆只属于那部手机。\n\n" +
                        "换机、卸载后重装（若选择清除数据）、设备损坏或重置，" +
                        "未解封信件将永久丢失。\n\n" +
                        "已解封信件可导出为图片留存。\n\n" +
                        "您可以在设置页主动清除所有数据。",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = onDismissClearConfirm,
            title = {
                Text(
                    text = "确认清除",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "此操作将删除所有胶囊数据，包括未解封的信件。\n\n" +
                            "未解封信件将永久丢失，无法恢复。",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmClear,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("确认清除")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissClearConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F0),
                        contentColor = Color.Black
                    )
                ) {
                    Text("取消")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            color = if (isDestructive) Color.Red else Color.Black,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}