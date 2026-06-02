package com.timecapsule.app.ui.main

import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timecapsule.app.data.local.PreferenceManager
import com.timecapsule.app.ui.components.DisclaimerDialog
import com.timecapsule.app.ui.compose.EditScreen
import com.timecapsule.app.ui.compose.ReadScreen
import com.timecapsule.app.ui.compose.SealedScreen
import com.timecapsule.app.ui.compose.SettingsScreen
import com.timecapsule.app.ui.compose.SplashScreen
import com.timecapsule.app.ui.compose.UnlockedScreen

/**
 * 主Activity
 * 底部Tab导航：编辑 / 信箱 / 设置
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getInstance(this)

        setContent {
            var showDisclaimer by remember { mutableStateOf(!prefs.hasAgreedDisclaimer) }
            val context = LocalContext.current
            val splashFinished by viewModel.splashFinished.collectAsState()
            val selectedTab by viewModel.selectedTab.collectAsState()
            val mailboxScreen by viewModel.mailboxScreen.collectAsState()
            val sealedCount by viewModel.sealedCount.collectAsState()
            val editContent by viewModel.editContent.collectAsState()
            val isRandomMode by viewModel.isRandomMode.collectAsState()
            val customUnlockDate by viewModel.customUnlockDate.collectAsState()
            val unlockedList by viewModel.unlockedList.collectAsState()
            val sealedList by viewModel.sealedList.collectAsState()
            val selectedCapsule by viewModel.selectedCapsule.collectAsState()
            val decryptedContent by viewModel.decryptedContent.collectAsState()
            val commentInput by viewModel.commentInput.collectAsState()
            val showClearConfirm by viewModel.showClearConfirm.collectAsState()
            val showSendConfirm by viewModel.showSendConfirm.collectAsState()

            // 首次启动免责声明
            if (showDisclaimer) {
                DisclaimerDialog(
                    onAgree = {
                        prefs.hasAgreedDisclaimer = true
                        showDisclaimer = false
                    }
                )
                return@setContent
            }

            // 启动页
            if (!splashFinished) {
                SplashScreen(
                    sealedCount = sealedCount,
                    onFinished = { viewModel.onSplashFinished() }
                )
                return@setContent
            }

            // 主界面：内容区 + 底部导航
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // 内容区（占剩余空间）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> EditScreen(
                            content = editContent,
                            isRandomMode = isRandomMode,
                            customUnlockDate = customUnlockDate,
                            showSendConfirm = showSendConfirm,
                            onContentChange = { viewModel.onEditContentChange(it) },
                            onToggleMode = { viewModel.onToggleTimeMode(it) },
                            onCustomDateSelected = { viewModel.onCustomDateSelected(it) },
                            onSendClick = { viewModel.onSendClick() },
                            onConfirmSend = { viewModel.onConfirmSend() },
                            onDismissSendConfirm = { viewModel.onDismissSendConfirm() }
                        )

                        1 -> when (mailboxScreen) {
                            null -> UnlockedScreen(
                                unlockedList = unlockedList,
                                sealedCount = sealedCount,
                                onCapsuleClick = { viewModel.onCapsuleClick(it) },
                                onNavigateToSealed = { viewModel.navigateToSealed() }
                            )
                            "sealed" -> SealedScreen(
                                sealedList = sealedList,
                                onBack = { viewModel.onMailboxBack() }
                            )
                            "read" -> ReadScreen(
                                capsule = selectedCapsule,
                                decryptedContent = decryptedContent,
                                commentInput = commentInput,
                                onCommentChange = { viewModel.onCommentChange(it) },
                                onSaveComment = { viewModel.onSaveComment() },
                                onBack = { viewModel.onMailboxBack() }
                            )
                            else -> UnlockedScreen(
                                unlockedList = unlockedList,
                                sealedCount = sealedCount,
                                onCapsuleClick = { viewModel.onCapsuleClick(it) },
                                onNavigateToSealed = { viewModel.navigateToSealed() }
                            )
                        }

                        2 -> SettingsScreen(
                            showClearConfirm = showClearConfirm,
                            onShowClearConfirm = { viewModel.onShowClearConfirm() },
                            onDismissClearConfirm = { viewModel.onDismissClearConfirm() },
                            onConfirmClear = { viewModel.onConfirmClear() },
                            onExportAll = { viewModel.exportAllRead(context) }
                        )
                    }
                }

                // 底部导航栏（仅信箱子页面为null时显示，阅读/封存时隐藏）
                val showNavBar = selectedTab != 1 || mailboxScreen == null
                if (showNavBar) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        sealedCount = sealedCount,
                        onTabSelected = { viewModel.onTabSelected(it) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUnlock()
        viewModel.restoreDraft()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveDraft()
    }
}

/**
 * 底部导航栏
 * 编辑 / 信箱 / 设置
 */
@Composable
private fun BottomNavBar(
    selectedTab: Int,
    sealedCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF5F5F0)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 编辑
        NavItem(
            label = "编辑",
            selected = selectedTab == 0,
            badge = null,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        // 信箱（带未读数量角标）
        NavItem(
            label = "信箱",
            selected = selectedTab == 1,
            badge = if (sealedCount > 0) sealedCount else null,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )

        // 设置
        NavItem(
            label = "设置",
            selected = selectedTab == 2,
            badge = null,
            onClick = { onTabSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 单个导航项
 */
@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    badge: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (selected) Color.Black else Color.Gray,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )

            // 角标
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .background(
                            color = Color.Black,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.toString(),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}