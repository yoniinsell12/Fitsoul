@echo off
echo Updating Android SDK Command-line Tools...

REM Set the path to your Android SDK
set ANDROID_SDK_ROOT=C:\Users\yonii\AppData\Local\Android\Sdk

REM Use the sdkmanager to update the command-line tools
echo y | "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" --update
echo y | "%ANDROID_SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat" "cmdline-tools;latest"

echo.
echo Android SDK tools have been updated. Please restart Android Studio if it's running.
pause
