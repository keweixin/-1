# 城市临期食品智能分发系统 - 部署说明文档

## 一、环境准备

新电脑需要安装以下软件，版本必须一致：

| 软件 | 版本要求 | 下载地址 | 验证命令 |
|------|---------|---------|---------|
| JDK | 17 或以上 | https://adoptium.net/ | `java -version` |
| Node.js | 18 或以上 | https://nodejs.org/ (选 LTS) | `node -v` |
| MySQL | 8.0 | https://dev.mysql.com/downloads/ | `mysql --version` |
| Maven | 3.8+ | https://maven.apache.org/ | `mvn -v` |

> 如果你用 IntelliJ IDEA，Maven 可以用 IDEA 内置的，不需要单独装。

---

## 二、拉取项目

```bash
git clone https://github.com/keweixin/bylw.git
cd bylw
```

---

## 三、初始化���据库（最重要的一步）

### 方式 A：一键脚本（推荐）

Windows 双击 `setup-db.bat`，或在项目根目录执行：

```bash
setup-db.bat
```

按提示输入 MySQL 用户名和密码（默认 root/root），脚本会自动完成所有初始化。

### 方式 B：手动执行

如果一键脚本执行失败，按以下顺序逐个执行 SQL 文件：

```bash
# 打开 MySQL 命令行
mysql -u root -p

# 第 1 步：建库 + 建表（必须第一个执行）
source bylw-spring/sql/schema.sql;

# 第 2 步：导入标签、食品、用户行为数据
source bylw-spring/sql/seed_data.sql;

# 第 3 步：导入用户、文章、食谱、社区帖子
source bylw-spring/sql/seed_data_v2.sql;

# 第 4 步：导入扩充食品数据
source bylw-spring/sql/seed_data_v2_extra.sql;

# 第 5 步：导入 200 条食品 + 百科 + 食谱
source bylw-spring/sql/init_data_v2.sql;

# 第 6 步：数据库结构迁移（加列、索引、触发器）
source bylw-spring/sql/migration_comprehensive_v2.sql;

# 第 7 步：增量迁移
source bylw-spring/sql/migration_business_optimize.sql;
source bylw-spring/sql/migration_add_version.sql;
source bylw-spring/sql/migration_food_review.sql;
source bylw-spring/sql/migration_remaining.sql;
```

### 验证数据库是否初始化成功

```sql
USE bylw;
SHOW TABLES;          -- 应该有 25 张表左右
SELECT COUNT(*) FROM food;                -- 应该有 200+ 条食品
SELECT COUNT(*) FROM sys_user;            -- 应该有 15+ 个用户
SELECT COUNT(*) FROM food_article;        -- 应该有文章数据
SELECT COUNT(*) FROM food_recipe;         -- 应该有食谱数据
SELECT COUNT(*) FROM community_post;      -- 应该有社区帖子
```

---

## 四、启动后端（Spring Boot）

### 方式 A：IntelliJ IDEA（推荐）

1. 打开 IDEA → File → Open → 选择 `bylw-spring` 目录
2. 等待 Maven 自动下载依赖（首次可能需要几分钟）
3. 找到主启动类（一般在 `com.bylw` 包下，带 `@SpringBootApplication` 注解）
4. 右键 → Run

### 方式 B：命令行

```bash
cd bylw-spring
mvn spring-boot:run
```

### 验证后端是否启动成功

浏览器访问 http://localhost:8080/api/food/list ，如果返回 JSON 数据就说明成功了。

> 后端默认运行在 **8080** 端口。

---

## 五、启动前端（Vue）

```bash
cd bylw-vue
npm install
npm run dev
```

- `npm install` 会自动下载所有前端依赖，首次可能需要几分钟
- `npm run dev` 启动开发服务器

### 验证前端是否启动成功

终端会显示类似：

```
Local: http://localhost:3000/
```

浏览器打开 http://localhost:3000 即可看到系统首页。

---

## 六、测试账号

| 角色 | 用户名 | 密码 | 用途 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 后台管理：用户管理、食品管理、订单管理、内容审核等 |
| 普通用户 | health_wang | 123456 | 用户端：浏览食品、下单、社区、收藏等 |
| 普通用户 | office_li | 123456 | 用户端 |
| 更多用户 | 见 seed_data_v2.sql | 123456 | 共 10+ 个测试用户 |

> 所有 seed_data 中的测试用户密码均为 123456。

---

## 七、登录入口

| 端 | 地址 | 说明 |
|----|------|------|
| 用户端 | http://localhost:3000 | 首页，普通用户登录后进入 |
| 管理后台 | http://localhost:3000/admin | 管理员登录后进入 |
| 商家后台 | http://localhost:3000/merchant | 商家登录后进入 |
| 骑手端 | http://localhost:3000/rider | 骑手登录后进入 |

---

## 八、常见问题

### Q1: 后端启动报错 "Communications link failure"

MySQL 没启动，或者密码不对。检查：
1. MySQL 服务是否已启动
2. `bylw-spring/src/main/resources/application.yml` 中的数据库密码是否和你本机一致（默认 root/root）

如果密码不是 root，修改 `application.yml` 中这两行：
```yaml
url: jdbc:mysql://localhost:3306/bylw?...
username: root          # 改成你的 MySQL 用户名
password: root          # 改成你的 MySQL 密码
```

### Q2: 前端页面空白 / 接口报 404

前后端没连上。检查：
1. 后端是否已启动（http://localhost:8080 能否访问）
2. `bylw-vue/.env` 文件内容是否为 `VITE_API_BASE_URL=http://localhost:8080`
3. 前端运行在 3000 端口，后端运行在 8080 端口，Vite 会自动代理 `/api` 请求到后端

### Q3: npm install 很慢或报错

切换到国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q4: 页面有数据但图片不显示

图片文件在 `bylw-vue/public/images/` 目录下，这些已经包含在 Git 仓库中。如果图片缺失：
1. 确认 `git clone` 时网络稳定，没有文件丢失
2. 检查 `bylw-vue/public/images/foods/` 目录下是否有图片文件

### Q5: Maven 下载依赖很慢

在 `bylw-spring/pom.xml` 中确认已配置国内镜像，或修改 Maven 的 `settings.xml`：
```xml
<mirror>
    <id>aliyun</id>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
</mirror>
```

---

## 九、项目目录结构

```
bylw/
├── bylw-spring/              # 后端 (Spring Boot 3.2 + Java 17)
│   ├── src/main/java/        # Java 源码
│   │   └── com/bylw/
│   │       ├── controller/   # 接口控制器
│   │       ├── service/      # 业务逻辑层
│   │       ├── mapper/       # 数据访问层 (MyBatis-Plus)
│   │       └── entity/       # 实体类
│   ├── src/main/resources/
│   │   └── application.yml   # 后端配置文件
│   ├── sql/                  # 数据库脚本（建表 + 种子数据 + 迁移）
│   └── pom.xml               # Maven 依赖配置
│
├── bylw-vue/                 # 前端 (Vue 3 + TypeScript + Tailwind CSS)
│   ├── src/
│   │   ├── api/              # 后端接口调用
│   │   ├── views/            # 页面组件
│   │   ├── components/       # 公共组件
│   │   └── router/           # 路由配置
│   ├── public/images/        # 图片资源（食品、文章、社区等）
│   ├── .env                  # 前端环境变量（API 地址）
│   └── package.json          # Node.js 依赖配置
│
├── setup-db.bat              # Windows 一键数据库初始化脚本
├── setup-db.sh               # Linux/macOS 一键数据库初始化脚本
└── SETUP.md                  # 本文档
```
