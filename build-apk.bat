@echo off
REM Windows 构建脚本

echo === 租房存证 APK 构建脚本 ===
echo.

REM 检查 Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未安装 Java，请先安装 JDK 17
    echo    从 https://adoptium.net 下载
    exit /b 1
)

echo ✓ Java 已安装

REM 检查 Android SDK
if "%ANDROID_HOME%"=="" (
    if "%ANDROID_SDK_ROOT%"=="" (
        echo.
        echo ❌ 未设置 ANDROID_HOME 环境变量
        echo.
        echo 请先安装 Android SDK，然后设置环境变量
        echo 或者直接使用 Android Studio 打开项目构建
        exit /b 1
    )
)

echo ✓ Android SDK 已配置
echo.

REM 构建
echo 开始构建 APK...
echo.

call gradlew.bat assembleDebug

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ✅ 构建成功！
    echo.
    echo APK 文件位置：
    echo    %cd%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 你可以将此 APK 安装到 Android 设备上
) else (
    echo.
    echo ❌ 构建失败，请检查错误信息
)
