# QQMusic API for [AllMusic](https://github.com/Coloryr/AllMusic)

> 一个适配 **AllMusic / Coloryr AllMusic** 的 QQ 音乐外置 API 实现。

项目仅请求 QQ 音乐接口正常返回的免费 / 已授权播放链接，不包含绕过会员、付费、版权或地区限制的逻辑。

---

## ✨ 功能特性

| 功能                | 说明                            |
| :---------------- | :---------------------------- |
| 🔎 **歌曲搜索**       | 支持通过关键词搜索 QQ 音乐歌曲             |
| 🎵 **歌曲信息获取**     | 支持根据 QQ 音乐 `songmid` 获取歌曲基础信息 |
| ▶️ **播放链接解析**     | 仅返回 QQ 音乐接口正常下发的正式播放链接        |
| 🖼 **封面获取**       | 自动拼接 QQ 音乐专辑封面地址              |
| 📝 **歌词解析**       | 支持 QQ 音乐歌词接口与时间轴歌词解析          |
| 🐛 **Debug 日志开关** | 可通过 `QQSong.debug` 控制是否输出调试日志 |

---

## 🚀 使用方式

| 方式               | 说明                                         |
| :--------------- | :----------------------------------------- |
| 🏛 **固定命令（推荐）**  | 使用 AllMusic 原生命令调用，兼容现有点歌流程                |
| 🧩 **外置 API 加载** | 作为 AllMusic 外置音乐 API 使用，放入对应插件 / API 加载目录 |

---

## 📦 获取 AllMusic3

本 API 依赖 AllMusic3 的核心接口与运行环境。

AllMusic 仓库地址：

```text
https://github.com/Coloryr/AllMusic
```

你需要前往 AllMusic 仓库获取新版服务端核心文件：

| 获取方式                  | 说明                                              |
| :-------------------- | :---------------------------------------------- |
| ⚙️ **GitHub Actions** | 前往 AllMusic 仓库的 Actions 页面，下载新版构建产物             |
| 🧪 **Pre-release**    | 如果仓库发布了预览版 / 测试版，也可以在 Release 或 Pre-release 中获取 |

> 建议优先使用与你服务器当前 AllMusic 版本一致的 core jar，避免接口版本不匹配。

---

## 🛠 构建

将 AllMusic 的 `server-4.0.0-xxx.jar`，或你当前版本对应的 core jar，放入项目根目录下的 `libs/` 文件夹。

目录结构示例：

```text
AllMusic_QQMusic/
├─ libs/
│  └─ server-4.0.0-xxx.jar
├─ src/
├─ build.gradle
└─ settings.gradle
```

然后执行：

```bash
./gradlew clean build
```

Windows 环境可使用：

```bat
gradlew.bat clean build
```

构建完成后，输出文件位于：

```text
build/libs/AllMusic_QQMusic-1.0.0.jar
```

---

## 📥 安装

将构建生成的 jar 文件放入 AllMusic3 对应的外置 API / netapi 加载目录中。

```text
AllMusic_QQMusic-1.0.0.jar
```

然后重启服务端或重新加载 AllMusic。

加载成功后，API ID 为：

```text
qqmusic
```

---

## 🎮 命令示例

### 搜索歌曲 （若默认为qq音乐搜索可以省略qqmusic）

```text
/music search qqmusic 歌名
```

示例：

```text
/music search qqmusic 稻香
```

## ⚙️ Debug 日志

默认情况下，QQMusic API 不会输出详细调试日志。

如需排查接口返回、Cookie 注入、播放链接解析等问题，可以在代码中开启：

```java
QQSong.debug = true;
```

关闭调试日志：

```java
QQSong.debug = false;
```

> Debug 模式可能会输出接口请求、返回内容片段或 Cookie 相关信息。请勿在公开环境中长期开启。

---

## 🔒 安全与合规说明

本项目只请求 QQ 音乐接口正常返回的免费 / 已授权播放链接。

本项目不包含，也不会提供以下能力：

| 类型      | 说明                   |
| :------ | :------------------- |
| 🚫 会员绕过 | 不绕过 QQ 音乐会员限制        |
| 🚫 付费绕过 | 不绕过付费歌曲限制            |
| 🚫 版权绕过 | 不绕过版权或地区限制           |
| 🚫 破解逻辑 | 不包含解密、破解、逆向绕过播放限制等逻辑 |

如果 QQ 音乐接口未返回正式播放链接，本 API 会直接返回 `null`，不会使用试听链接替代。

---

## 🧪 开发说明

本 API 由 **AI 辅助 + 人工调试 + Postman 接口测试** 共同生成。

由于 QQ 音乐接口可能存在变动，不同账号、Cookie、地区、版权状态下返回结果也可能不同，因此不保证所有歌曲都能成功获取播放链接。

如果你在使用过程中遇到问题，欢迎提交 Issues，并尽量附带以下信息：

| 信息           | 说明                              |
| :----------- | :------------------------------ |
| AllMusic 版本  | 例如 `4.0.0-test2`                |
| API 版本       | 当前 jar 版本                       |
| 歌曲 `songmid` | 出问题的 QQ 音乐歌曲 ID                 |
| 是否登录 Cookie  | 是否配置 QQ 音乐相关 Cookie             |
| Debug 日志     | 开启 `QQSong.debug = true` 后的关键日志 |

---

## 🐛 反馈问题

如果发现 Bug，欢迎提交 Issue。

请尽量提供完整复现步骤，例如：

```text
1. 使用的 AllMusic 版本：
2. 使用的 QQMusic API 版本：
3. 执行的命令：
4. 歌曲 songmid：
5. 预期结果：
6. 实际结果：
7. Debug 日志：
```

---

## 📄 License

本项目仅用于学习、研究与 AllMusic 外置 API 适配测试。

请遵守 QQ 音乐相关服务条款，以及你所在地区的法律法规。
