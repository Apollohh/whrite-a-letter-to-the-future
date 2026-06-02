package com.timecapsule.app.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timecapsule.app.R
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.model.CapsuleStatus
import java.util.Collections

/**
 * 已解封信箱（Tab 1 主页面）
 *
 * 布局调整：
 * - 去掉底部封存信箱入口（移到顶部）
 * - 顶部：标题 + 封存信箱入口
 */
@Composable
fun UnlockedScreen(
    unlockedList: List<CapsuleEntity>,
    sealedCount: Int,
    onCapsuleClick: (CapsuleEntity) -> Unit,
    onNavigateToSealed: () -> Unit
) {
    val sortedList = rememberSortedList(unlockedList)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 顶部栏：标题 + 封存信箱入口
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "信箱",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Center)
            )

            // 右上角封存信箱入口
            Text(
                text = "封存信箱 · $sealedCount",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onNavigateToSealed() }
            )
        }

        // 信件列表
        if (sortedList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // 空状态纯空白
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(sortedList, key = { it.id.toString() }) { capsule ->
                    CapsuleItem(
                        capsule = capsule,
                        onClick = { onCapsuleClick(capsule) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CapsuleItem(
    capsule: CapsuleEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (capsule.status == CapsuleStatus.UNLOCKED) {
                    R.drawable.ic_envelope
                } else {
                    R.drawable.ic_letter_open
                }
            ),
            contentDescription = null,
            tint = Color(0xFFF5F5F0),
            modifier = Modifier.padding(end = 16.dp)
        )

        Column {
            if (capsule.status == CapsuleStatus.UNLOCKED) {
                Text(
                    text = "一封来自过去的信件",
                    color = Color.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "解封日期：${capsule.unlockDateDisplay}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                val preview = extractFirstLine(capsule.comment ?: "")
                Text(
                    text = preview,
                    color = Color.Black,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun extractFirstLine(text: String): String {
    return if (text.length <= 20) {
        text
    } else {
        text.substring(0, 20) + "\n" + text.substring(20, (40).coerceAtMost(text.length))
    }
}

@Composable
private fun rememberSortedList(list: List<CapsuleEntity>): List<CapsuleEntity> {
    return androidx.compose.runtime.remember(list) {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val unlocked = list.filter { it.status == CapsuleStatus.UNLOCKED }
        val read = list.filter { it.status == CapsuleStatus.READ }

        val todayUnlocked = unlocked.filter {
            val unlockDay = it.unlockAt.toStartOfDay()
            unlockDay == today
        }
        val otherUnlocked = unlocked.filter {
            val unlockDay = it.unlockAt.toStartOfDay()
            unlockDay != today
        }

        val shuffledToday = todayUnlocked.shuffled()
        shuffledToday + otherUnlocked.sortedByDescending { it.createdAt } +
                read.sortedByDescending { it.createdAt }
    }
}

private fun Long.toStartOfDay(): Long {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = this@toStartOfDay }
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}