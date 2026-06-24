# fool-service

## 概述

Just for My Dream.

* fool-common 公共库
* fool-log 日志
* fool-error-handler 错误处理
* fool-dao dao的封装
* fool-dto 公共的response/request定义
* fool-model 模型管理
* fool-view 视图管理
* fool-query 查询组件
* fool-app-manage 应用、应用数据库管理与初始化安装流程
* fool-db-manage 工作数据库、数据源与 SQL 执行管理
* fool-event 事件定义与消息通知
* fool-report 报表定义、矩阵结果与旧报表网格输出
* fool-reflect 基于反射的无代码
* fool-dynamic 基于配置的无代码
* fool-auth 简单的基于角色的权限管理
* business-application 一个启动程序

## Harness 与 Standard Engine

Agent 入口在 `AGENTS.md`。本地最小仓库门禁：

```bash
python scripts/check_repo_harness.py
```

验证矩阵见 `docs/validation.md`，版本化标准见 `docs/standards/README.md`。
需要保存自动化证据时使用：

```bash
python scripts/check_repo_harness.py --report-json artifacts/runs/<run_id>/repo-harness.json
```

## Docker 环境

```bash
docker compose up -d --build
```

启动后访问：

* Vue 前端：http://localhost:8081/
* Spring Boot 后端：http://localhost:8080/
* MySQL：127.0.0.1:3307，库名 `car_wash`，root 密码 `Pa88word`
* Redis：127.0.0.1:6380

常用检查：

```bash
docker compose ps
curl http://localhost:8080/test
curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view
curl -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list
```

`docker/mysql/init/` 会在全新 MySQL volume 初始化时创建当前已迁移模块的基础表。已有 volume 不会自动重跑 init 脚本，可用下面命令手动补一次：

```bash
for sql in docker/mysql/init/*.sql; do
  docker compose exec -T mysql mysql -uroot -pPa88word car_wash < "$sql"
done
```

## 前端

Vue 3 前端位于 `frontend/`，用于替代 `../FoolFrame/src/Web` 的旧 Node/Express/Jade/Angular 操作界面首批流程。

```bash
cd frontend
npm install
npm test
npm run build
```

Vite 本地开发：

```bash
cd frontend
npm run dev
```

## 迁移状态

迁移对照和剩余差距记录在 `docs/migration/foolframe-parity.md`。当前已经完成 Docker 化启动、Spring Boot 后端容器构建、Vue 前端首屏操作台与 `OrderList` view/data smoke、应用管理基础模型、DAO 创建适配、创建者授权用户写入、AppSys 视图准备、旧模块和 root model shell 安装记录、菜单记录/子项关系创建、角色创建及角色-用户/角色-菜单关系创建、工作数据库/数据源/SQL 执行迁移、事件/消息基础模型、直接用户/角色/部门/公司通知关系、角色授权用户展开、部门与公司部门用户展开、All 授权用户收件人来源、应用数据库目录加载、事件对象查询的模型元数据表名、对象 ID 列解析与匹配行值保留、scoped JDBC runtime 与可配置调度 lifecycle、旧模型/关系 MySQL DDL 生成器、app-manage 执行 hook、App.SysCon/业务库 DAO 路由分流、连接字符串到 DaoService/JdbcTemplate 工厂、安装流程模型 schema 接线、静态 module source 模型展开与 legacy AssemblyModuleSource 依赖排序、typed reflective Java module source 基础模型发现、file/jar 包扫描、跨包引用 module 发现、集合 One2Many/自引用 Recurve/双向 Many2Many/ReferToProperty/MultiType/ObjectWithSubItem parent target-property 关系生成与 ColumnAttribute key-group/generation/format/encryption/no-map/multi-column DBMaps 元数据和 DDL 列生成、module-source 模块/模型/属性/关系壳元数据持久化与默认 auto-view hook、旧默认列表/详细视图 factory 行为、报表基础模型/矩阵结果和基础 smoke schema；剩余 FoolFrame 运行时行为仍在迁移中。
