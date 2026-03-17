# 如何获取 APK 文件

## 方案一：Android Studio 构建（推荐）

1. **安装 Android Studio**
   - 下载：https://developer.android.com/studio
   - 安装后打开

2. **打开项目**
   - File → Open → 选择 `rent-proof-android` 目录

3. **等待 Sync**
   - 首次打开会下载依赖，需要几分钟

4. **构建 APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或菜单栏 Build → Make Project (Ctrl+F9)

5. **获取 APK**
   - 构建完成后，点击通知栏的 `locate` 链接
   - APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

---

## 方案二：GitHub Actions 自动构建

1. **上传到 GitHub**
   ```bash
   cd rent-proof-android
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/rent-proof.git
   git push -u origin main
   ```

2. **触发构建**
   - 进入 GitHub 仓库
   - Actions → Build APK → Run workflow

3. **下载 APK**
   - 构建完成后，在 Artifacts 中下载

---

## 方案三：命令行构建（需要 Java + Android SDK）

### macOS/Linux
```bash
cd rent-proof-android
chmod +x build-apk.sh
./build-apk.sh
```

### Windows
```cmd
cd rent-proof-android
build-apk.bat
```

---

## 方案四：在线构建服务

使用 [AppCenter](https://appcenter.ms) 或 [Bitrise](https://www.bitrise.io)：
1. 连接 GitHub 仓库
2. 配置 Android 构建
3. 下载 APK

---

## 快速对比

| 方案 | 难度 | 速度 | 推荐度 |
|------|------|------|--------|
| Android Studio | ⭐⭐ | 快 | ⭐⭐⭐⭐⭐ |
| GitHub Actions | ⭐⭐⭐ | 慢 | ⭐⭐⭐⭐ |
| 命令行 | ⭐⭐⭐⭐ | 中 | ⭐⭐⭐ |
| 在线服务 | ⭐⭐ | 慢 | ⭐⭐ |

---

**最快方式：** 用 Android Studio 打开项目，Build → Build APK
