# 🪪 前缀与后缀

[返回 README](../README.md) · [配置说明](configuration.md) · [条件表达式](conditions.md) · [常见问题](troubleshooting.md)

DisplayNameFmt 有自己的 prefix 和 suffix。它们只是两段可重复使用的动态格式，不会修改 Vault、LuckPerms 或玩家真实名称。

## 🚀 基本配置

```yaml
display-name:
  format: '%displaynamefmt_prefix%%player_name%%displaynamefmt_suffix%'
  prefix: '&c[管理员] '
  suffix: ' &8[%player_world%]'
  refresh-interval-ticks: 20
```

对应的 PAPI 变量是：

```text
%displaynamefmt_prefix%
%displaynamefmt_suffix%
```

`display-name.format` 是否使用这两个变量由你决定。它们也可以单独交给其他支持 PlaceholderAPI 的插件。

## 🍀 使用 LuckPerms 前后缀

如果服务器已经提供 LuckPerms 的 PAPI 变量，可以直接把它们当作输入：

```yaml
display-name:
  format: '%displaynamefmt_prefix%%player_name%%displaynamefmt_suffix%'
  prefix: '%luckperms_prefix%'
  suffix: '%luckperms_suffix%'
  refresh-interval-ticks: 20
```

也可以继续拼接自己的内容：

```yaml
display-name:
  prefix: '%luckperms_prefix%&#55ff99[在线] '
  suffix: '%luckperms_suffix% &8[%player_world%]'
```

这里只会通过 PlaceholderAPI 读取 LuckPerms 的结果，不会把内容写回 LuckPerms，所以不会覆盖或污染原来的 prefix/suffix。

如果 `%luckperms_prefix%` 原样显示，请先安装并启用提供这些变量的 PlaceholderAPI 扩展。

## 🧩 使用条件

prefix 和 suffix 可以使用命名条件：

```yaml
display-name:
  format: '%displaynamefmt_prefix%%player_name%%displaynamefmt_suffix%'
  prefix: '%displaynamefmt_condition_staff-prefix%'
  suffix: '%displaynamefmt_condition_ping-suffix%'
  refresh-interval-ticks: 20

conditions:
  staff-prefix:
    conditions:
      - 'permission:group.staff'
    true: '&#ff5555[管理员] '
    false: '&7'

  ping-suffix:
    conditions:
      - '%player_ping%<100'
    true: ' &a●'
    false: ' &c●'
```

条件输出也可以继续引用其他条件。详细语法见[条件表达式](conditions.md)。

## 🏷️ 给名称牌或 Tab 插件使用

在支持 PlaceholderAPI 的名称牌、Tab 或聊天插件中填写：

```text
prefix: %displaynamefmt_prefix%
suffix: %displaynamefmt_suffix%
```

具体配置位置由对方插件决定。DisplayNameFmt 只负责返回解析后的字符串，不会主动发送名称牌或 Tab 数据包。

如果对方插件只支持 Vault、不支持 PlaceholderAPI，这两个变量就无法直接接入，需要换用它支持的占位符功能或其他桥接方式。

## 🎨 颜色

在 Paper 显示名中，DisplayNameFmt 会解析传统颜色和 RGB：

```text
&c红色
&#55ff99真彩色
```

其他插件读取 prefix/suffix 时，拿到的是包含这些颜色代码的字符串。最终如何显示，取决于对方插件是否支持对应的颜色写法。

## ♻️ 递归与循环保护

prefix、suffix 和命名条件可以互相拼接，但不要直接引用自己：

```yaml
display-name:
  prefix: '%displaynamefmt_prefix%'
```

也不要让 prefix 和 suffix 互相引用形成一圈。插件检测到循环时会返回空字符串，并在控制台记录一次警告，避免无限递归。
