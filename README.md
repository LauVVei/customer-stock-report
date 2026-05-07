# 客户持仓分析报告项目

该目录包含一个独立的报告项目，展示离线 Spark 更新后的两个数据库表：

- `public.customer_stock_report_summary`
- `public.customer_stock_report_detail`

## 目录结构

- `backend`：Spring Boot 后端接口服务
- `frontend`：Vue3 + Ant Design Vue + ECharts 前端页面

## 设计说明

- 首页展示所有账户总览
- 点击账户进入账户详情页
- 账户详情展示：
  - 账户摘要
  - 收益贡献图
  - 仓位分布图
  - 未完成匹配交易图
  - 股票明细表
  - 股票详情抽屉
- 颜色遵循 A 股习惯：
  - 红色代表盈利/上涨
  - 绿色代表亏损/下跌
- 页面默认仅在首次进入时加载数据
- 如需获取最新数据，使用页面右上角“立即刷新”

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

默认地址：`http://localhost:5174`

## 运行配置

前端可修改：

`frontend/public/runtime-config.json`

```json
{
  "apiBaseUrl": "http://localhost:18080/api"
}
```

后端数据库配置：

`backend/src/main/resources/application.yml`
