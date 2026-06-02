# whrite-a-letter-to-the-future
一款纯本地、零云端、零打扰的情感记录应用。  它不是工具，而是一个**情感装置**——让你写给未来的自己一封信，封存起来，直到约定的日子才准拆开。期间没有任何提醒、推送或倒计时，只有某天打开应用时，突然发现"有一封信到了"的惊喜感。
> 写给未来的信。
> 
> 有些记忆只属于那部手机。

[](https://developer.android.com/)
[](https://kotlinlang.org/)
[](LICENSE)
[](CHANGELOG.md)

---

## 产品理念

一款纯本地、零云端、零打扰的情感记录应用。

它不是工具，而是一个**情感装置**——让你写给未来的自己一封信，封存起来，直到约定的日子才准拆开。期间没有任何提醒、推送或倒计时，只有某天打开应用时，突然发现"有一封信到了"的惊喜感。

### 核心原则

| 原则  | 说明  |
| --- | --- |
| **被动惊喜** | 不提醒、不催促，某天打开突然发现信到了 |
| **仪式闭环** | 封存有确认、解封有日期、阅读有回信、导出有纪念 |
| **黑盒保护** | 未解封信件绝对不可预览，连首句都不暴露 |
| **设备绑定** | 强化"珍贵因为易逝"的情感价值，换机即失 |
| **零打扰** | 无推送、无Badge、无后台任务、无电量焦虑 |
| **极致静默** | 操作无Toast，无反馈噪音，一切静默完成 |

---

## 技术栈

| 层级  | 选型  |
| --- | --- |
| 开发语言 | Kotlin |
| 架构模式 | MVVM + Repository |
| UI 框架 | Jetpack Compose / View 系统 |
| 数据库 | Room (SQLite)，预留 Migration |
| 加密方案 | AES-256-GCM + Android Keystore (TEE/StrongBox) |
| 图片生成 | Android Canvas API |
| 最低系统版本 | Android 8.0 (API 26) |

---

## 功能特性

### 写信与封存

- 全屏纯白编辑界面，无字数限制
- 两种解封时间模式：
  - **默认模式**：系统从三种算法中随机选择，用户无感知
    - `GENTLE`：90~180天均匀分布
    - `SURPRISE`：90~730天加权（1年峰值）
    - `ADVENTURE`：365~1825天加权（2年峰值）
  - **自定义模式**：使用系统 DatePickerDialog 选择具体日期
- 异步 AES-256-GCM 加密，硬件密钥绑定
- 封存后不可修改、不可删除、不可预览

### 解封与阅读

- 打开应用时自动检测到期信件（仅比较年月日）
- 当天解封信件随机置顶展示
- 阅读页显示解封日期，原文只读
- 支持单次回信（保存后锁定）

### 导出与备份

- 已读信件可导出为长图 PNG（米白纹理背景 + 黑色手写体排版）
- 批量备份所有已读信件
- 单张图片 ≤ 500KB，72dpi

### 数据管理

- 卸载默认保留数据，重装自动恢复
- 设置页提供"清除所有数据"选项
- **换机/设备重置后未解封信件永久不可恢复**（设计如此）

---

## 架构设计

### 数据模型

```kotlin
@Entity(tableName = "capsules")
data class Capsule(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val content: ByteArray,              // AES-256-GCM 加密密文
    val salt: ByteArray,                 // 加密盐值
    val createdAt: Long,                 // 创建时间戳
    val unlockAt: Long,                  // 解封时间戳（仅比较年月日）
    val unlockDateDisplay: String,       // 解封日期展示
    var status: CapsuleStatus,           // DRAFT / SEALED / UNLOCKED / READ
    var comment: String?,                // 回信（单次）
    var commentAt: Long?,                // 回信时间戳
    val isRandom: Boolean,               // 是否默认模式
    val randomMode: RandomMode?          // 系统分配的算法（用户不可见）
)
```

### 状态机

```
编辑 → DRAFT → 寄出 → SEALED → 打开App检测 → UNLOCKED → 阅读 → READ
                                              ↓              ↓
                                          置顶展示        可导出
```

### 加密方案

- **算法**：AES-256-GCM
- **密钥来源**：Android TEE（StrongBox 优先，回退 TEE）
- **密钥存储**：Android Keystore，不可导出，不可备份
- **硬件绑定**：换机/设备重置后密钥不可恢复

---

## 安装与使用

### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 11+
- Android SDK API 26+
- Kotlin 1.8+

### 构建

```bash
git clone https://github.com/yourusername/time-capsule.git
cd time-capsule
./gradlew assembleDebug
```

### 首次启动

应用首次启动会强制展示免责声明，用户必须勾选"我已理解并同意"后方可进入应用：

> 有些记忆只属于那部手机。
> 换机、卸载后重装（若选择清除数据）、设备损坏或重置，未解封信件将永久丢失。
> 已解封信件可导出为图片留存。

---

## 项目结构

```
├── app/
│   ├── src/main/java/com/timecapsule/app/
│   │   ├── data/           # Room 数据库、DAO、Repository
│   │   ├── crypto/         # 加密管理器 (Tink + Keystore)
│   │   ├── ui/             # Compose / View 界面层
│   │   │   ├── editor/     # 编辑页
│   │   │   ├── mailbox/    # 已解封/封存信箱
│   │   │   ├── reader/     # 阅读页
│   │   │   ├── settings/   # 设置页
│   │   │   └── export/     # 图片导出
│   │   ├── viewmodel/      # MVVM ViewModel
│   │   ├── model/          # 数据模型
│   │   └── util/           # 工具类（日期生成、首句提取等）
│   └── src/main/res/       # 资源文件（米白纹理、图标等）
├── docs/                   # 产品文档与 PRD
└── gradle/                 # Gradle 配置
```

---

## 性能指标

| 指标  | 目标值 |
| --- | --- |
| 冷启动时间 | ≤ 1.5 秒 |
| 加密 1000 字耗时 | ≤ 200 ms |
| 解密 100 封信耗时 | ≤ 500 ms |
| 导出图片耗时 | ≤ 1 秒 |
| 内存占用 | ≤ 50 MB |
| 安装包体积 | ~3.5 MB |

---

## 安全与隐私

- **零云端**：所有数据仅存储于设备本地，无任何网络请求
- **零权限**：除存储导出图片外，不申请任何敏感权限
- **硬件加密**：密钥由 Android Keystore 管理，不可提取
- **明文不落盘**：敏感内容仅在内存中解密，不写入明文缓存

---

## 设计规范

| 维度  | 规格  |
| --- | --- |
| 设计风格 | 极简、留白、无装饰、优雅简约大气 |
| 主背景 | 纯白 `#FFFFFF` |
| 主文字/图标 | 纯黑 `#000000` |
| 辅助元素 | 米白 `#F5F5F0` |
| 字体  | 系统默认字体，无自定义字体 |
| 动画  | 无动画，页面即时切换 |
| 暗黑模式 | 不支持（仅浅色模式） |
| 反馈方式 | 极致静默，无 Toast、无震动、无声音 |

---

## 不做清单 (Anti-Features)

以下功能**明确不做**，以维持产品纯粹性：

- ❌ 动画效果（启动页、寄出、拆信、封存均无动画）
- ❌ 云端同步/备份
- ❌ 推送通知、Badge、声音、震动
- ❌ 后台定时任务
- ❌ 实时倒计时
- ❌ 多设备登录
- ❌ 社交分享（仅支持导出图片）
- ❌ 富文本编辑（仅纯文本）
- ❌ 暗黑模式
- ❌ 密码找回机制
- ❌ 未解封信件删除/导出
- ❌ Toast 提示（所有操作静默完成）

---

## 贡献指南

本项目目前处于**开发冻结**状态（v1.2 Final），核心功能与架构已确定，不接受功能增删的 PR。

以下类型的贡献欢迎提交：

- 🐛 Bug 修复
- 📝 文档改进
- ⚡ 性能优化（不改变交互逻辑）
- 🔒 安全增强

请遵循现有代码风格，保持"极致静默"的交互原则。

---

## 开源协议

[MIT License](LICENSE)

---

## 致谢

> "时间胶囊"的设计灵感来源于实体时间胶囊的仪式感与不确定性。
> 
> 感谢所有相信"等待本身也是一种美好"的人。

---

<p align="center">
  <sub>有些记忆只属于那部手机。</sub>
</p>
