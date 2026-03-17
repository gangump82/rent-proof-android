# 租房存证 - Android App

> 纯本地应用，无需后端服务

## 项目结构

```
rent-proof-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/rentproof/app/
│   │   │   ├── RentProofApp.kt          # Application
│   │   │   ├── data/
│   │   │   │   ├── Models.kt            # 数据模型 + Room
│   │   │   │   └── Repository.kt        # 数据仓库
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt
│   │   │       ├── MainViewModel.kt
│   │   │       ├── HomeFragment.kt      # 首页
│   │   │       ├── TimelineFragment.kt  # 时间线
│   │   │       ├── HelperFragment.kt    # 维权助手
│   │   │       └── RecordAdapter.kt     # 列表适配器
│   │   └── res/
│   │       ├── layout/
│   │       ├── drawable/
│   │       ├── navigation/
│   │       └── values/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## 技术栈

- **语言：** Kotlin
- **架构：** MVVM
- **数据库：** Room (SQLite)
- **UI：** Material Design 3
- **导航：** Navigation Component
- **异步：** Coroutines + LiveData
- **相机：** CameraX

## 核心功能

1. **拍照存证** - 相机拍照 + 时间水印
2. **时间线** - 按日期查看存证记录
3. **导出** - 复制证据摘要到剪贴板
4. **维权文档** - 押金退还、维修催告等模板

## 构建运行

1. 用 Android Studio 打开项目
2. Sync Gradle
3. Run on device (API 26+)

## 注意事项

- 需要相机、存储、位置权限
- 数据存储在本地 SQLite
- 照片存储在外部存储目录
