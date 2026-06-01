# 客户股票报表系统

该项目用于展示离线生成的客户股票报表数据。后端通过 Spring Boot 读取 PostgreSQL 中的报表表，前端通过 Vue 3、Ant Design Vue 和 ECharts 提供账户总览、账户详情、股票明细和日报趋势分析页面。

## 项目结构

- `backend/`：Spring Boot 后端服务，提供报表查询 API，并可托管前端静态页面。
- `frontend/`：Vue 3 前端应用，负责账户看板、图表、筛选和股票明细交互。

## 核心功能

- 账户总览：展示账户数量、总资产、可用资金、持仓市值、持仓收益、当日资产变化、当日已实现收益、日内 T 收益和未匹配交易数量。
- 每日趋势：展示全账户或单账户维度的每日总资产、每日资产变化、每日资产变化率、现金/持仓市值结构、每日已实现收益和持仓浮动收益。
- 账户列表：按账户展示资产、收益、交易统计、最新日报日期和快照更新时间，并支持进入单账户工作台。
- 账户详情：展示单账户日报指标、资产结构、收益趋势、股票收益排行、日内 T 收益排行和未匹配交易风险。
- 股票明细：支持按日报日期、股票代码/名称、收益类型筛选持仓，并支持表格排序。
- 股票详情抽屉：展示单只股票的持仓、价格、收益、交易统计、未匹配交易和每日收益走势。
- 后端 API：封装账户总览、账户列表、账户日报、账户股票明细、账户图表、股票详情和股票日报接口。

## 数据来源

后端默认读取以下 PostgreSQL 报表表：

- `public.customer_stock_report_summary`：账户当前汇总报表。
- `public.customer_stock_report_detail`：账户当前持仓明细报表。
- `public.customer_stock_report_summary_daily`：账户每日汇总报表。
- `public.customer_stock_report_detail_daily`：股票每日持仓明细报表。

这些表通常由离线任务生成后写入数据库，本项目只负责查询和展示。

## 后端运行

```bash
cd backend
mvn spring-boot:run
```

默认端口：`18888`

数据库连接通过环境变量配置：

```bash
POSTGRES_JDBC_URL=jdbc:postgresql://localhost:5432/saylove02
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
```

## 前端运行

```bash
cd frontend
npm install
npm run dev
```

前端接口地址可通过 `frontend/public/runtime-config.json` 配置：

```json
{
  "apiBaseUrl": "http://localhost:18888/api"
}
```

## 构建

前端生产构建：

```bash
cd frontend
npm run build
```

后端打包：

```bash
cd backend
mvn -DskipTests package
```

后端打包时会把 `frontend/dist` 复制到 Spring Boot 的静态资源目录，生成可直接部署的后端包。
