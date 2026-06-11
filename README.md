# M3U TV Player — Android TV M3U 直播播放器

一个简洁的 Android TV 直播播放器，支持加载和播放 M3U/M3U8 格式的直播源。

## 功能特点

- 支持远程 HTTP/HTTPS M3U 地址
- 支持本地文件路径
- 自动解析频道名称、分组、Logo 等信息
- ExoPlayer 内核，兼容绝大多数直播流格式
- Android TV 遥控器操作优化（DPAD 导航）
- 深色简洁界面
- 无广告，完全离线

## 界面截图

| 频道列表 | 播放界面 |
|---------|---------|
| 输入 M3U 地址 → 点击加载 → 选择频道播放 | 自动播放，按 OK 键显示控制栏 |

## 构建方法

### 方法一：Android Studio（推荐）

1. 打开 Android Studio
2. File → Open → 选择 `m3u-tv-player` 目录
3. 等待 Gradle 同步完成
4. 连接 Android TV 设备或启动模拟器
5. 点击 Run 按钮

### 方法二：命令行构建

```bash
# 安装 Android SDK 后，在项目目录执行：
./gradlew assembleRelease
# APK 位置: app/build/outputs/apk/release/app-release.apk
```

## 安装到 Android TV

1. 将生成的 APK 复制到 U 盘，插入电视
2. 在电视上安装（需要开启「允许安装未知来源应用」）
3. 或用 ADB 安装：

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

## 使用方法

1. 打开 App，界面简洁：顶部输入框 + 下方频道列表
2. 输入 M3U 播放列表地址（支持 http/https 或本地文件路径）
3. 点击「加载」或按遥控器 OK 键
4. 加载完成后显示频道列表，用遥控器上下选择频道
5. 按 OK 键播放所选频道
6. 播放中按 OK/确定 键显示控制栏（返回列表、重试）
7. 按返回键回到频道列表

## M3U 格式示例

```
#EXTM3U
#EXTINF:-1 tvg-id="cctv1" tvg-name="CCTV1" group-title="央视",CCTV-1 综合
http://example.com/cctv1.m3u8
#EXTINF:-1 tvg-id="cctv2" tvg-name="CCTV2" group-title="央视",CCTV-2 财经
http://example.com/cctv2.m3u8
```

## 技术栈

- Kotlin
- AndroidX + Leanback
- Media3 ExoPlayer (HLS 支持)
- OkHttp
- ViewBinding

## 最低要求

- Android 5.0 (API 21) 或更高
- Android TV 设备
