package com.timecapsule.app.ui.main

import kotlinx.coroutines.flow.first
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timecapsule.app.data.local.AppDatabase
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.data.repository.CapsuleRepository
import com.timecapsule.app.model.CapsuleStatus
import com.timecapsule.app.utils.ImageExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 主ViewModel
 * 底部Tab导航：编辑 / 信箱 / 设置
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CapsuleRepository

    init {
        val dao = AppDatabase.getInstance(application).capsuleDao()
        repository = CapsuleRepository(dao)
    }

    // ==================== 导航状态 ====================

    /** 底部Tab：0=编辑, 1=信箱, 2=设置 */
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    /** 信箱内子页面：null=已解封信箱, "sealed"=封存信箱, "read"=阅读页 */
    private val _mailboxScreen = MutableStateFlow<String?>(null)
    val mailboxScreen: StateFlow<String?> = _mailboxScreen.asStateFlow()

    /** 启动页是否完成 */
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    // ==================== 数据状态 ====================

    /** 启动页：正在路上的信件数 */
    private val _sealedCount = MutableStateFlow(0)
    val sealedCount: StateFlow<Int> = _sealedCount.asStateFlow()

    /** 编辑页：当前输入内容 */
    private val _editContent = MutableStateFlow("")
    val editContent: StateFlow<String> = _editContent.asStateFlow()

    /** 编辑页：时间模式（true=默认，false=自定义） */
    private val _isRandomMode = MutableStateFlow(true)
    val isRandomMode: StateFlow<Boolean> = _isRandomMode.asStateFlow()

    /** 编辑页：自定义解封日期（时间戳） */
    private val _customUnlockDate = MutableStateFlow<Long?>(null)
    val customUnlockDate: StateFlow<Long?> = _customUnlockDate.asStateFlow()

    /** 已解封信箱列表 */
    private val _unlockedList = MutableStateFlow<List<CapsuleEntity>>(emptyList())
    val unlockedList: StateFlow<List<CapsuleEntity>> = _unlockedList.asStateFlow()

    /** 封存信箱列表 */
    private val _sealedList = MutableStateFlow<List<CapsuleEntity>>(emptyList())
    val sealedList: StateFlow<List<CapsuleEntity>> = _sealedList.asStateFlow()

    /** 阅读页：当前选中的胶囊 */
    private val _selectedCapsule = MutableStateFlow<CapsuleEntity?>(null)
    val selectedCapsule: StateFlow<CapsuleEntity?> = _selectedCapsule.asStateFlow()

    /** 阅读页：解密后的原文 */
    private val _decryptedContent = MutableStateFlow("")
    val decryptedContent: StateFlow<String> = _decryptedContent.asStateFlow()

    /** 阅读页：回信输入 */
    private val _commentInput = MutableStateFlow("")
    val commentInput: StateFlow<String> = _commentInput.asStateFlow()

    /** 设置页：清除确认弹窗显示 */
    private val _showClearConfirm = MutableStateFlow(false)
    val showClearConfirm: StateFlow<Boolean> = _showClearConfirm.asStateFlow()

    /** 编辑页：寄出确认弹窗显示 */
    private val _showSendConfirm = MutableStateFlow(false)
    val showSendConfirm: StateFlow<Boolean> = _showSendConfirm.asStateFlow()

    /** 草稿内容（切后台自动保存用） */
    private val _draftContent = MutableStateFlow("")
    val draftContent: StateFlow<String> = _draftContent.asStateFlow()

    init {
        checkUnlock()
        collectFlows()
    }

    // ==================== 导航方法 ====================

    /** 启动页完成 */
    fun onSplashFinished() {
        _splashFinished.value = true
    }

    /** 切换底部Tab */
    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        // 切到信箱时重置子页面
        if (index == 1) {
            _mailboxScreen.value = null
        }
    }

    /** 进入封存信箱 */
    fun navigateToSealed() {
        _mailboxScreen.value = "sealed"
    }

    /** 进入阅读页 */
    /**
     * 点击已解封信件：进入阅读页
     * 修复：首次点击时自动标记为 READ
     */
    fun onCapsuleClick(capsule: CapsuleEntity) {
        _selectedCapsule.value = capsule
        _mailboxScreen.value = "read"

        viewModelScope.launch {
            if (capsule.status == CapsuleStatus.UNLOCKED) {
                repository.markAsRead(capsule.id)
                // 更新本地状态：只改状态，保留所有其他字段
                _selectedCapsule.value = capsule.copy(status = CapsuleStatus.READ)
            }

            val text = repository.decryptContent(capsule)
            _decryptedContent.value = text
        }
    }

    /** 信箱内返回 */
    fun onMailboxBack() {
        _mailboxScreen.value = null
        _selectedCapsule.value = null
        _decryptedContent.value = ""
        _commentInput.value = ""
    }

    // ==================== 编辑页方法 ====================

    fun onEditContentChange(text: String) {
        _editContent.value = text
    }

    fun onToggleTimeMode(isRandom: Boolean) {
        _isRandomMode.value = isRandom
    }

    fun onCustomDateSelected(timestamp: Long) {
        _customUnlockDate.value = timestamp
    }

    /** 点击寄出 → 显示确认弹窗 */
    fun onSendClick() {
        val content = _editContent.value.trim()
        if (content.isEmpty()) return
        _showSendConfirm.value = true
    }

    /** 确认寄出 → 执行加密保存 */
    fun onConfirmSend() {
        _showSendConfirm.value = false
        val content = _editContent.value.trim()

        viewModelScope.launch {
            if (_isRandomMode.value) {
                repository.createAndSeal(content, isRandom = true)
            } else {
                val unlockAt = _customUnlockDate.value ?: return@launch
                val display = formatDateDisplay(unlockAt)
                repository.createAndSeal(
                    content = content,
                    isRandom = false,
                    customUnlockAt = unlockAt,
                    customDisplay = display
                )
            }

            _editContent.value = ""
            _customUnlockDate.value = null
            _draftContent.value = ""
        }
    }

    fun onDismissSendConfirm() {
        _showSendConfirm.value = false
    }

    // ==================== 阅读页方法 ====================

    fun onCommentChange(text: String) {
        _commentInput.value = text
    }

    fun onSaveComment() {
        val capsule = _selectedCapsule.value ?: return
        val comment = _commentInput.value.trim()
        if (comment.isEmpty()) return

        viewModelScope.launch {
            repository.saveComment(capsule.id, comment)
            _commentInput.value = ""
            _mailboxScreen.value = null  // 返回已解封信箱
        }
    }

    // ==================== 设置页方法 ====================

    fun onShowClearConfirm() {
        _showClearConfirm.value = true
    }

    fun onDismissClearConfirm() {
        _showClearConfirm.value = false
    }

    fun onConfirmClear() {
        viewModelScope.launch {
            repository.clearAllData()
            _showClearConfirm.value = false
        }
    }

/** 导出：包含已读(READ)和已解封未读(UNLOCKED) */
fun exportAllRead(context: Context) {
    viewModelScope.launch {
        // 修复：Flow 用 first() 获取值
        val allCapsules = repository.allCapsules.first()
        val exportableList = allCapsules
            .filter { it.status == CapsuleStatus.READ || it.status == CapsuleStatus.UNLOCKED }

        val pairs = exportableList.map { capsule ->
            val content = repository.decryptContent(capsule)
            Pair(capsule, content)
        }

        ImageExporter.exportAll(context, pairs)
    }
}
    // ==================== 生命周期方法 ====================

    /** App启动/回到前台：检查解封 */
    fun checkUnlock() {
        viewModelScope.launch {
            repository.checkAndUnlock()
        }
    }

    /** 切后台时保存草稿 */
    fun saveDraft() {
        _draftContent.value = _editContent.value
    }

    /** 回到前台时恢复草稿 */
    fun restoreDraft() {
        if (_draftContent.value.isNotEmpty() && _editContent.value.isEmpty()) {
            _editContent.value = _draftContent.value
        }
    }

    // ==================== 内部方法 ====================

    private fun collectFlows() {
        viewModelScope.launch {
            repository.sealedCount.collect { count ->
                _sealedCount.value = count
            }
        }
        viewModelScope.launch {
            repository.unlockedCapsules.collect { list ->
                _unlockedList.value = list
            }
        }
        viewModelScope.launch {
            repository.allCapsules.collect { list ->
                _sealedList.value = list.filter { it.status == CapsuleStatus.SEALED }
            }
        }
    }

    private fun formatDateDisplay(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "${year}年${month}月${day}日"
    }
}