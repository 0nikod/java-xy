# 校园二手教材与物品交易平台

基于 Java 8、JavaFX、Maven 与 SQLite 的桌面端校园二手交易系统。项目面向课程设计与本地演示场景，覆盖普通用户发布、浏览、搜索、购买、购物车、订单、评价，以及管理员审核、用户管理、日志、统计图表和 AI 辅助能力。

## 功能概览

### 普通用户

- 账号注册、登录与封禁状态校验
- 商品浏览、关键词搜索、分类筛选、排序
- 商品详情查看、图片预览、评价查看
- 发布商品，支持本地上传 `0-3` 张商品图片
- 单商品直接购买，购买后生成订单并将商品标记为已售
- 购物车加入、移除与批量结算
- 个人中心查看：我发布的商品、我购买的订单、我卖出的订单、我发布/收到的评价
- 对已完成购买订单发布评价
- AI 辅助：商品描述优化、购买建议、搜索辅助

### 管理员

- 管理员登录与后台首页
- 待审核商品查看、审核通过、违规删除
- 用户列表、按用户名搜索、封禁与解封
- 管理员操作日志查看
- 平台基础统计、明细统计表与图形化统计
- AI 辅助：运营摘要、统计图解读

### 系统能力

- SQLite 本地持久化，首次启动自动建表并初始化演示数据
- 商品图片本地存储，默认目录为 `data/images/`
- `MVC + Service + DAO` 分层结构
- OpenAI 兼容接口接入，未配置 Key、网络失败或模型异常时自动回退本地 Mock，不阻断主流程
- Maven 构建、测试、格式化与发布打包

## 技术栈

- Java 8
- JavaFX
- Maven
- SQLite / sqlite-jdbc
- OpenAI Java SDK（兼容 OpenAI API 形式的服务）
- JUnit 4
- Spotless

## 项目结构

```text
.
├── data/                         # 本地运行数据、SQLite 数据库与商品图片
├── docs/                         # 需求与实施计划
├── scripts/                      # 演示资源生成脚本
├── src/main/java/com/campus/secondhand
│   ├── app/                      # 应用启动与场景切换
│   ├── config/                   # 应用配置、数据库初始化
│   ├── controller/               # JavaFX 控制器
│   ├── dao/                      # 数据访问层
│   ├── model/                    # 领域模型与枚举
│   ├── service/                  # 业务服务与 AI / 图片服务
│   └── util/                     # 通用工具、会话、数据库连接
├── src/main/resources
│   ├── db/                       # schema.sql 与 seed.sql
│   ├── fxml/                     # JavaFX 页面
│   └── css/                      # 样式
├── pom.xml
└── README.md
```

## 环境要求

- JDK 8
- Maven 3.x
- 可运行 JavaFX 的桌面环境

项目默认 Maven 配置中的 JavaFX classifier 为 `linux`。如果在 Windows 或 macOS 构建运行，需要在 `pom.xml` 中将 `javafx.platform` 调整为对应平台 classifier，或通过 Maven 属性覆盖。

## 本地运行

```bash
# 如本机有多个 JDK，可显式指定 Java 8
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean compile
mvn exec:java
```

如果项目目录中使用 `.envrc`，也可以先执行：

```bash
source .envrc
mvn clean compile
mvn exec:java
```

首次启动会自动初始化数据库与演示数据。默认运行数据路径如下：

- 数据库：`data/secondhand.db`
- 商品图片：`data/images/`

## 可选配置

可以通过环境变量或 JVM 系统属性指定数据库和图片目录：

```bash
export SECONDHAND_DB_PATH="./data/secondhand.db"
export SECONDHAND_IMAGE_DIR="./data/images"
```

AI 相关配置：

```bash
export OPENAI_API_KEY="你的 API Key"
export OPENAI_BASE_URL="https://api.deepseek.com"
export OPENAI_MODEL="deepseek-v4-flash"
```

说明：

- 未配置 `OPENAI_API_KEY` 时自动使用本地 Mock AI。
- 配置 Key 后优先调用真实接口。
- 接口失败时自动回退 Mock，保证发布、搜索、统计等主流程可继续演示。

## 演示账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 进入管理员后台，审核商品、管理用户、查看统计 |
| 普通用户/卖家 | `demo_user` | `user123` | 已发布多条演示商品 |
| 普通用户/买家 | `demo_buyer` | `buyer123` | 可浏览、购买、评价商品 |

演示数据包含在售商品、待审核商品、已成交订单和本地种子图片，便于直接展示完整业务流程。

## 常用命令

### 编译

```bash
mvn clean compile
```

### 运行测试

```bash
mvn test
```

### 格式检查与自动格式化

```bash
mvn spotless:check
mvn spotless:apply
```

### 打包发布

```bash
mvn package
```

打包后会在项目根目录生成 `dist/` 目录，包含：

- `CampusSecondhand.exe`
- Fat JAR：`java-xy-1.0.0-SNAPSHOT-jar-with-dependencies.jar`
- `data/` 运行数据目录

启动时会以运行目录为基准解析 `data/secondhand.db` 与 `data/images/`，便于绿色版目录直接携带与读写。

## 推荐演示流程

1. 使用 `demo_user / user123` 登录，发布一个商品并上传图片。
2. 使用 `admin / admin123` 登录后台，在商品审核页通过该商品。
3. 使用 `demo_buyer / buyer123` 登录，搜索并查看商品详情。
4. 体验 AI 购买建议，将商品加入购物车或直接购买。
5. 在个人中心查看购买订单，并对已完成订单发布评价。
6. 回到管理员后台查看用户管理、操作日志、统计图表、AI 运营摘要和统计解读。

## 需求与范围说明

- `docs/goal.md`：完整功能需求文档。
- `docs/plan.md`：项目实施计划与验收标准。

当前版本已将购物车、评价、AI 搜索/购买建议、AI 统计解读等能力纳入实现范围；如后续扩展报表导出、更多支付/消息能力，可继续在现有分层结构上扩展。
