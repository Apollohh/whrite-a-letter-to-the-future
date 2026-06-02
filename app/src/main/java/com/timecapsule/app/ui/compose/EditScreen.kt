package com.timecapsule.app.ui.compose

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/**
 * 编辑页（Tab 0）
 *
 * 布局调整：
 * - 顶部：寄出按钮（右上角）
 * - 上部：时间模式选择（默认/自定义）
 * - 中部：全屏输入区
 * - 底部：字数统计（右下角）
 */
@Composable
fun EditScreen(
    content: String,
    isRandomMode: Boolean,
    customUnlockDate: Long?,
    showSendConfirm: Boolean,
    onContentChange: (String) -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onCustomDateSelected: (Long) -> Unit,
    onSendClick: () -> Unit,
    onConfirmSend: () -> Unit,
    onDismissSendConfirm: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // 顶部栏：寄出按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "时间胶囊",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            Button(
                onClick = onSendClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F0),
                    contentColor = Color.Black
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(text = "寄出", fontSize = 14.sp)
            }
        }

        // 时间模式选择栏（移到输入区上方）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeButton(
                text = "默认",
                selected = isRandomMode,
                onClick = { onToggleMode(true) }
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            ModeButton(
                text = "自定义",
                selected = !isRandomMode,
                onClick = {
                    onToggleMode(false)
                    showDatePicker(context) { timestamp ->
                        onCustomDateSelected(timestamp)
                    }
                }
            )

            // 显示已选日期
            if (!isRandomMode && customUnlockDate != null) {
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                    text = formatDateDisplay(customUnlockDate),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // 全屏输入区
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(Color.Black),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                text = "此刻你想对未来的自己说什么...",
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // 右下角字数统计
            Text(
                text = "${content.length} 字",
                color = Color.Black,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    // 寄出确认弹窗
    if (showSendConfirm) {
        AlertDialog(
            onDismissRequest = onDismissSendConfirm,
            title = {
                Text(
                    text = "确认封存",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "信件封存后不可修改或删除。\n\n" +
                            if (isRandomMode) "系统将为您随机选择解封时间。"
                            else "解封日期：${formatDateDisplay(customUnlockDate ?: 0)}",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmSend,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismissSendConfirm,
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
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.Black else Color(0xFFF5F5F0),
            contentColor = if (selected) Color.White else Color.Black
        )
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}

private fun showDatePicker(
    context: android.content.Context,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
        show()
    }
}

private fun formatDateDisplay(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return "${year}年${month}月${day}日"
}