# 城市临期食品智能分发系统 - Docker 部署

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Pinia + Tailwind CSS |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + JWT |
| 数据库 | MySQL 8.0 |
| 部署 | Docker + Docker Compose + Nginx |

## 快速启动

### 前提条件

- 安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### 一键启动

```bash
cd docker
docker compose up -d --build
```

启动完成后访问：

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| 后端 API | http://localhost:8080/api |

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | health_wang | 123456 |

## 配置说明

编辑 `docker/.env` 修改端口和密码：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL root 密码 | `bylw2026secure` |
| `DB_PORT` | MySQL 端口 | `3306` |
| `BACKEND_PORT` | 后端 API 端口 | `8080` |
| `FRONTEND_PORT` | 前端端口 | `80` |
| `JWT_SECRET` | JWT 签名密钥 | 内置默认值 |

## 常用命令

```bash
# 查看状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 重置数据库
docker compose down -v

# 重新构建
docker compose up -d --build
```

## 服务架构

```
浏览器 → Nginx(:80) → 前端静态文件
                     → /api/ 反向代理 → Spring Boot(:8080) → MySQL(:3306)
```

- `init.sql` 是当前数据库的完整导出（建表 + 数据 + 触发器），MySQL 容器首次启动时自动执行
- 前端 Nginx 负责静态文件托管和 API 反向代理
- 后端通过 Docker 内部网络 `mysql` 主机名连接数据库
