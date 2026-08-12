# 🩺 常见问题

[返回 README](../README.md) · [配置说明](configuration.md) · [条件表达式](conditions.md)

## 🧩 PAPI 变量原样显示

例如游戏里直接出现：

```text
%player_name%
```

请按顺序检查：

1. PlaceholderAPI 是否正常启用。
2. 提供这个变量的插件或 PAPI 扩展是否已经安装。
3. 用 PlaceholderAPI 自己的解析命令测试同一个变量。
4. 安装扩展后是否执行过 `/papi reload`。
5. 变量名是否拼错。

DisplayNameFmt 直接使用 PlaceholderAPI 的解析结果。PAPI 无法解析的变量，DisplayNameFmt 也无法补出来。

## 🎨 颜色代码没有变成颜色

支持的写法是：

```text
&a绿色
&#55ff99真彩色
```

常见错误：

- 把 RGB 写成 `#55ff99`，前面漏了 `&`
- RGB 不是完整的 6 位十六进制颜色
- 其他插件再次处理显示名时去掉了颜色
- 查看位置根本没有使用 Paper 的玩家显示名

## 🏷️ 头顶名称或 Tab 列表没有变化

这是正常情况。DisplayNameFmt 设置的是 Paper 的玩家显示名，不是玩家真实名称、头顶名称或 Tab 列表名称。

要修改这些位置，需要对应的名称牌或 Tab 插件。如果它支持 PlaceholderAPI，可以让它读取 `%displaynamefmt_prefix%` 和 `%displaynamefmt_suffix%`。配置方法见[前缀与后缀](prefix-suffix.md)。

## 🪪 Prefix/suffix 没有效果

请检查：

- `display-name.prefix` 和 `display-name.suffix` 是否填写正确
- 用 PAPI 测试时，`%displaynamefmt_prefix%` 和 `%displaynamefmt_suffix%` 是否能正常解析
- 名称牌插件是否支持 PlaceholderAPI
- 是否已经在名称牌插件中填写这两个变量
- 名称牌插件是否会刷新 PAPI 变量

DisplayNameFmt 不会写入 Vault 或 LuckPerms，也不负责发送名称牌数据包。更多说明见[前缀与后缀](prefix-suffix.md)。

## 🔁 显示名被改回去了

通常是另一个插件也在设置玩家显示名。可以检查：

- 聊天格式插件
- 权限组前缀插件
- Tab 或名称牌插件
- 其他昵称、伪装或显示名插件

尝试临时停用这些插件，找出是谁覆盖了结果。DisplayNameFmt 会按 `refresh-interval-ticks` 定时更新，但两个插件互相覆盖不是稳定的解决办法。

## ❓ 条件一直走 `false`

先检查：

- PAPI 变量是否真的解析成了你以为的内容
- 文字大小写是否完全一致
- 数字两边是否都是有效数字
- 权限节点是否正确
- 一行里是否混用了 `;` 和 `|`

文字比较区分大小写，也不会自动去掉颜色代码和空格。

## ❌ 重载失败

执行 `/displaynamefmt reload` 后，插件会直接告诉你失败原因。常见原因有：

- `display-name.format` 是空字符串
- `refresh-interval-ticks` 小于 1 或不是整数
- 条件名包含大写字母或其他不允许的字符
- 条件列表为空
- 条件表达式不完整
- `type` 不是 `AND` 或 `OR`
- 配置版本比插件支持的版本更高

热重载失败不会替换当前正在使用的配置。修正文件后可以再次重载。

## 🔄 出现循环条件警告

控制台可能显示类似：

```text
Cyclic display-name condition: a -> b -> a
```

这表示条件互相引用形成了一个圈。插件会把这次结果当作空文字，避免无限递归。

打开配置，顺着警告里的条件名检查 `true` 和 `false`，删除其中一条反向引用即可。

## ⏱️ 修改后没有立即生效

先执行：

```text
/displaynamefmt reload
```

还要确认执行者拥有：

```text
displaynamefmt.reload
```

如果命令成功，插件会立刻刷新所有在线玩家。之后也会按配置的间隔继续刷新。
