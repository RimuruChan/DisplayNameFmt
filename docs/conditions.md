# 🧩 条件表达式

[返回 README](../README.md) · [配置说明](configuration.md) · [常见问题](troubleshooting.md)

命名条件可以根据 PAPI 变量或玩家权限输出不同文字。输出结果可以是颜色、前后缀，也可以继续引用另一个命名条件。

## 🚀 最小示例

```yaml
display-name:
  format: '%displaynamefmt_condition_ping-color%%player_name%'
  refresh-interval-ticks: 20

conditions:
  ping-color:
    conditions:
      - '%player_ping%<100'
    true: '&a'
    false: '&c'
```

## 🔣 运算符

表达式左右两边都会先解析 PAPI 变量。

### 数字比较

| 运算符 | 含义 | 示例 |
| --- | --- | --- |
| `>=` | 大于或等于 | `%player_ping%>=100` |
| `>` | 大于 | `%player_level%>30` |
| `<=` | 小于或等于 | `%player_ping%<=50` |
| `<` | 小于 | `%player_ping%<100` |

数字两边允许有空格。只要有一边不是有效数字，这次判断就会返回“不成立”。

### 文字比较

| 运算符 | 含义 | 示例 |
| --- | --- | --- |
| `=` | 等于 | `%player_world%=world` |
| `!=` | 不等于 | `%player_world%!=world_nether` |
| `<-` | 左边包含右边 | `%player_world%<-lobby` |
| `!<-` | 左边不包含右边 | `%player_world%!<-test` |
| `|-` | 左边以右边开头 | `%player_world%|-lobby_` |
| `!|-` | 左边不以右边开头 | `%player_world%!|-test_` |
| `-|` | 左边以右边结尾 | `%player_world%-|nether` |
| `!-|` | 左边不以右边结尾 | `%player_world%!-|the_end` |

文字比较区分大小写，也不会自动去掉颜色代码或两边空格。

### 权限判断

```text
permission:group.staff
!permission:group.staff
```

- `permission:`：玩家有这个权限时成立
- `!permission:`：玩家没有这个权限时成立

权限名本身也可以包含 PAPI 变量。

## 🔗 一行里写 AND 或 OR

用分号 `;` 表示 AND，也就是每一项都要成立：

```yaml
conditions:
  fast-staff:
    conditions:
      - 'permission:group.staff;%player_ping%<100'
    true: '&a'
    false: '&7'
```

用竖线 `|` 表示 OR，也就是任意一项成立即可：

```yaml
conditions:
  lobby:
    conditions:
      - '%player_world%=lobby|%player_world%=lobby_nether'
    true: '&a大厅'
    false: '&7其他'
```

同一行不能同时混用 `;` 和 `|`。需要更复杂的组合时，把判断拆成多个命名条件。

## 🗂️ 多行条件

`type` 决定 `conditions` 列表里的多行如何组合：

```yaml
conditions:
  staff-in-lobby:
    type: AND
    conditions:
      - 'permission:group.staff'
      - '%player_world%=lobby'
    true: '&#ff5555[值班] '
    false: ''
```

- `AND`：所有行都成立
- `OR`：任意一行成立
- 不写 `type` 时默认是 `AND`

## 🧱 在格式中使用条件

条件 `staff-prefix` 对应：

```text
%displaynamefmt_condition_staff-prefix%
```

完整示例：

```yaml
display-name:
  format: '%displaynamefmt_condition_staff-prefix%%player_name%'

conditions:
  staff-prefix:
    conditions:
      - 'permission:group.staff'
    true: '&#ff5555[管理员] '
    false: '&7'
```

## ♻️ 递归拼接

`true` 和 `false` 的输出会再次经过 PlaceholderAPI 解析，所以一个条件可以引用另一个条件：

```yaml
display-name:
  format: '%displaynamefmt_condition_ping-good%%player_name%'

conditions:
  ping-good:
    conditions:
      - '%player_ping%<50'
    true: '&a'
    false: '%displaynamefmt_condition_ping-medium%'

  ping-medium:
    conditions:
      - '%player_ping%<100'
    true: '&e'
    false: '%displaynamefmt_condition_ping-bad%'

  ping-bad:
    conditions:
      - '%player_ping%>=100'
    true: '&c'
    false: '&7'
```

效果：

- 延迟低于 50：绿色
- 延迟低于 100：黄色
- 延迟达到 100：红色
- 延迟不是有效数字：灰色

## 🛑 循环引用

不要让 A 引用 B，同时 B 又引用 A：

```yaml
conditions:
  a:
    conditions: ['1=1']
    true: '%displaynamefmt_condition_b%'

  b:
    conditions: ['1=1']
    true: '%displaynamefmt_condition_a%'
```

插件发现循环后会返回空文字，并在控制台记录警告，不会无限递归。

## ⚠️ 容易踩的坑

- 条件名只能写小写字母、数字、`_` 和 `-`
- 条件名必须以字母或数字开头
- `conditions` 不能为空
- 数字比较失败时不会自动改成文字比较
- 一行短表达式不能混用 AND 和 OR
- PAPI 变量没有正确解析时，判断结果通常也不会符合预期
