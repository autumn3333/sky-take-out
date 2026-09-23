# 🥡 sky-take-out —— 苍穹外卖

一个外卖点餐系统的后端服务，采用 Maven 多模块架构，同时支撑**管理端**（员工、分类、菜品、套餐、订单、数据报表、工作台）与**用户端**（微信登录、浏览下单、购物车、地址簿、支付）两条业务线。

> 服务端口 `8080` · Spring Boot 2.7.3 · JDK 1.8 · 模块 `sky-common` / `sky-pojo` / `sky-server`

## 功能模块

### 管理端

| 模块 | 说明 |
|---|---|
| 员工管理 | 登录 / 退出、新增、分页查询、启用禁用、编辑；密码 MD5 加密存储，账号被锁定的异常处理 |
| 分类管理 | 新增、分页查询、删除（有菜品或套餐关联时禁止删除）、修改、启用禁用、按类型查询 |
| 菜品管理 | 新增（含口味）、分页查询、批量删除、查询回显、修改、起售停售、按分类查询 |
| 套餐管理 | 新增、分页查询、删除、查询回显、修改、起售停售 |
| 订单管理 | 条件搜索、各状态统计、订单详情、接单、拒单、取消、派送、完成 |
| 数据报表 | 营业额统计、用户统计、订单统计、销量 Top10 |
| 工作台 | 今日运营数据、订单 / 菜品 / 套餐总览 |
| 店铺状态 | 设置与查询营业状态（存于 Redis） |
| 通用接口 | 图片上传（阿里云 OSS） |

### 用户端

| 模块 | 说明 |
|---|---|
| 微信登录 | 基于微信小程序 `code` 换取 openid，首次登录自动注册 |
| 店铺 / 菜品浏览 | 查询营业状态、按分类查询菜品与套餐、套餐内菜品明细 |
| 购物车 | 添加、减少、查询、清空 |
| 地址簿 | 新增、查询、修改、删除、设置默认地址 |
| 下单与支付 | 提交订单、微信支付、支付回调；超时未支付自动取消 |
| 历史订单 | 分页查询、订单详情、重复下单、取消订单、催单 |

## 技术栈

| 层 | 技术 |
|---|---|
| 框架 | Spring Boot 2.7.3（JDK 1.8），Maven 多模块 |
| 持久层 | MyBatis + MySQL 8，Druid 连接池，PageHelper 分页 |
| 缓存 | Spring Cache + Redis |
| 认证 | JWT（jjwt），管理端与用户端双拦截器 |
| 实时通信 | WebSocket（来单语音提醒） |
| 定时任务 | Spring Scheduling（超时订单处理） |
| 外部服务 | 阿里云 OSS（文件上传）、微信支付 APIv3、微信小程序登录 |
| 报表导出 | Apache POI |
| 接口文档 | Knife4j（Swagger 2） |

## 模块划分

```
sky-take-out/                # 父工程，统一依赖版本管理
├── sky-common/              # 通用层：常量、异常、上下文、JSON 处理、JWT 工具、OSS 工具、微信支付工具
├── sky-pojo/                # 实体层：entity / dto / vo
└── sky-server/              # 服务层：controller / service / mapper / config / interceptor / websocket / task
```

依赖方向单向：`sky-server` → `sky-pojo` / `sky-common`，公共层不反向依赖服务层。

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7 / 8.0
- Redis 5.0+（存储店铺营业状态）

## 快速开始

### 1. 初始化数据库

> ⚠️ 本仓库**未包含建表 SQL 脚本**，需要先自行准备 `sky_take_out` 库及其数据表（`employee`、`category`、`dish`、`dish_flavor`、`setmeal`、`setmeal_dish`、`orders`、`order_detail`、`shopping_cart`、`address_book`、`user` 等）。

```sql
CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4;
```

### 2. 设置第三方服务密钥（环境变量）

阿里云 OSS 与微信支付属于**个人凭证**，统一通过环境变量注入，`application-dev.yml` 中不会出现明文密钥。启动前先设置：

```bash
# Linux / macOS
export OSS_ACCESS_KEY_ID=你的AccessKeyId
export OSS_ACCESS_KEY_SECRET=你的AccessKeySecret
export WX_APPID=你的小程序AppID
export WX_SECRET=你的小程序AppSecret
export WX_MCH_ID=你的商户号
export WX_MCH_SERIAL_NO=你的商户证书序列号
export WX_API_V3_KEY=你的APIv3密钥
```

```powershell
# Windows PowerShell
$env:OSS_ACCESS_KEY_ID="你的AccessKeyId"
$env:OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"
$env:WX_APPID="你的小程序AppID"
$env:WX_SECRET="你的小程序AppSecret"
$env:WX_MCH_ID="你的商户号"
$env:WX_MCH_SERIAL_NO="你的商户证书序列号"
$env:WX_API_V3_KEY="你的APIv3密钥"
```

密钥缺失时应用会直接启动失败，避免带着空凭证跑到运行时才发现问题。

数据库与 Redis 的连接信息也在 `sky-server/src/main/resources/application-dev.yml` 中，按需修改：

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: ${DB_PASSWORD:1234}     # 环境变量优先，未设置时用 1234
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:123456}
```

此外还需要按实际情况修改商户私钥路径 `privateKeyFilePath`、平台证书路径 `weChatPayCertFilePath`，以及支付回调地址 `notifyUrl` / `refundNotifyUrl`（必须是公网可访问地址）。

> 完全不用微信支付 / OSS 时，可以不设置对应的环境变量，但需要把 `WeChatProperties`、`AliOssProperties` 相关的 Bean 注入临时注释掉，否则应用启动会因占位符无法解析而失败。

### 3. 启动项目

```bash
mvn clean package -DskipTests
mvn -pl sky-server spring-boot:run
```

或在 IDE 中直接运行 `com.sky.SkyApplication`。

### 4. 访问接口文档

启动后打开 <http://localhost:8080/doc.html>（Knife4j）。

## 接口分组

所有管理端接口以 `/admin` 为前缀、用户端以 `/user` 为前缀、支付回调以 `/notify` 为前缀。

| 分组 | 路径前缀 | 认证方式 |
|---|---|---|
| 管理端 | `/admin/**` | 请求头 `token` 携带 JWT |
| 用户端 | `/user/**` | 请求头 `authentication` 携带 JWT |
| 支付回调 | `/notify/**` | 无（由微信服务器调用） |
| WebSocket | `/ws/{sid}` | 无 |

主要接口举例：

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/admin/employee/login` | 员工登录 |
| GET | `/admin/order/conditionSearch` | 订单条件搜索 |
| GET | `/admin/report/turnoverStatistics` | 营业额统计 |
| GET | `/admin/workspace/businessData` | 今日运营数据 |
| POST | `/admin/common/upload` | 图片上传（OSS） |
| POST | `/user/user/login` | 微信小程序登录 |
| GET | `/user/dish/list` | 按分类查询菜品 |
| POST | `/user/shoppingCart/add` | 添加购物车 |
| POST | `/user/order/submit` | 提交订单 |
| POST | `/user/order/payment` | 订单支付 |
| POST | `/user/order/repetition/{id}` | 再来一单 |

## 关键实现说明

**公共字段自动填充。** 新增和更新接口上用 `@AutoFill(OperationType.INSERT / UPDATE)` 标注，`AutoFillAspect` 切面统一填充 `createTime`、`updateTime`、`createUser`、`updateUser` 四个字段，避免每个 Service 重复赋值。操作人 ID 从 `BaseContext`（ThreadLocal）获取，由 JWT 拦截器在校验通过后写入。

**双 JWT 拦截器。** `JwtTokenAdminInterceptor` 与 `JwtTokenUserInterceptor` 分别校验管理端 `token` 头和用户端 `authentication` 头，解析出 ID 后放入 `BaseContext`。两套密钥与过期时间在 `application.yml` 的 `sky.jwt` 下分别配置。

**统一时间格式。** `WebMvcConfiguration#extendMessageConverters` 注册了自定义 `JacksonObjectMapper`，统一把 `LocalDateTime` / `LocalDate` / `LocalTime` 序列化成 `yyyy-MM-dd HH:mm:ss` 等格式，避免前端收到时间戳数组。

**定时任务。** `OrderTask` 每分钟扫描一次「待支付且下单超过 15 分钟」的订单，自动置为已取消并写入取消原因；每天凌晨 1 点把「派送中超过 60 分钟」的订单自动置为已完成。

**WebSocket 来单提醒。** `WebSocketServer` 用 `@ServerEndpoint("/ws/{sid}")` 暴露端点，用 `Map<String, Session>` 维护在线会话，通过 `sendToAllClient` 群发新订单提醒，管理端收到后播放语音。

**全局异常处理。** `GlobalExceptionHandler` 捕获业务异常（`BaseException` 的各子类，如账号被锁定、套餐启用失败、购物车业务异常等）、SQL 完整性约束异常和兜底异常，统一返回 `Result` 结构。

## 说明与注意事项

> ### ⚠️ 历史提交中曾包含真实密钥，请务必轮换
>
> 在本次整理之前，`sky-server/src/main/resources/application-dev.yml` 曾以明文提交过**真实可用的**第三方凭证：阿里云 OSS 的 AccessKey ID / Secret、微信小程序 AppID / AppSecret、微信支付商户号与 APIv3 密钥。该文件现已改为环境变量引用，但**密钥字符串仍然保留在 Git 历史记录中**，而本仓库是公开仓库，任何访问者都能翻到。
>
> 因此改文件只是止血，真正解除风险必须按顺序做完下面几件事：
>
> 1. 到阿里云控制台**禁用并删除**泄露的 AccessKey，重新生成一对新的；
> 2. 到微信公众平台 / 商户平台**重置** AppSecret 与 APIv3 密钥；
> 3. 新密钥只通过环境变量或本机未跟踪的配置文件提供，不再写进仓库；
> 4. 本地确认 `git log -p -- sky-server/src/main/resources/application-dev.yml` 已无新增密钥提交。
>
> 如果这批密钥对生产环境有影响，第 1、2 步请在看到本说明后尽快完成。

- 数据源、Redis 密码同理建议外置为环境变量，`application-dev.yml` 中已预留 `DB_PASSWORD` / `REDIS_PASSWORD` 两个变量位。
- 仓库中的 `*.eml`、`.classpath`、`.project` 是早期 IDE 导出的残留文件，可以安全删除。
- 微信支付的 `notifyUrl` 原先指向一个 cpolar 临时内网穿透地址，每次重启都会变化，联调时需替换为自己的公网地址。
