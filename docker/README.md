# 城市临期食品分发与膳食个性化推荐系统

基于 Vue 3 + Spring Boot + MySQL 的城市临期食品分发系统，支持用户下单、商家管理、骑手配送���管理员后台管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Pinia + Tailwind CSS |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + JWT |
| 数据库 | MySQL 8.0 |
| 部署 | Docker + Docker Compose + Nginx |

## 项目结构

```
xm/
├── bylw-spring/          # Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile        # (位于 docker/ 目录)
├── bylw-vue/             # Vue 3 前端
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
└── docker/               # 部署配置
    ├── docker-compose.yml
    ├── Dockerfile         # 后端 Dockerfile
    ├── .env               # 环境变量配置
    ├── init.sql           # 数据库初始化脚本
    └── README.md
```

## 快速部署（Docker Compose 一键启动）

### 前提条件

- 安装 [Docker](https://docs.docker.com/get-docker/)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

### 1. 获取项目

```bash
git clone https://github.com/<your-username>/bylw-food-distribution.git
cd bylw-food-distribution/docker
```

### 2. 配置环境变量

```bash
# 编辑 .env 文件，修改以下配置
cp .env .env.local
```

必须修改的配置项：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL root 密码 | `bylw2026secure` |
| `JWT_SECRET` | JWT 签名密钥（至少32字符） | 内置默认值 |

### 3. 启动服务

```bash
# 构建并启动所有服务
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 4. 访问系统

| 服务 | 地址 |
|------|------|
| 前端首页 | http://localhost (端口由 FRONTEND_PORT 控制) |
| 后端 API | http://localhost:8080/api (端口由 BACKEND_PORT 控制) |
| MySQL | localhost:3306 |

### 5. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 商家 | merchant1 | 123456 |
| 骑手 | rider1 | 123456 |
| 用户 | user1 | 123456 |

> 首次登录后请立即修改密码。

## 常用命令

```bash
# 停止服务
docker compose down

# 停止并删除数据卷（重置数据库）
docker compose down -v

# 重新构建并启动
docker compose up -d --build

# 仅重启后端
docker compose restart backend

# 查看后端日志
docker compose logs -f backend

# 进入 MySQL 容器
docker compose exec mysql mysql -uroot -p
```

## 本地开发

### 后端

```bash
cd bylw-spring
# 确保 MySQL 已运行且创建了 bylw 数据库
mvn spring-boot:run
```

### 前端

```bash
cd bylw-vue
npm install
npm run dev
# 访问 http://localhost:3000
```

## 故障排除

### 数据库连接失败

```bash
# 检查 MySQL 是否就绪
docker compose exec mysql mysqladmin ping -h localhost -uroot -p
```

### 端口冲突

修改 `.env` 中的端口配置：

```env
FRONTEND_PORT=8081
BACKEND_PORT=8082
DB_PORT=3307
```

### 前端无法连接后端

确认 `nginx.conf` 中的 `proxy_pass` 指向 `http://backend:8080`（Docker 内部网络）而非 `localhost`。

## 许可证

本项目仅供学习和毕业论文使用。
