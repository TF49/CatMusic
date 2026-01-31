# CatMusic

一款基于 Android 平台开发的音乐播放器应用，旨在提供流畅、美观的音乐播放体验。

## 🌟 功能特性

- 🎵 **音乐播放**：支持播放、暂停、上一首、下一首切换
- 🔄 **播放模式**：支持列表循环、单曲循环、随机播放
- 📊 **进度同步**：实时同步播放进度，支持拖动跳转
- 📝 **歌词同步**：支持歌词实时同步显示，自动滚动和高亮
- 🎨 **专辑封面**：流畅的封面旋转动画，精美的背景设计
- 🎼 **自定义歌曲**：通过本地 Node.js 服务器添加自定义/本地歌曲
- ❤️ **音乐收藏**：收藏/取消收藏歌曲，在「我的收藏」中查看并播放
- 📋 **播放列表管理**：在播放器内查看当前列表、点击切歌、从列表移除、清空列表
- 📱 **响应式设计**：适配不同屏幕尺寸

## 🛠️ 技术栈

| 技术/框架 | 用途 |
|---------|------|
| Android | 应用开发平台 |
| Java | 主要开发语言 |
| OkHttp | 网络请求 |
| Glide | 图片加载和缓存 |
| MediaPlayer | 音频播放核心组件 |
| Service | 后台音乐播放服务 |
| Binder | Activity 与 Service 通信 |
| Custom View | 自定义歌词显示控件 |
| Node.js + Axios | QQ 音乐 API 代理、本地自定义歌曲服务 |

## 📦 安装说明

### 前提条件
- Android Studio 4.0+
- Android SDK 21+
- Gradle 7.0+

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/TF49/CatMusic.git
   cd CatMusic
   ```

2. **安装并启动 Node.js 后端服务（必需）**
   ```bash
   cd catmusic_server-main
   npm install
   node prod.server.js
   ```
   - 默认监听 `http://<你的局域网 IP>:3000/`
   - 请确保 Android 设备与服务器处于同一局域网

3. **配置 Android 客户端服务器地址**
   - 打开 `app/src/main/java/com/example/catmusic/Config.java`
   - 将 `BASE_URL` 修改为你实际的服务器地址，例如：
     ```java
     public static final String BASE_URL = "http://192.168.1.16:3000/";
     ```

4. **在 Android Studio 中打开项目**
    - 启动 Android Studio
    - 选择 "Open an existing project"
    - 导航到克隆的项目目录并选择
    
5. **构建项目**
    - 等待 Gradle 同步完成
    - 点击 "Build" -> "Make Project" 构建项目

6. **运行应用**
    - 连接 Android 设备或启动模拟器（确保能访问上述服务器地址）
    - 点击 "Run" -> "Run 'app'" 运行应用

## 🚀 使用说明

### 基本操作

- **播放/暂停**：点击中央的播放/暂停按钮
- **上一首/下一首**：点击左右箭头按钮
- **切换播放模式**：点击循环按钮切换循环模式
- **随机播放**：点击随机按钮开启/关闭随机播放
- **调整进度**：拖动进度条到指定位置

### 歌词显示

- 歌词会自动同步显示，当前播放的歌词会高亮显示
- 歌词会随播放进度自动滚动
- 支持多种格式的歌词解析

### 专辑封面

- 专辑封面会随音乐播放旋转
- 封面背景采用渐变设计，具有立体感

### 自定义歌曲

- 所有 QQ 音乐歌曲由 Node.js 服务器统一代理、过滤和整理
- 通过在服务器端 `router.js` 中维护 `customSongs` 数组，可注入自定义歌曲
- 支持将本地静态资源（如 `public/music`、`public/images`）映射为可播放的歌曲与封面
- 详细步骤见仓库中的《添加自定义歌曲完整指南.md》

## 📁 项目结构

```
CatMusic/
├── app/                                 # Android 客户端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── example/
│   │   │   │           └── catmusic/
│   │   │   │               ├── adapter/          # 适配器类
│   │   │   │               ├── bean/             # 数据模型
│   │   │   │               ├── biz/              # 业务逻辑
│   │   │   │               ├── lyric/            # 自定义歌词控件及歌词解析
│   │   │   │               ├── service/          # 音乐播放服务
│   │   │   │               ├── ui/               # UI 相关类（首页/播放页/歌单页等）
│   │   │   │               ├── utils/            # 工具类（包括歌词解析、网络等）
│   │   │   │               └── Config.java       # 统一管理后端服务器地址
│   │   │   └── res/                              # 资源文件
│   │   │       ├── drawable/                     # 图片和形状资源（含专辑封面背景等）
│   │   │       ├── layout/                       # 布局文件（含播放器页面）
│   │   │       ├── values/                       # 字符串和样式资源
│   │   │       └── xml/                          # 其他 XML 资源
│   │   └── AndroidManifest.xml                   # 应用配置文件
│   └── build.gradle                              # 模块构建配置
├── catmusic_server-main/                         # Node.js 后端代理 & 自定义歌曲服务
│   ├── router.js                                 # 核心路由与 QQ 音乐 API 代理、自定义歌曲配置
│   ├── prod.server.js                            # 生产环境启动入口
│   └── public/                                   # 静态资源（自定义 mp3、封面图片等）
└── build.gradle                                  # 项目构建配置
```

## 🔧 核心功能实现

### 1. 音乐播放

- 使用 Android 系统提供的 MediaPlayer 组件进行音频播放
- 支持多种播放模式和控制方式
- 实现了进度条同步和拖动跳转

### 2. 歌词同步

- 自定义 `LyricView` 控件，支持实时同步和滚动效果
- 通过 `LyricParser` 等工具类实现灵活的歌词解析，支持多种格式
- 高亮显示当前播放的歌词，并与进度精准匹配

### 3. 专辑封面

- 流畅的旋转动画，15 秒完成一圈
- 精美的渐变背景设计，具有立体感
- 优化的阴影效果，增强视觉体验

### 4. 自定义歌曲与后端代理

- Node.js 后端通过 `router.js` 统一代理 QQ 音乐接口，完成签名、过滤与数据整形
- 在后端维护 `customSongs` 配置，即可无缝向客户端注入自定义歌曲
- 支持将自建静态资源（mp3、封面图）映射为标准歌曲结构，前端无需额外适配

## ✨ 技术亮点

1. **自定义歌词控件**：实现了支持实时同步、滚动效果和高亮显示的自定义歌词控件
2. **灵活的歌词解析**：支持多种格式的歌词解析，提高了应用的兼容性
3. **流畅的动画效果**：实现了流畅的专辑封面旋转动画
4. **良好的前后端架构设计**：Android + Node.js 分层清晰，前端专注交互展示，后端负责数据聚合与处理
5. **自定义歌曲扩展能力**：通过后端配置即可扩展自定义歌曲库，不受第三方 API 数量限制
6. **优化的性能**：通过合理的资源管理和优化的代码，提高了应用的性能

## 🤝 贡献指南

欢迎大家贡献代码和提出建议！

### 贡献流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循 Android 开发最佳实践
- 代码注释清晰，便于理解
- 保持代码风格一致

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- GitHub Issues: [https://github.com/TF49/CatMusic/issues](https://github.com/TF49/CatMusic/issues)
- Email: 2131969030@qq.com

## 📚 文档

- `项目总结.md`：项目整体设计与开发总结
- `添加自定义歌曲完整指南.md`：如何在当前架构下为 CatMusic 添加自定义歌曲的完整操作步骤

## 📋 待办事项

- [x] 优化 UI/UX 设计
- [x] 添加音乐收藏功能（已做：收藏/取消收藏、我的收藏页、歌曲项与播放器内收藏入口）
- [x] 实现播放列表管理（已做：播放列表弹窗、从列表移除、清空列表、点击切歌）
- [x] 优化应用性能（已做：统一日志控制，减少 Logcat 刷屏）
- [x] 增加单元测试和 UI 测试

### 日志与调试

应用使用 `LogUtil`（`utils/LogUtil.java`）统一控制日志输出，默认关闭详细日志（`Log.d`），避免 Logcat 持续刷屏、减轻 I/O 与 CPU 开销。需要排查问题时，在代码中将 `LogUtil.VERBOSE = true` 即可恢复全部调试日志。

---

感谢使用 CatMusic！希望你喜欢这款音乐播放器 🎵