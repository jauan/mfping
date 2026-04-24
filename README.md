# Mfping - 全能网络诊断工具

Mfping 是一款基于 Kotlin 和 Jetpack Compose 开发的 Android 网络诊断应用。它提供了从基础的 Ping 测试到高级的局域网矩阵扫描、端口探测及路由追踪等一系列强大功能。

## 📱 功能特性

- **网络仪表盘 (Dashboard)**
  - 实时显示当前联网类型（Wi-Fi、移动网络等）。
  - 展示本地 IP 地址及公网 IP 地址。
  - 紧凑型 UI 设计，关键信息一目了然。

- **子网矩阵扫描 (Subnet Scanner)**
  - **视觉化矩阵**：以 001-254 的网格形式直观展示整个子网段的状态。
  - **逐行扫描动画**：扫描过程实时反馈，具有极佳的视觉流动感。
  - **混合探测算法**：结合 ICMP Ping 与 TCP 常用端口探测，大幅提升对防火墙后隐藏设备的发现率。
  - **网关专项优化**：针对路由器（.1）进行特定服务端口探测。

- **设备发现 (Device Discovery)**
  - 经典的列表模式展示局域网内在线设备。
  - 自动识别设备主机名 (Hostname)。

- **端口扫描 (Port Scanner)**
  - 默认扫描 14 个常用服务端口（HTTP, SSH, MySQL, RDP 等）。
  - 支持自定义端口范围或指定端口扫描。
  - 实时显示端口开放状态及对应服务名称。

- **ICMP Ping 工具**
  - 实时绘制 Ping 日志，连接成功以**绿色**高亮显示。
  - 动态统计最小、平均、最大延迟及丢包情况。
  - 运行期间目标主机地址自动高亮。

- **路由追踪 (Traceroute)**
  - 采用 TTL 递增技术模拟路由跳转。
  - 详细记录每一跳 (Hop) 的 IP 和响应时间。

## 🛠 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose (Material 3)
- **架构**：MVVM + Clean Architecture 思想
- **异步处理**：Kotlin Coroutines & Flow
- **网络库**：OkHttp3 (用于公网 IP 获取), Java Socket (用于探测)
- **自适应布局**：使用 `material3-adaptive` 实现大屏及折叠屏适配。

## 📦 构建说明

项目配置了自定义 APK 输出名称。执行以下命令生成 Release 版本：

```bash
./gradlew assembleRelease
```

生成的文件位于：`app/build/outputs/apk/release/mfping_app.apk`

## ⚖️ 许可证

本项目仅供学习与网络诊断使用，请勿用于非法用途。
