# ✨ DisplayNameFmt

![Java 25](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.20--26.2-222222)
![License](https://img.shields.io/badge/License-GPLv3-blue)

一个简单、直接的 Paper 显示名格式插件。

你可以把 PlaceholderAPI 变量、颜色和条件判断拼在一起，为每位玩家生成不同的显示名。

## 🌟 它能做什么

- 直接解析 PlaceholderAPI 变量，不再套一层自定义变量语法
- 支持传统颜色，例如 `&a`、`&l`
- 支持 RGB 真彩色，例如 `&#55ff99`
- 支持数字、文字、权限判断
- 支持 AND、OR 和条件递归拼接
- 提供自己的 prefix 和 suffix PAPI 变量，名称牌、Tab 和聊天插件可以直接读取
- 定时刷新显示名，玩家换世界或重生时也会立即刷新
- 支持 `/displaynamefmt reload` 热重载
- 配置文件带版本号，升级插件时会自动迁移旧配置

## 📦 运行要求

| 项目 | 要求 |
| --- | --- |
| 服务端 | Paper 1.20–26.2 |
| Java | 25 |
| 前置插件 | PlaceholderAPI 2.11.6 或更高版本 |

> DisplayNameFmt 修改的是 Paper 的玩家显示名，不会修改正版账号名，也不会直接控制头顶名称或 Tab 列表。其他聊天、Tab 或名称牌插件是否采用这个显示名，取决于它们自己的设置。

## 🚀 快速开始

1. 安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)。
2. 把 DisplayNameFmt 的 jar 放进服务端的 `plugins` 文件夹。
3. 启动服务端，让插件生成 `plugins/DisplayNameFmt/config.yml`。
4. 修改配置后执行 `/displaynamefmt reload`。

最简单的配置：

```yaml
config-version: 1

display-name:
  format: '%displaynamefmt_prefix%%player_name%%displaynamefmt_suffix%'
  prefix: '&7[&#55ff99%player_world%&7] &f'
  suffix: ''
  refresh-interval-ticks: 20

conditions: {}
```

`display-name.format` 会直接交给 PlaceholderAPI 解析。某个变量不能用时，请先确认提供该变量的 PAPI 扩展或插件已经安装。

## 🎨 条件格式示例

下面的配置会让管理员显示红色前缀，普通玩家不显示前缀：

```yaml
display-name:
  format: '%displaynamefmt_prefix%%player_name%%displaynamefmt_suffix%'
  prefix: '%displaynamefmt_condition_staff-prefix%'
  suffix: ''
  refresh-interval-ticks: 20

conditions:
  staff-prefix:
    conditions:
      - 'permission:group.staff'
    true: '&#ff5555[管理员] '
    false: '&7'
```

命名条件会变成一个 PAPI 变量：

```text
%displaynamefmt_condition_<条件名>%
```

插件自己的前后缀也可以直接使用：

```text
%displaynamefmt_prefix%
%displaynamefmt_suffix%
```

条件结果还能引用另一个条件，所以可以一层一层拼接。插件会拦住循环引用，避免无限递归。

## 🛠️ 命令与权限

| 命令 | 作用 | 权限 |
| --- | --- | --- |
| `/displaynamefmt reload` | 重新读取配置并刷新在线玩家 | `displaynamefmt.reload` |

`displaynamefmt.reload` 默认只给 OP。

## 📚 文档

| 文档 | 内容 |
| --- | --- |
| [配置说明](docs/configuration.md) | 每个配置项、颜色和完整示例 |
| [条件表达式](docs/conditions.md) | 运算符、AND / OR、权限和递归拼接 |
| [前缀与后缀](docs/prefix-suffix.md) | 自有 prefix/suffix、LuckPerms 输入和名称牌用法 |
| [配置迁移](docs/migration.md) | `config-version`、自动补全和旧配置升级 |
| [常见问题](docs/troubleshooting.md) | 变量不解析、颜色不显示、重载失败等问题 |
| [开发与构建](docs/development.md) | 本地构建、测试和项目结构 |

## 🧱 自己构建

Windows：

```powershell
.\gradlew.bat clean test build
```

Linux / macOS：

```bash
./gradlew clean test build
```

构建完成后，插件位于：

```text
build/libs/DisplayNameFmt-1.0-SNAPSHOT-all.jar
```

## 🤝 参与开发

欢迎提交问题和改进。提交代码前，请先运行测试，确认构建可以通过。详细说明见[开发与构建](docs/development.md)。

## 📄 开源协议

Copyright © 2026 RimuruChan。

本项目使用 [GNU General Public License v3.0](LICENSE) 开源，对应 SPDX 标识 `GPL-3.0-only`。

你可以使用、修改和分发本项目，但分发修改版时也需要按 GPLv3 开源，并保留相同的许可证和版权说明。
