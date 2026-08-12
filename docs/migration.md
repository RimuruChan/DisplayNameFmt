# 🔄 配置迁移

[返回 README](../README.md) · [配置说明](configuration.md) · [常见问题](troubleshooting.md)

DisplayNameFmt 用 `config-version` 识别配置文件结构。当前配置版本是：

```yaml
config-version: 1
```

## 🤖 插件会自动做什么

启动或执行 `/displaynamefmt reload` 时，插件会：

1. 没有配置文件时，创建一份默认配置。
2. 配置版本较旧时，按顺序迁移到当前版本。
3. 从默认配置补上缺少的新配置项。
4. 尽量保留你原来的值、顺序和注释。
5. 先写入临时文件，再替换原文件，降低写到一半损坏的风险。

控制台会说明是否发生了迁移，以及补上了哪些配置项。

## ⬆️ 从 v0 升级到 v1

没有 `config-version` 的配置会被当作 v0。

旧写法：

```yaml
format: '&f%player_name%'
refresh-interval-ticks: 20
conditions: {}
```

迁移后的写法：

```yaml
config-version: 1

display-name:
  format: '&f%player_name%'
  refresh-interval-ticks: 20

conditions: {}
```

插件会把根目录下的 `format` 和 `refresh-interval-ticks` 移到 `display-name` 中。

如果新位置已经有值，会优先保留新位置的值，不用旧值覆盖它。

## 🚧 配置来自更高版本

如果配置文件的 `config-version` 比当前插件支持的版本更高，插件会拒绝读取。

这样做是为了避免旧版插件误改新版配置。解决方法是：

1. 安装与这份配置匹配的新版插件；或
2. 恢复这版插件对应的配置备份。

不要只把版本号手动改小。配置结构可能真的不同，硬改数字可能让配置被错误读取。

## ❌ 迁移失败会怎样

- 启动时配置无效：插件会输出错误并停止启用
- 热重载时配置无效：重载失败，当前正在使用的配置保持不变

修正控制台提示的配置项后，再执行：

```text
/displaynamefmt reload
```

## 💾 升级建议

自动迁移会处理已知结构，但升级前仍建议备份：

```text
plugins/DisplayNameFmt/config.yml
```

尤其是一次跨多个插件版本升级时，备份能让你随时回到原来的配置。
