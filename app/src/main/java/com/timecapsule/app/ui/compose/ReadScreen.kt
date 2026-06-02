package com.timecapsule.app.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.model.CapsuleStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 阅读页
 *
 * 修正逻辑：
 * - 首次点击解封信件时，状态自动变为 READ
 * - READ 状态显示原文 + 可选回信（已回信的显示回信内容）
 */
@Composable
fun ReadScreen(
    capsule: CapsuleEntity?,
    decryptedContent: String,
    commentInput: String,
    onCommentChange: (String) -> Unit,
    onSaveComment: () -> Unit,
    onBack: () -> Unit
) {
    if (capsule == null) return

    // 状态可能是 READ（已自动标记）或原本就是 READ
    val isRead = capsule.status == CapsuleStatus.READ
    val hasComment = !capsule.comment.isNullOrEmpty()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 顶部栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "←",
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBack() }
            )

            Text(
                text = "解封日期：${capsule.unlockDateDisplay}",
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 内容区
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // 原文
            Text(
                text = decryptedContent,
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Divider(
                color = Color(0xFFF5F5F0),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 回信区
            if (hasComment) {
                // 已回信：显示回信内容
                Text(
                    text = "我的回信",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = capsule.comment ?: "",
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "回信时间：${formatTimestamp(capsule.commentAt)}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            } else {
                // 未回信：显示输入框（可选）
                Text(
                    text = "我的回信（可选，仅一次）：",
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = commentInput,
                    onValueChange = onCommentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(1.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(Color.Black),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            if (commentInput.isEmpty()) {
                                Text(
                                    text = "此刻你想对过去的自己说什么...（可选）",
                                    color = Color.LightGray,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "回信的机会只有一次，请珍惜",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveComment,
                    enabled = commentInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F0),
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "保存回信", fontSize = 14.sp)
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}