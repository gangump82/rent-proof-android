#!/bin/bash
# 快速安装 - 在有 Java 环境的机器上运行

set -e

echo "=== 租房存证 - 快速构建 ==="

# 检查环境
command -v java >/dev/null 2>&1 || { echo "需要安装 Java 17"; exit 1; }

# 创建临时目录
TMPDIR=$(mktemp -d)
cd "$TMPDIR"

# 下载 Android command-line tools
echo "下载 Android SDK..."
curl -sO https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q commandlinetools-linux-*.zip

# 设置 SDK
export ANDROID_HOME="$TMPDIR/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"
mv cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"

# 接受许可
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1

echo "SDK 准备完成"
echo ""
echo "现在可以将项目复制到此处并运行: gradle assembleDebug"
