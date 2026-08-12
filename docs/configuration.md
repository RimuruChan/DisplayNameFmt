# ⚙️ 配置说明

[返回 README](../README.md) · [条件表达式](conditions.md) · [配置迁移](migration.md)

配置文件位于：

```text
plugins/DisplayNameFmt/config.yml
```

第一次启动时，插件会自动创建这个文件。修改后执行 `/displaynamefmt reload`，不需要重启服务端。

## 🧾 完整示例

```yaml
config-version: 1

display-name:
  format: '%displaynamefmt_condition_staff-prefix%&f%player_name% &8(&7%player_world%&8)'
  refresh-interval-ticks: 20

conditions:
  staff-prefix:
    conditions:
      - 'permission:group.staff'
    type: AND
    true: '&#ff5555[管理员] '
    false: '&7'
```

## 🧭 配置项

### `config-version`

配置文件版本。插件用它判断是否需要迁移旧配置。

请不要为了“看起来更新”而手动增加这个数字。插件升级后会在需要时自动处理。更多说明见[配置迁移](migration.md)。

### `display-name.format`

玩家显示名的最终格式。

这个字符串会直接交给 PlaceholderAPI 解析，所以可以直接写任何已经可用的 PAPI 变量：

```yaml
display-name:
  format: '&7[&#55ff99%player_world%&7] &f%player_name%'
```

DisplayNameFmt 自己只提供命名条件变量：

```text
%displaynamefmt_condition_<条件名>%
```

如果要做权限、延迟、世界等判断，请看[条件表达式](conditions.md)。

### `display-name.refresh-interval-ticks`

重新计算所有在线玩家显示名的间隔，单位是 tick。

- `20` tick 大约是 1 秒
- 最小值是 `1`
- 间隔越短，变量变化显示得越快，但计算次数也越多

一般保持 `20` 就够用。如果显示名中的内容很少变化，可以适当调大。

玩家进入服务器、切换世界和重生后，插件也会主动刷新一次，不用等待完整间隔。

### `conditions`

命名条件列表。没有条件时写成：

```yaml
conditions: {}
```

每个条件可以设置：

| 配置项 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `conditions` | 是 | 无 | 要判断的表达式，可以是一条或多条 |
| `type` | 否 | `AND` | 多条表达式之间使用 `AND` 还是 `OR` |
| `true` | 否 | `'true'` | 条件成立时输出的文字 |
| `false` | 否 | `'false'` | 条件不成立时输出的文字 |

条件名只能使用小写字母、数字、下划线和短横线，并且必须以字母或数字开头。

## 🎨 颜色写法

传统颜色和格式：

```text
&0 &1 &2 &3 &4 &5 &6 &7 &8 &9
&a &b &c &d &e &f
&k &l &m &n &o &r
```

RGB 真彩色：

```text
&#RRGGBB
```

例如：

```yaml
display-name:
  format: '&#55ff99&l%player_name%'
```

## 🧩 PlaceholderAPI 变量

DisplayNameFmt 不会自己冒充其他插件的变量。变量由 PlaceholderAPI 和对应扩展提供。

例如，使用某个扩展前通常需要先安装它：

```text
/papi ecloud download <扩展名>
/papi reload
```

具体要装哪个扩展，请查看变量提供方的说明。可以先用 PlaceholderAPI 自己的解析命令测试变量；如果在那里都不能正常解析，DisplayNameFmt 也无法解析。

## 👀 显示名的作用范围

插件设置的是 Paper API 中的玩家 `displayName`：

- 不会修改玩家的真实账号名
- 不会直接修改玩家头顶名称
- 不会直接修改 Tab 列表名称
- 聊天插件是否显示它，取决于聊天插件是否读取 Paper 的显示名

如果你同时安装了其他显示名插件，后写入显示名的插件可能覆盖先写入的结果。
