package com.timecapsule.app.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timecapsule.app.R
import com.timecapsule.app.data.local.entity.CapsuleEntity

/**
 * 封存信箱（信箱子页面）
 *
 * 布局调整：
 * - 保留返回按钮（底部导航在封存时隐藏）
 */
@Composable
fun SealedScreen(
    sealedList: List<CapsuleEntity>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 顶部栏：返回 + 标题
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
                text = "封存信箱",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 信件列表
        if (sealedList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 空状态纯空白
            }
        } else {
            LazyColumn {
                items(sealedList, key = { it.id.toString() }) { capsule ->
                    SealedItem(capsule = capsule)
                }
            }
        }
    }
}

@Composable
private fun SealedItem(capsule: CapsuleEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_envelope),
            contentDescription = null,
            tint = Color(0xFFF5F5F0),
            modifier = Modifier.padding(end = 16.dp)
        )

        Text(
            text = "一封来自过去的信件",
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}