# 城市临期食品智能分发系统 - 部署说明文档

## 一、环境准备

新电脑只需要安装一个软件：

| 软件 | 下��地址 | 验证命令 |
|------|---------|---------|
| Docker Desktop | https://www.docker.com/products/docker-desktop/ | `docker -v` |

> 安装 Docker Desktop 后，`docker compose` 命令会自动包含在内，无需额外安装 JDK、Node.js、MySQL、Maven。

---

## 二、拉取项目

```bash
git clone https://github.com/keweixin/-1.git
cd -1
```

---

## 三、一键启动（Docker Compose）

### 1. 进入部署目录

```bash
cd docker
```

### 2. 检查环境变量（可选）

`docker/.env` 文件内容：

```env
DB_PASSWORD=bylw2026secure      # MySQL 密码
DB_PORT=3306                    # MySQL 端口
BACKEND_PORT=8080               # 后端 API 端口
JWT_SECRET=bylw-production-change-this-to-a-secure-random-string-32chars
JWT_EXPIRATION=86400000         # Token 过期时间(毫秒)
FRONTEND_PORT=80                # 前端端口
```

一般不需要修改。如果本机 80/8080/3306 端口被占用，改对应的值即可。

### 3. 构建并启动所有服务

```bash
docker compose up -d --build
```

这一条命令会自动完成：
- 拉取 MySQL 8.0 镜像，创建数据库并自动导入 `init.sql`（包含所有建表语句、种子数据、触发器）
- 编译 Spring Boot 后端项目，打包并运行
- 编译 Vue 前端项目，用 Nginx 托管并提供 API 反向代理

> 首次构建需要下载依赖和镜像，大约 5-10 分钟。

### 4. 确认服务启动

```bash
# 查看三个容器的状态（应该都是 running / healthy）
docker compose ps

# 查看日志（确认没有报错）
docker compose logs -f
```

正确启动后输出类似：

```
NAME              STATUS
bylw-mysql        running (healthy)
bylw-backend      running
bylw-frontend     running
```

---

## 四、访问系统

| 端 | 地址 |
|----|------|
| 前端首页 | http://localhost |
| 后端 API | http://localhost:8080/api |
| MySQL | localhost:3306 |

---

## 五、测试账号

| 角色 | 用户名 | 密码 | 用途 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 后台管理：用户管理、食品管理、订单管理、内容审核 |
| 普通用户 | health_wang | 123456 | 用户端：浏览食品、下单、社区、收藏 |
| 普通用户 | office_li | 123456 | 用户端 |
| 更多用户 | 见数据库 | 123456 | 共 10+ 个测试用户 |

---

## 六、登录入口

| 端 | 地址 |
|----|------|
| 用户端 | http://localhost |
| 管理后台 | http://localhost/admin |
| 商家后台 | http://localhost/merchant |
| 骑手端 | http://localhost/rider |

---

## 七、常用 Docker 命令

```bash
# 查看服务状态
docker compose ps

# 查看实时日志
docker compose logs -f

# 只看后端日志
docker compose logs -f backend

# 进入 MySQL 容器
docker compose exec mysql mysql -uroot -pbylw2026secure bylw

# 重启单个服务
docker compose restart backend

# 停止所有服务
docker compose down

# 停止并清除数据库数据（重置所有数据）
docker compose down -v

# 重新构建并启动（代码有修改时）
docker compose up -d --build
```

---

## 八、常见问题

### Q1: 端口被占用

修改 `docker/.env` 中的端口：

```env
FRONTEND_PORT=8081    # 前端改到 8081
BACKEND_PORT=8082     # 后端改到 8082
DB_PORT=3307          # MySQL 改到 3307
```

然后重新 `docker compose up -d`。

### Q2: 前端页面空白 / 接口报错

```bash
# 检查三个容器是否都在运行
docker compose ps

# 查看后端日志排查原因
docker compose logs backend
```

### Q3: 数据库连接失败

```bash
# 检查 MySQL 是否就绪
docker compose exec mysql mysqladmin ping -h localhost -uroot -pbylw2026secure

# 如果刚启动，等 MySQL 健康检查通过后再看
docker compose ps
```

### Q4: 想重置所有数据

```bash
docker compose down -v
docker compose up -d --build
```

`-v` 会删除 MySQL 数据卷，下次启动时会重新执行 `init.sql` 初始化。

### Q5: 构建很慢（国内网络）

Docker 拉取镜像慢可以配置镜像加速器，在 Docker Desktop → Settings → Docker Engine 中添加：

```json
{
  "registry-mirrors": ["https://docker.1ms.run"]
}
```

Maven 下载依赖慢，后端 Dockerfile 已配置阿里云镜像源，一般不需要额外处理。

### Q6: 代码修改后如何更新

```bash
# 重新拉取最新代码
git pull

# 重新构建并启动
cd docker
docker compose up -d --build
```

---

## 九、项目目录结构

```
-1/
├── bylw-spring/              # 后端 (Spring Boot 3.2 + Java 17)
│   ├── src/main/java/
│   │   └── com/bylw/
│   │       ├── controller/   # 接口控制器
│   │       ├── service/      # 业务逻辑层
│   │       ├── mapper/       # 数据访问层 (MyBatis-Plus)
│   │       └── entity/       # 实体类
│   ├── src/main/resources/
│   │   └── application.yml   # 后端配置文件
│   └── pom.xml               # Maven 依赖配置
│
├── bylw-vue/                 # 前端 (Vue 3 + TypeScript + Tailwind CSS)
│   ├── src/
│   │   ├── api/              # 后端接口调用
│   │   ├── views/            # 页面组件
│   │   ├── components/       # 公共组件
│   │   └── router/           # 路由配置
│   ├── public/images/        # 图片资源（食品、文章、社区等）
│   ├── Dockerfile            # 前端容器构建文件
│   ├── nginx.conf            # Nginx 配置（API 反向代理）
│   └── package.json          # Node.js 依赖配置
│
├── docker/                   # Docker 部署配置
│   ├── docker-compose.yml    # 编排文件（MySQL + 后端 + 前端）
│   ├── Dockerfile            # 后端容器构建文件
│   ├── .env                  # 环境变量配置
│   ├── init.sql              # 数据库完整初始化脚本（含数据）
│   └── README.md             # Docker 部署说明
│
├── setup-db.bat              # Windows 本地开发数据库初始化脚本
├── setup-db.sh               # Linux/macOS 本地开发数据库初始化脚本
└── SETUP.md                  # 本文档
```

---

## 十、本地开发（不使用 Docker）

如果你想在本地用 IDEA 开发而不是 Docker 部署，参考以下步骤：

### 环境要求

| 软件 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0 |
| Maven | 3.8+（或 IDEA 内置） |

### 步骤

```bash
# 1. 初始化数据库（双击 setup-db.bat 或手动执行）
setup-db.bat

# 2. 启动后端（IDEA 打开 bylw-spring 目录，运行主启动类）
#    或命令行: cd bylw-spring && mvn spring-boot:run

# 3. 启动前端
cd bylw-vue
npm install
npm run dev

# 4. 访问 http://localhost:3000
```
