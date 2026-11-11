# 校园二手闲置交易平台（campus-secondhand）

> 《Spring Boot 企业级项目开发》期末综合大作业 · 选题一 · 高阶级（第 1-10 章全技术栈）

面向校园场景的 C2C 二手闲置交易平台，提供 **商品发布 → 平台审核 → 检索浏览 → 在线下单 → 支付确认 → 收货完成 → 超时自动取消** 的完整业务闭环，并配套卖家中心与管理员数据看板。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 语言 / 框架 | Java 17 · Spring Boot 3.3.5（兼容课程 2.7+ 规范）· Spring MVC |
| 视图 | Thymeleaf 3.1（服务端渲染，未使用 Vue/React） |
| 持久层 | MyBatis-Plus 3.5.7 · HikariCP |
| 数据库 | MySQL 8.0.46（utf8mb4） |
| 缓存 | Redis 5（商品详情缓存 + 缓存三防） |
| 安全 | Spring Security 6（BCrypt、三级角色、URL + 方法级鉴权、CSRF） |
| 消息 | RabbitMQ 4.4（TTL + DLX 延迟队列、异步邮件、状态推送） |
| 定时 / 邮件 | Spring Task · JavaMailSender |
| 可视化 | ECharts 5.5（本地化引入） |

## 快速开始

```bash
# 1. 初始化数据库（创建库、账号、表与演示数据）
mysql -uroot -p < sql/db_init_user.sql      # 创建应用专用账号 campus/campus123
mysql -ucampus -pcampus123 < sql/campus_secondhand.sql   # 建库建表 + 演示数据

# 2. 确保 Redis 已启动
redis-cli ping        # 期望 PONG

# 3. （可选）启动 RabbitMQ，未启动时超时取消由定时任务兜底
net start RabbitMQ

# 4. 打包并运行
mvn clean package -DskipTests
java -jar target/campus-secondhand.jar --spring.profiles.active=dev

# 5. 浏览器访问
http://localhost:8080
```

## 演示账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员（看板 / 审核 / 用户 / 订单） |
| seller01 | 123456 | 认证卖家（张三） |
| seller02 | 123456 | 认证卖家（李四） |
| user01 | 123456 | 普通用户（王五） |
| user02 | 123456 | 普通用户（赵六） |

## 功能地图

- **前台**：首页（分类 / 热门 / 最新）、商品列表（筛选 / 搜索 / 排序 / 分页）、商品详情（多图 / 下单 / 留言）、发布闲置、个人中心、我的订单、想要清单、最近浏览
- **卖家中心**：经营概览、我的发布（上下架 / 删除）、卖出订单（确认订单）、留言回复
- **后台**：数据看板（成交趋势折线 / 分类圆环 / 订单状态漏斗 / 卖家排行）、商品审核（通过 / 驳回）、用户管理（禁用 / 启用 / 升级卖家）、订单管理（详情 / 强制取消）、定时任务一键演示

## 技术亮点

1. **防超卖**：`UPDATE item SET deal_status=2 WHERE deal_status=1 AND status=1` 乐观抢占，影响行数为 0 即抢购失败。
2. **超时取消双保险**：RabbitMQ TTL + 死信交换机实现 30 分钟延迟取消；Spring Task 每 5 分钟兜底扫描。
3. **缓存三防**：null 标记防穿透、SETNX 互斥锁防击穿、TTL 随机抖动防雪崩，且全部异常降级查库。
4. **消息可靠性**：Jackson2JsonMessageConverter 统一序列化 + 显式解析，消费失败只告警不抛异常；事务提交后（afterCommit）才发消息。
5. **安全体系**：三级角色叠加 + URL 级与方法级双重鉴权 + CSRF + XSS/注入防护 + BCrypt + 会话单点控制。

## 目录结构

```
src/main/java/com/campus/shop
├── common/      统一返回体、业务异常、全局异常处理、缓存 key、常量
├── config/      Security / RabbitMQ / Redis / MyBatis-Plus / WebMvc 配置
├── controller/  8 个控制器（Home/Item/Order/User/Seller/Admin/Upload/Api）
├── entity/      9 个与表对应的实体
├── mapper/      9 个 Mapper（含自定义 SQL）
├── dto/         表单与查询对象
├── service/     业务接口与实现
├── mq/          消息生产者、消费者、消息体
├── task/        定时任务
└── util/        订单号生成
src/main/resources
├── templates/   20 个 Thymeleaf 模板
├── static/      css / js / images
└── application*.yml  dev / test / prod 多环境配置
```

## 验证与答辩演示

```powershell
# 1. 启动自检：应用启动后日志会打印数据库 / Redis 状态
#    ================ 启动自检开始（profile = dev）================
#    [自检-数据库] 连接正常，表数量 = 9，用户 8 人，在售商品 38 件，订单 25 笔
#    [自检-Redis] 读写正常

# 2. 超时取消端到端验证（自动下单 -> 模拟超时 -> 触发扫描 -> 校验订单与商品状态）
powershell -ExecutionPolicy Bypass -File docs\verify_timeout.ps1
```

答辩演示顺序建议：admin 看板（定时任务一键演示）→ 卖家发布 → admin 审核 → 买家下单支付 → 卖家确认 → 买家收货 → 看板数据变化。

## 交付文档

| 文档 | 说明 |
| --- | --- |
| `校园二手闲置交易平台_开发文档.docx` | 需求、设计、数据库、技术实现、页面、测试、分工、评分对照 |
| `校园二手闲置交易平台_部署文档.docx` | Windows 开发环境与 Linux + Nginx 生产部署、常见问题排错 |
| `校园二手闲置交易平台_源码核心代码说明.docx` | 65 个 Java 类全文与关键 SQL |
| `校园二手闲置交易平台_答辩PPT.pptx` | 12 页答辩演示稿 |
| `校园二手闲置交易平台_数据库设计.xlsx` | 9 张表结构设计与索引说明 |

文档目录：`D:\projects\campus-secondhand-docs`

## Git 提交规范

```
feat:     新功能
fix:      修复缺陷
refactor: 重构（不改行为）
docs:     文档
style:    格式调整
chore:    构建与杂项
```
