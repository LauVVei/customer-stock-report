# customer-stock-report

customer-stock-report 是一个客户持仓与收益分析报表系统。项目包含 Spring Boot 后端和 Vue 3 前端，用于展示离线计算后的客户资产、持仓、收益、日度收益和股票明细数据。

## 功能

- 展示全部交易账户的汇总概览。
- 查看单个账户的资产、收益和持仓详情。
- 展示账户收益贡献、仓位分布、未完成匹配交易和股票明细。
- 后端从 PostgreSQL 报表表读取数据并提供 REST API。
- 前端通过运行时配置切换 API 地址，支持独立开发和静态部署。

## 技术栈

- 后端：Java 17、Spring Boot 3.3、Spring JDBC、PostgreSQL
- 前端：Vue 3、TypeScript、Vite、Ant Design Vue、ECharts、Axios

## 目录结构

```text
backend/   Spring Boot API 服务和静态资源托管
frontend/  Vue 3 前端应用
```

## 数据库配置

后端使用环境变量读取数据库连接信息，默认只指向本地示例库：

```bash
export POSTGRES_JDBC_URL=jdbc:postgresql://localhost:5432/saylove02
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=your_database_password
```

Windows PowerShell 示例：

```powershell
$env:POSTGRES_JDBC_URL="jdbc:postgresql://localhost:5432/saylove02"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="your_database_password"
```

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

默认端口：`18080`

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

默认开发地址：`http://localhost:5174`

前端运行时 API 地址配置文件：

```text
frontend/public/runtime-config.json
```

示例：

```json
{
  "apiBaseUrl": "http://localhost:18080/api"
}
```

## 生产构建

构建前端：

```bash
cd frontend
npm install
npm run build
```

构建后端：

```bash
cd backend
mvn clean package -DskipTests
```

运行后端 Jar：

```bash
java -jar target/customer-stock-report-backend-1.0.0.jar
```

## 部署方式

1. 部署 PostgreSQL，并准备报表查询所需的数据表。
2. 在服务器上配置 `POSTGRES_JDBC_URL`、`POSTGRES_USER` 和 `POSTGRES_PASSWORD`。
3. 构建前端并将 `frontend/dist` 交给后端资源复制流程，或使用 Nginx 独立托管。
4. 构建并运行 Spring Boot 后端。
5. 确认 `runtime-config.json` 中的 `apiBaseUrl` 指向实际后端地址。

## 安全说明

仓库不应提交真实数据库密码、构建产物、IDE 配置、`.env` 文件或服务器地址。生产凭据请通过环境变量、密钥管理服务或部署平台配置注入。
