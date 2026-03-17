#!/bin/bash
# 本地构建脚本

echo "=== 租房存证 APK 构建脚本 ==="
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ 未安装 Java，请先安装 JDK 17"
    echo "   Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "   macOS: brew install openjdk@17"
    echo "   Windows: 从 https://adoptium.net 下载"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "✓ Java 版本: $JAVA_VERSION"

# 检查 Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo ""
    echo "❌ 未设置 ANDROID_HOME 环境变量"
    echo ""
    echo "请先安装 Android SDK，然后设置环境变量："
    echo "   export ANDROID_HOME=/path/to/android-sdk"
    echo ""
    echo "或者直接使用 Android Studio 打开项目构建"
    exit 1
fi

echo "✓ Android SDK: ${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
echo ""

# 进入项目目录
cd "$(dirname "$0")"

# 创建 gradlew 如果不存在
if [ ! -f "gradlew" ]; then
    echo "创建 Gradle Wrapper..."
    gradle wrapper --gradle-version 8.2 || {
        echo "❌ 请先安装 Gradle: https://gradle.org/install/"
        exit 1
    }
fi

# 构建
echo "开始构建 APK..."
echo ""

chmod +x gradlew
./gradlew assembleDebug

if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ 构建成功！"
    echo ""
    echo "APK 文件位置："
    echo "   $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "你可以将此 APK 安装到 Android 设备上"
else
    echo ""
    echo "❌ 构建失败，请检查错误信息"
fi
