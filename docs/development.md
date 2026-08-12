# 🧑‍💻 开发与构建

[返回 README](../README.md) · [配置说明](configuration.md)

## 🧰 开发环境

| 工具 | 版本 |
| --- | --- |
| Java | 25 |
| Gradle Wrapper | 9.6.1 |
| Kotlin | 2.4.10 |
| Paper API | 1.21.11 |
| PlaceholderAPI | 2.11.6 |

项目自带 Gradle Wrapper，不需要另外安装 Gradle。

## 🏗️ 构建

Windows：

```powershell
.\gradlew.bat clean test build
```

Linux / macOS：

```bash
./gradlew clean test build
```

可直接部署的 Shadow jar 位于：

```text
build/libs/DisplayNameFmt-1.0-SNAPSHOT-all.jar
```

项目会把 SnakeYAML 打进 jar 并换到自己的包名下，避免和服务端或其他插件使用的版本冲突。Paper、PlaceholderAPI 和 Adventure API 不会被打进插件。

## 🧪 只运行测试

Windows：

```powershell
.\gradlew.bat test
```

Linux / macOS：

```bash
./gradlew test
```

测试报告位于：

```text
build/reports/tests/test/index.html
```

## 🗂️ 代码结构

```text
src/
├─ main/
│  ├─ kotlin/moe/skd/displaynamefmt/
│  │  ├─ condition/   条件解析与计算
│  │  └─ config/      配置读取、补全与迁移
│  └─ resources/
│     ├─ config.yml
│     └─ plugin.yml
└─ test/
   └─ kotlin/moe/skd/displaynamefmt/
```

主要入口：

- `DisplayNameFmt.kt`：插件生命周期、命令和定时刷新
- `DisplayNameRenderer.kt`：解析 PAPI 变量并生成 Adventure 文本
- `DisplayNameExpansion.kt`：提供命名条件 PAPI 变量
- `ConditionParser.kt`：读取条件表达式
- `ConditionEngine.kt`：计算条件并处理递归
- `ConfigLoader.kt`：把 YAML 转成运行时配置
- `YamlDocument.kt`：配置创建、补全、迁移和安全写入

## ✅ 提交改动前

建议至少确认：

1. 改动只解决一个清楚的问题。
2. 新行为有对应测试。
3. `.\gradlew.bat clean test build` 可以通过。
4. 配置结构变更时，更新 `config-version` 并提供迁移逻辑。
5. 同时更新 README 或 `docs/` 中相关说明。

## 📄 开源协议

参与本项目代表你同意自己的贡献按 [GPLv3](../LICENSE) 发布。
