# ListTools - Velocity 白名单系统插件

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Velocity](https://img.shields.io/badge/Velocity-3.3.0+-green.svg)

一个功能完整的 Velocity 代理服务器白名单系统插件，提供严格的玩家访问控制和管理功能。
**注意**：本插件由 AI 开发。

## 功能特性

### 🔐 白名单系统
- **严格匹配**：玩家名区分大小写，确保安全性
- **实时检查**：玩家连接时即时验证白名单状态
- **自动踢出**：未授权玩家自动被拒绝连接

### ⚙️ 配置管理
- **YAML配置**：使用标准YAML格式，易于编辑
- **热重载**：支持运行时重载配置文件
- **默认配置**：首次运行自动生成默认配置

### 🎮 命令系统
- **完整命令**：支持添加、删除、查看白名单
- **权限控制**：管理员权限保护
- **命令别名**：支持多种命令别名
- **自动补全**：智能命令参数补全

### ⏰ 定时检查
- **自动检查**：定期检查在线玩家白名单状态
- **可配置间隔**：支持秒、分钟、小时单位
- **手动检查**：支持手动触发检查

## 安装说明

1. 下载 `ListTools-1.1-SNAPSHOT.jar` 文件
2. 将文件放入 Velocity 服务器的 `plugins` 目录
3. 重启 Velocity 服务器
4. 插件将自动生成配置文件 `plugins/listtools/config.yml`

## 配置文件

```yaml
# 是否启用白名单系统
enabled: true

# 玩家没有白名单时的踢出消息
kick_message: "你还没有白名单！快去服务器群里申请吧！(｀・ω・´)"

# 自动检查间隔时间（支持 30s, 5m, 1h 格式）
auto_check: "1h"

# 白名单玩家列表
whitelist:
  - "Player1"
  - "Player2"
  - "Player3"
```

## 命令使用

### 主命令
- `/listtools` - 显示帮助信息
- `/lt` - 主命令别名
- `/whitelist` - 主命令别名

### 白名单管理
```bash
# 添加玩家到白名单
/listtools whitelist add <玩家名>

# 从白名单移除玩家
/listtools whitelist remove <玩家名>

# 查看白名单列表
/listtools whitelist list
```

### 系统管理
```bash
# 重载配置文件
/listtools reload

# 查看插件状态
/listtools status
```

## 权限系统

### 权限节点
- `listtools.admin` - 管理员权限，允许使用所有命令
- 控制台默认拥有所有权限

### 权限配置
```bash
# LuckPerms 示例
/lp user <玩家名> permission set listtools.admin true
/lp group admin permission set listtools.admin true
```

### 权限保护功能
- ✅ 所有命令都需要权限验证
- ✅ Tab补全根据权限智能过滤
- ✅ 无权限玩家会收到明确的拒绝提示
- ✅ 支持所有命令别名：`/listtools`, `/lt`, `/whitelist`

详细权限配置请参考 [PERMISSIONS.md](PERMISSIONS.md)

## 技术特性

### 🔧 技术栈
- **Java 17** - 现代Java版本支持
- **Velocity API 3.3.0** - 最新Velocity API
- **SnakeYAML 2.0** - YAML配置文件处理
- **Adventure API** - 现代文本组件系统

### 🚀 性能优化
- **线程安全**：使用读写锁保证并发安全
- **内存缓存**：白名单数据缓存，减少IO操作
- **异步处理**：非阻塞的命令处理
- **批量操作**：支持批量添加/删除玩家

### 🛡️ 安全特性
- **严格匹配**：玩家名完全匹配，防止绕过
- **权限控制**：命令权限保护
- **错误处理**：完善的异常处理机制
- **日志记录**：详细的操作日志

## 开发信息

### 构建项目
```bash
# 编译项目
mvn clean compile

# 打包插件
mvn package
```

### 项目结构
```
src/main/java/org/plugin/listtools/
├── ListTools.java              # 主插件类
├── ConfigManager.java          # 配置管理器
├── WhitelistManager.java       # 白名单管理器
├── PlayerConnectionListener.java # 连接事件监听器
├── ListToolsCommand.java       # 命令处理器
└── AutoCheckTask.java          # 自动检查任务
```

## 更新日志

### v1.1 (2025-10-07)
- 🐛 **修复插件加载错误**: 解决了 "plugin is not registered" 错误
- 🔧 **调度器优化**: 修复了自动检查任务的调度器注册问题
- 📄 **许可证添加**: 添加了完整的 MIT 开源许可证
- 📚 **文档完善**: 新增权限配置和使用示例文档
- ✨ **Tab补全增强**: 优化了命令补全的权限过滤和智能匹配

### v1.0-SNAPSHOT (2025-10-06)
- ✅ 基础白名单系统
- ✅ 完整命令系统
- ✅ 自动检查功能
- ✅ 配置文件管理
- ✅ 权限控制
- ✅ 多语言支持

## 支持与反馈

如果您在使用过程中遇到问题或有改进建议，请通过以下方式联系：

- 创建 Issue 报告问题
- 提交 Pull Request 贡献代码
- 在服务器群里反馈使用体验

## 许可证

本项目采用 [MIT 许可证](LICENSE) 开源发布。

```
MIT License

Copyright (c) 2025 NSrank & Augment

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

**作者**: NSrank, Augment  
**版本**: 1.1-SNAPSHOT  
**兼容性**: Velocity 3.3.0+, Java 17+
