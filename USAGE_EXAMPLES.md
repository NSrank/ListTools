# ListTools 使用示例

## 基础使用流程

### 1. 安装插件
```bash
# 将 ListTools-1.0-SNAPSHOT.jar 放入 plugins 目录
# 重启 Velocity 服务器
# 插件会自动生成配置文件
```

### 2. 配置权限
```bash
# 给予管理员权限（LuckPerms示例）
/lp user Admin permission set listtools.admin true
/lp group moderator permission set listtools.admin true
```

### 3. 基础白名单管理

#### 添加玩家到白名单
```bash
# 控制台执行
listtools whitelist add Steve
listtools whitelist add Alex

# 游戏内执行（需要权限）
/lt whitelist add Notch
/whitelist add Herobrine
```

#### 查看白名单
```bash
# 控制台
listtools whitelist list

# 游戏内
/lt whitelist list
```

#### 移除玩家
```bash
# 控制台
listtools whitelist remove Steve

# 游戏内
/lt whitelist remove Alex
```

## 高级管理功能

### 查看插件状态
```bash
# 控制台
listtools status

# 游戏内
/lt status
```

输出示例：
```
=== ListTools 状态 ===
白名单系统: 启用
白名单玩家数量: 5
自动检查间隔: 1小时
在线玩家数量: 3
```

### 重载配置文件
```bash
# 修改配置文件后重载
listtools reload
/lt reload
```

## Tab补全示例

### 命令补全
```bash
# 输入 /lt 然后按 Tab
/lt <Tab>
# 显示: whitelist reload status

# 输入 /lt w 然后按 Tab
/lt w<Tab>
# 自动补全为: /lt whitelist
```

### 子命令补全
```bash
# 输入 /lt whitelist 然后按 Tab
/lt whitelist <Tab>
# 显示: add remove list

# 输入 /lt whitelist a 然后按 Tab
/lt whitelist a<Tab>
# 自动补全为: /lt whitelist add
```

### 玩家名补全
```bash
# 添加玩家时补全在线玩家（排除已在白名单的）
/lt whitelist add <Tab>
# 显示当前在线但不在白名单的玩家

# 移除玩家时补全白名单玩家
/lt whitelist remove <Tab>
# 显示当前白名单中的所有玩家
```

## 配置文件示例

### 基础配置
```yaml
# config.yml
enabled: true
kick_message: "你还没有白名单！快去服务器群里申请吧！(｀・ω・´)"
auto_check: "1h"
whitelist:
  - "Steve"
  - "Alex"
  - "Notch"
```

### 自定义踢出消息
```yaml
kick_message: "抱歉，您需要白名单才能进入服务器！请联系管理员申请。"
```

### 调整检查间隔
```yaml
# 每30秒检查一次
auto_check: "30s"

# 每5分钟检查一次
auto_check: "5m"

# 每2小时检查一次
auto_check: "2h"
```

## 实际使用场景

### 场景1：新服务器开荒
```bash
# 1. 启用白名单系统
# 2. 添加核心玩家
/lt whitelist add Owner
/lt whitelist add Admin1
/lt whitelist add Admin2

# 3. 查看当前白名单
/lt whitelist list

# 4. 设置较短的检查间隔
# 编辑 config.yml: auto_check: "5m"
/lt reload
```

### 场景2：活动期间临时开放
```bash
# 1. 临时禁用白名单
# 编辑 config.yml: enabled: false
/lt reload

# 2. 活动结束后重新启用
# 编辑 config.yml: enabled: true
/lt reload

# 3. 检查并清理未授权玩家
/lt status
```

### 场景3：批量管理白名单
```bash
# 1. 直接编辑配置文件添加多个玩家
# config.yml:
# whitelist:
#   - "Player1"
#   - "Player2"
#   - "Player3"
#   - "Player4"
#   - "Player5"

# 2. 重载配置
/lt reload

# 3. 验证白名单
/lt whitelist list
```

## 故障排除

### 权限问题
```bash
# 问题：玩家无法使用命令
# 解决：检查权限配置
/lp user <玩家名> info
/lp user <玩家名> permission set listtools.admin true
```

### 配置问题
```bash
# 问题：配置修改不生效
# 解决：重载配置
/lt reload

# 问题：白名单不工作
# 解决：检查配置文件
/lt status
```

### 性能问题
```bash
# 问题：服务器卡顿
# 解决：调整检查间隔
# 编辑 config.yml: auto_check: "1h"  # 增加间隔
/lt reload
```

## 最佳实践

1. **权限管理**
   - 只给可信任的管理员权限
   - 定期审查权限列表
   - 使用权限组管理

2. **白名单维护**
   - 定期清理不活跃玩家
   - 备份白名单配置
   - 记录添加/移除原因

3. **性能优化**
   - 根据服务器规模调整检查间隔
   - 监控插件日志
   - 避免频繁重载配置

4. **安全考虑**
   - 使用强踢出消息提醒玩家申请流程
   - 监控未授权连接尝试
   - 配合其他安全插件使用
