# 校园二手教材与物品交易平台

当前版本为课程设计首版收口版本，基于 Java 8、Maven、JavaFX、SQLite，覆盖注册登录、商品发布与审核、图片本地管理、购买下单、个人中心、用户管理、日志和统计展示，并保留延期模块占位。

## 本地运行

```bash
source .envrc
JAVA_HOME=/usr/lib/jvm/java-8-openjdk PATH="$JAVA_HOME/bin:$PATH" mvn clean compile
mvn exec:java
```

可选环境变量：

```bash
export SECONDHAND_DB_PATH="./data/secondhand.db"
export SECONDHAND_IMAGE_DIR="./data/images"
export OPENAI_API_KEY=""
export OPENAI_BASE_URL="https://api.openai.com/v1"
export OPENAI_MODEL="gpt-4.1-mini"
```

## 发布打包

执行 `mvn package` 后，会在项目根目录生成 `dist/` 发布目录，包含：

- `CampusSecondhand.exe`
- Fat JAR
- `data/` 运行数据目录

启动时会以运行目录为基准解析 `data/secondhand.db` 与 `data/images/`，便于绿色版目录直接携带与读写。

## 当前交付范围

- 普通用户：注册、登录、搜索/筛选/排序、发布商品、上传 `0-3` 张图片、查看详情、单商品购买、个人中心、下架未售商品
- 管理员：登录、审核商品、删除违规商品、用户搜索、封禁/解封、日志查看、统计图表、AI 运营摘要
- 系统能力：SQLite 自动建表、演示数据初始化、无 Key 时 Mock AI 兜底、图片本地存储

## 演示数据

- 管理员：`admin / admin123`
- 普通用户卖家：`demo_user / user123`
- 普通用户买家：`demo_buyer / buyer123`
- 初始数据包含待审核商品、在售商品、已成交订单，以及一条可展示的本地种子图片


## 代码格式化

```bash
mvn spotless:check
mvn spotless:apply
```
## AI 说明

- 首版只保留 `商品描述优化` 和 `管理员运营摘要`
- 配置 `OPENAI_API_KEY` 后优先走真实调用
- 未配置 Key、网络失败或模型异常时自动回退到本地 Mock，不阻断主流程

## 延期模块

- 购物车
- 评价
- AI 定价建议
- AI 购买建议
- AI 搜索辅助
- AI 违规风险分析
- 图表 AI 解读

这些能力当前只保留文档或界面占位，不纳入首版验收。
