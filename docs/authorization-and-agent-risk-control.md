# Authorization and Agent Risk Control

本文档是 Fool Service 权限控制、数据范围控制，以及 Agent 中高风险动作接入的设计源。
`docs/agent-sessions.md` 继续描述当前 Agent 会话、能力顺序和模型提供方边界，
`tasks.md` 只跟踪实施状态和验收条件。

状态：设计基线，尚未表示相关执行能力已经实现或可以在生产环境开放。

## 1. 背景与问题

系统已经具备以下基础能力：

- View 列表、详情、查询和报表运行入口；
- 报表字段、查询条件和结果列配置；
- 登录、用户、角色、菜单和应用级组织关系；
- `/agent` Chat 工作区和五阶段配置草案；
- View、Model、数据源和事件自动化的只读元数据快照；
- Agent 草案中的低、中、高风险文字标记和 dry-run 建议。

这些能力还不能构成中高风险动作的安全执行边界。当前实现存在以下结构性缺口：

1. `auth_item` 主要表达父菜单和 View 入口，不能表达资源、动作、数据行、字段、应用、
   数据库和审批范围。
2. `/api/v1/data/*`、`/api/v1/report/*` 和 `/api/v1/agent/*` 没有统一的服务端
   Policy Enforcement Point。请求中即使携带 token，也不等于执行前完成了授权。
3. Agent 会话按原始 token 字符串确认会话归属，`FOOL_AGENT_SESSION` 也保存原始 token，
   而不是经过验证的用户身份和作用域。
4. `medium-draft-only`、`high-dry-run-required`、`Require human approval` 等内容
   当前只是草案文本，不是可验证、不可绕过的状态机。
5. 外部模型请求会携带最近会话消息和完整结构化草案。没有内容分级、字段脱敏和提供方
   出站策略时，授权读取和允许发送给模型是两个未被区分的问题。
6. `runoperation` 可以落到 `CREATE`、`UPDATE`、`DELETE`、`ASSEBMLY`、`WCF`、
   `JSONPOST`、`JSONGET` 等不同副作用，不能只按同一个“执行操作”权限处理。

因此，实施顺序必须是：先建立统一身份与授权边界，再开放只读 Agent 工具，最后逐项接入
中高风险动作。前端隐藏按钮、模型提示词和 Chat 中的“请确认”都不能替代服务端授权。

## 2. 目标

本设计需要实现以下目标：

- 所有受保护请求默认拒绝，且在服务端每次访问和执行时重新鉴权；
- 角色负责表达职责，属性策略负责表达应用、数据库、公司、部门、数据行和字段边界；
- 页面、报表、导出、Chat 和后台自动化共享同一套数据范围；
- LLM 只能提出结构化意图，不能决定权限、风险等级、审批结果或直接调用数据库；
- 中高风险动作必须经过确定性的预检、预览、确认或审批、执行和审计状态机；
- 审批只对一个不可变 payload 生效，修改内容、超时、重放或权限变化都使审批失效；
- 直接从页面发起的中高风险动作与 Chat 发起的动作使用同一条受控执行路径；
- Agent 能力不能超过当前用户在当前应用和数据库作用域中的有效权限；
- 敏感数据、token、密码、连接串和密钥不会进入模型上下文、审批记录或普通日志；
- 保持旧版 FoolFrame 菜单和请求兼容，但不继续把菜单可见性当作最终授权结论。

## 3. 非目标

第一阶段不包含以下内容：

- 不允许模型执行任意 SQL、脚本、反射调用、类或 DLL；
- 不通过 Prompt 约束来替代服务端策略；
- 不一次性重写 `auth_*` 和 `SW_APP_AUTH_*` 两套旧表；
- 不把全部业务操作抽象成一个万能执行器；
- 不承诺 DDL 和外部系统调用总能自动回滚；
- 不在本设计中实现完整 IAM、单点登录或跨系统身份联盟；
- 不因为用户是管理员而跳过数据脱敏、审计、风险计算或审批有效性校验。

## 4. 设计原则

### 4.1 默认拒绝

没有匹配到明确 Allow 的请求必须拒绝。策略加载失败、身份解析失败、资源解析失败、
风险计算失败或审计前置记录失败时，中高风险动作都必须 fail closed。

### 4.2 每次请求重新鉴权

菜单、路由、前端按钮和 Agent capability 只用于改善界面，不是安全边界。Controller、
业务 Service 和最终 Action Handler 都必须使用服务端解析出的身份进行授权。

### 4.3 最小权限与用户上下文执行

Agent 不拥有独立管理员账号。动作以发起用户为 owner，并在执行时使用该用户的当前权限、
数据范围和应用/数据库作用域。审批人只批准动作，不替代发起人的业务权限。

### 4.4 决策与生成分离

LLM 可以生成 `ActionIntent`，但以下字段只能由服务器生成：

- `actorUserId`；
- `effectiveRoles`；
- `resourceId` 的最终解析结果；
- `riskLevel` 和风险原因；
- `policyVersion`；
- `payloadHash`；
- `approvalPolicy`；
- `executorId`；
- `executionStatus`。

### 4.5 同一路径控制直接操作与 Agent 操作

保存报表、导出、保存数据、删除、执行 View Operation、修改数据源和启用事件时，不区分
“按钮发起”还是“Chat 发起”。两者都先形成受控 Action Request，再进入相同的预览、审批、
执行和审计链。

### 4.6 风险只能升高，不能降低

动作的最低风险等级由代码中的 Action Catalog 决定。数据库策略和运行时影响因子只能提高
风险等级，不能把代码定义的高风险动作配置为低风险。

### 4.7 不使用任意 SQL 表达数据权限

行级数据范围使用受限结构化 DSL，由服务器编译为参数化查询条件。权限表中不保存可直接
拼接到 SQL 的表达式。旧版 `QueryFilter` 即使暂时保留，也必须与服务端策略条件独立组合。

## 5. 总体架构

```text
Browser / Agent Chat / Scheduler
              |
              v
 Authentication Filter -> EffectiveSubject
              |
              v
 Resource Resolver -> AuthorizationService -> AuthorizationDecision
              |                    |
              |                    +-> row policy / field policy / quotas
              v
 Action Intent Validator -> Risk Engine -> Action Request Store
              |
              v
 Preview Handler -> Confirmation / Approval -> Execution Re-check
              |
              v
 Domain Action Handler -> Transaction / Outbox / Result
              |
              v
 Security Audit + Business Audit + Metrics
```

### 5.1 模块边界

当前依赖方向包含 `fool-auth -> fool-app-manage -> fool-view`，因此 `fool-view` 不能直接
依赖 `fool-auth`。设计沿用仓库已有的低层 SPI 模式：

| 模块 | 责任 |
| --- | --- |
| `fool-common` | 定义无业务依赖的 `EffectiveSubject`、`AuthorizationRequest`、`AuthorizationDecision`、`AuthorizationService`、`ControlledActionHandler` 等 SPI 和值对象。 |
| `fool-auth` | token 认证、Spring Security 过滤器、用户/角色/组织解析、JDBC 策略仓库、授权实现、step-up 认证和安全审计实现。 |
| `fool-view` / `fool-model` / `fool-db-manage` / `fool-event` | 解析本模块资源，在读写服务入口调用授权 SPI，并实现本模块受控 Action Handler。 |
| `fool-agent` | Chat 意图解析、Action Request 状态机、风险编排、预览/审批/执行协调和 Agent 会话关联；不拥有领域数据写入逻辑。 |
| `business-application` | 组装认证过滤器、授权实现、Handler Registry、公开端点 allowlist 和 HTTP API。 |
| `frontend` | 展示有效动作、风险、预览和审批状态；不自行计算最终权限和风险。 |

`AuthorizationService` 缺失或策略仓库不可用时，生产实现默认拒绝。单元测试必须显式提供
允许或拒绝的测试策略，不能因为没有加载 `fool-auth` 而隐式放行。

## 6. 身份、会话与作用域

### 6.1 请求认证

新接口统一使用：

```http
Authorization: Bearer <opaque-token>
```

兼容期内，旧 DTO 的 `token` / `Token` 只作为适配输入。过滤器解析后必须得到同一个
`EffectiveSubject`，业务代码不能继续直接信任 Request Body 中的 token。兼容期结束后停止
在新接口中接受 body token。

公开端点必须使用显式 allowlist，例如登录、验证码和最小化 liveness。其他端点默认需要
认证，包括 Agent provider/capability catalog。

### 6.2 Token 要求

- token 使用不可预测的随机值，并只存储服务端 hash；
- token 有可配置的 idle TTL 和 absolute TTL；
- 登录、退出、密码修改、角色/权限变更后执行轮换或吊销；
- 日志、Agent 会话、审批表和错误响应都不保存原始 token；
- Redis 中的 user-to-token 和 token-to-user 映射必须使用一致 key，并记录过期时间；
- 当前代码中的 token 日志输出必须移除；
- 旧 MD5 密码在成功登录时迁移到自适应密码 hash，未迁移账号不能用于高风险 step-up。

### 6.3 EffectiveSubject

服务端从当前认证和应用上下文构建：

```json
{
  "userId": "admin",
  "roleIds": ["auth:1", "legacy:app-1:1"],
  "companyId": "1",
  "departmentIds": ["10"],
  "appId": "app-1",
  "databaseId": "main",
  "sessionId": "auth-session-id",
  "authenticatedAt": "2026-07-15T10:00:00Z",
  "stepUpAt": null,
  "policyVersion": 42
}
```

`userId` 是会话归属主键。Agent Session 只保存 `OWNER_USER_ID` 和固定的 app/database scope，
不保存用于重新认证的 bearer token。

### 6.4 旧权限目录兼容

过渡期内 `SubjectResolver` 同时读取：

- `auth_user_role` / `auth_role`；
- `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER` / `SW_APP_AUTH_ROLE`；
- `SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE`；
- `SW_APP_AUTH_USER.APP_AUTH_DEP` 和公司关系。

角色 ID 带 namespace，避免两套目录的数字 ID 冲突。菜单继续由旧关系生成，但菜单项是否
返回也要经过 `view.discover` 或对应资源的授权检查。

## 7. 授权模型

授权请求由 Subject、Resource、Action、Scope 和 Environment 五部分组成。

### 7.1 Subject

Subject 可以是：

- 用户；
- 当前角色；
- 部门；
- 部门树；
- 公司；
- 服务身份。服务身份只能用于明确的 scheduler 或后台任务，不等于 Agent 身份。

### 7.2 Resource

资源使用稳定、可审计的类型和 ID，建议形成以下 canonical key：

```text
app:{appId}
app:{appId}:db:{dbId}
app:{appId}:db:{dbId}:view:{viewId}
app:{appId}:db:{dbId}:model:{modelId}
app:{appId}:db:{dbId}:model:{modelId}:field:{propertyId}
app:{appId}:db:{dbId}:operation:{operationId}
app:{appId}:db:{dbId}:datasource:{dataSourceKey}
app:{appId}:db:{dbId}:event:{definitionId}
agent:capability:{capabilityId}
```

客户端只能提供候选 ID。服务器必须根据 View/Model/Operation 元数据解析最终资源，并验证
资源确实属于当前 app/database scope。资源继承不是默认行为，只有 binding 明确设置
`include_children=true` 时，上级资源授权才能覆盖子资源。

### 7.3 Action Catalog

Action Catalog 是代码所有的稳定枚举或注册表，不使用模型自由生成的字符串：

| Action | 资源 | 最低风险 | 说明 |
| --- | --- | --- | --- |
| `view.discover` | View | LOW | 菜单和路由发现。 |
| `view.read` | View | LOW | 读取 View 定义和只读详情定义。 |
| `view.query` | View/Model | LOW | 列表、详情、lookup 和报表数据查询。 |
| `report.preview` | View | LOW | 读取字段模型并运行小分页报表。 |
| `report.save` | View/Report | MEDIUM | 保存或修改报表定义。 |
| `report.export` | View/Report | MEDIUM | 导出受数据范围限制的结果。 |
| `data.create` | Model/View | MEDIUM | 创建业务对象。 |
| `data.update` | Model/View | MEDIUM | 更新单个、非敏感业务对象。 |
| `data.delete` | Model/View | HIGH | 删除业务对象。 |
| `operation.execute` | Operation | 动态 | 根据底层 OperationBaseType 和副作用提高风险。 |
| `model.configure` | Model | HIGH | 修改模型、字段、关系和触发器元数据。 |
| `model.ddl.preview` | Model | MEDIUM | 生成 DDL diff，不执行。 |
| `model.ddl.execute` | Model | HIGH | 执行非破坏性 DDL。破坏性 DDL 为 CRITICAL。 |
| `datasource.test` | DataSource | MEDIUM | 服务端解析凭据后执行受限连通性检查。 |
| `datasource.route.update` | DataSource | HIGH | 修改应用或 DS key 路由。 |
| `datasource.credential.update` | DataSource | HIGH | 修改凭据引用，不返回明文。 |
| `event.preview` | Event | LOW | 匹配对象和接收人预览，不写事件。 |
| `event.enable` | Event | HIGH | 启用定义或 scheduler 变更。 |
| `message.send` | Event | HIGH | 创建消息或外部通知。 |
| `agent.use` | Agent capability/session | LOW | 读取允许的 Agent 能力并使用本人会话。 |
| `auth.permission.change` | Auth | CRITICAL | 只能走专用管理界面，不允许 Chat 执行。 |
| `action.approve` | Action Request | HIGH | 只允许有相应资源审批范围的用户。 |

`operation.execute` 必须继续解析底层操作：

- `CREATE`、单对象 `UPDATE` 最低为 MEDIUM；
- `DELETE` 最低为 HIGH；
- `ASSEBMLY`、`WCF`、`JSONPOST`、`JSONGET` 需要根据外部副作用、目标 allowlist、
  请求方法和可回滚性提高到 HIGH 或 CRITICAL；
- 未识别的 operation type 默认 CRITICAL，并拒绝 Agent 执行。

### 7.4 Permission Binding

一个授权 binding 至少包含：

```text
subject_type + subject_id
resource_type + resource_pattern
action
effect: ALLOW | DENY
app_id + database_id
include_children
data_policy_id
valid_from + valid_until
```

明确 DENY 始终优先。没有匹配 ALLOW 时拒绝。多个 ALLOW 的行范围取并集，字段级 DENY
再从允许集合中移除字段，配额取最严格的上限。

### 7.5 数据范围

预置范围：

- `OWN`：记录所有者等于当前用户；
- `DEPARTMENT`：记录部门属于当前部门集合；
- `DEPARTMENT_TREE`：包含授权部门的下级部门；
- `COMPANY`：记录公司等于当前公司；
- `EXPLICIT`：管理员分配的对象 ID 集合；
- `ALL`：当前 app/database 内全部记录；
- `CUSTOM`：受限结构化 DSL。

CUSTOM 示例：

```json
{
  "all": [
    { "field": "company_id", "op": "eqSubject", "value": "companyId" },
    { "field": "state", "op": "in", "value": ["OPEN", "CLOSED"] }
  ]
}
```

允许的 field 必须来自 Model 元数据，允许的操作符使用固定 allowlist。编译结果必须进入
现有结构化 QueryFilter/参数化 SQL 体系，不能拼接原始值。

### 7.6 字段策略

每个 Model Property 可以分别控制：

- `readable`；
- `filterable`；
- `sortable`；
- `exportable`；
- `writable`；
- `llmVisible`；
- `maskStrategy`；
- `classification`。

字段不允许读取时，不能只在序列化末尾删除。查询列、排序、过滤、聚合、报表列、导出列和
LLM 草案都必须使用同一字段策略，避免通过 count、排序或错误信息侧漏。

### 7.7 策略计算

`AuthorizationService.decide(request)` 的确定性顺序：

1. 验证认证状态和 token/session 有效期；
2. 固定 app/database scope；
3. 服务端解析资源和所属关系；
4. 从 Action Catalog 读取最低风险和能力要求；
5. 加载用户、角色、部门和公司 binding；
6. 先应用匹配 DENY，再判断是否存在 ALLOW；
7. 计算行范围、字段策略、结果行数和导出配额；
8. 合并环境条件，例如时间、step-up、新设备和服务状态；
9. 返回结构化 decision、reason code 和 policy version；
10. 记录授权成功或失败事件。中高风险动作在后续执行时重复此流程。

策略可以按 `subject + app + database + policyVersion` 缓存。权限变更时递增 version 并
主动失效缓存，不能只依赖 TTL。

## 8. 查询、报表与数据范围执行

### 8.1 查询

查询执行的最终条件是：

```text
finalFilter = clientValidatedFilter AND policyRowFilter AND resourceBoundaryFilter
```

服务端必须对以下路径应用完全一致的条件：

- `/api/v1/data/query-list`；
- `/api/v1/data/querydata` / `querylist`；
- `/api/v1/data/querydatadetail` / `itemview`；
- `/api/v1/data/inputquery`；
- `/api/v1/report/makereport`；
- Agent 的 query/report preview；
- 导出和后台事件匹配。

详情查询不能只凭 `objId` 返回记录。必须验证该对象满足有效行策略。分页总数、聚合、报表
总数也使用相同过滤条件，不能先查询全部结果再在 Java 内过滤。

### 8.2 报表

- `getmkqview` 只返回允许读取和用于报表的字段；
- `makereport` 只接受服务端元数据中存在且 `readable/filterable` 的列和条件；
- `ReportCols` 必须与运行时 `getmkqview` 结果比较；
- 小分页 preview 是 LOW，大结果和导出根据行数、字段等级提高风险；
- 保存报表定义是 MEDIUM，保存不会自动授予其他用户读取底层数据的权限；
- 打开已保存报表时重新计算当前用户权限，不复用保存人的数据范围。

### 8.3 写入

写入前必须同时满足：

- 对 View/Model 有对应 create/update/delete 权限；
- 目标字段全部 `writable`；
- 原记录在用户当前行范围内；
- 更新后的记录仍满足适用约束，或用户具有明确的跨范围移动权限；
- preview 中的对象版本、字段 diff 和执行时版本一致；
- 中高风险门禁完成。

批量动作必须先解析明确对象集合并记录 count。运行时对象数超过 preview 阈值时，不允许
继续执行，必须重新生成 preview 和审批。

## 9. 内容分级与模型出站策略

权限允许用户读取数据，不代表允许把数据发送到外部模型。字段和上下文使用四级分类：

| 分类 | 示例 | 外部模型策略 |
| --- | --- | --- |
| `PUBLIC` | 公共名称、公开文档 | 可发送。 |
| `INTERNAL` | 普通内部元数据、非敏感统计 | 默认可发送最小必要内容。 |
| `CONFIDENTIAL` | 客户信息、内部金额、人员数据 | 仅批准的 provider，且先脱敏或聚合。 |
| `RESTRICTED` | token、密码、API Key、连接串、密钥、身份证件、原始权限策略 | 永不发送；使用引用或本地 deterministic 路径。 |

未分类字段默认 `INTERNAL`；字段名命中密码、secret、token、key、connection、pwd 等模式时
强制为 `RESTRICTED`，数据库策略只能提高等级。

### 9.1 出站处理顺序

1. 根据用户授权裁剪不可见字段；
2. 根据 classification 删除、掩码或聚合；
3. 将查询结果和文档标记为“不可信数据”，不能作为 system instruction；
4. 只保留当前任务需要的元数据和有限会话历史；
5. 根据 provider policy 判断是否允许发送；
6. 记录 provider、model、分类摘要和字段数量，不记录敏感原文。

`CONFIDENTIAL` 或 `RESTRICTED` 请求无法满足 provider policy 时，系统降级到本地规则、
仅返回元数据建议，或明确拒绝。不能静默切换到另一个外部 provider。

### 9.2 模型输出处理

模型回复是不可信输入。需要执行时必须先解析为 JSON Schema 约束的 `ActionIntent`，拒绝：

- 未注册 action；
- 未注册 resource type；
- 任意 SQL、URL、类名、方法名或凭据；
- 模型提供的 actor、risk、approval、policyVersion、executor；
- 超出长度、数量或类型限制的参数；
- 将工具返回数据解释成新指令的请求。

## 10. 风险分级

### 10.1 等级

| 等级 | 含义 | 默认门禁 |
| --- | --- | --- |
| `LOW` | 小范围、只读、无外部副作用 | 自动执行，授权、限流和审计。 |
| `MEDIUM` | 可回滚的有限写入、保存配置、受限导出 | preview + 发起人显式确认；按策略要求 step-up。 |
| `HIGH` | 删除、批量写入、DDL、凭据路由、外部通知、scheduler | preview + step-up + 独立审批人 + 执行前重检。 |
| `CRITICAL` | 权限提升、破坏性 DDL、任意代码/SQL、不可控外部调用 | Agent 禁止执行，只能通过专用管理流程。 |

### 10.2 风险提高因子

Risk Engine 从 Action Catalog 最低风险开始，取所有提高因子的最高结果：

- 对象数量超过动作阈值；
- 包含 `CONFIDENTIAL` 或 `RESTRICTED` 字段；
- 跨部门、跨公司、跨应用或跨数据库；
- 删除、覆盖、不可逆或无可靠 rollback；
- DDL、凭据、路由、权限或 scheduler 变更；
- 外部网络、消息、邮件、Webhook、WCF 或 JSON side effect；
- 当前设备或会话未完成 step-up；
- preview 无法确定对象集合、接收人或影响行数；
- operation type、handler 或资源归属不明确。

风险计算结果包含稳定 reason code，例如 `BULK_ROW_THRESHOLD`、`SENSITIVE_FIELD`、
`EXTERNAL_SIDE_EFFECT`、`DESTRUCTIVE_DDL`。不能只保存一段模型生成的说明。

## 11. Action Request 协议

### 11.1 ActionIntent

模型或页面首先提交候选意图：

```json
{
  "schemaVersion": 1,
  "action": "data.update",
  "resource": {
    "type": "view",
    "id": "100"
  },
  "arguments": {
    "objectIds": ["10001"],
    "changes": {
      "order_state": 1
    }
  },
  "rationale": "将选中订单标记为已处理"
}
```

服务器补全 actor、canonical resource、scope、handler、risk、policy version 和 hash。

### 11.2 状态机

```text
DRAFT
  -> PREFLIGHT_DENIED
  -> PREVIEW_READY
       -> AWAITING_CONFIRMATION
       -> AWAITING_APPROVAL
       -> APPROVED
       -> CANCELLED
       -> EXPIRED
APPROVED
  -> EXECUTING
       -> SUCCEEDED
       -> FAILED
       -> ROLLED_BACK
       -> PARTIALLY_SUCCEEDED
```

状态只能通过服务端命令迁移。客户端不能直接设置 `APPROVED`、`EXECUTING` 或结果状态。
`PARTIALLY_SUCCEEDED` 只用于无法原子化的外部副作用，并必须包含逐项结果和人工处理说明。

### 11.3 Preflight

Preflight 在生成 preview 前执行：

- 认证和资源授权；
- Handler 是否注册且 action/resource 匹配；
- 参数 schema 和业务前置条件；
- 行/字段权限；
- 内容分类和 provider policy；
- 对象数量、外部接收人和依赖可用性；
- 风险等级和所需审批策略。

Preflight 拒绝仍写入安全审计，但不创建可执行请求。

### 11.4 PreviewBundle

MEDIUM/HIGH 请求必须生成不可变 preview：

```text
actionRequestId
canonicalPayloadHash
policyVersion
riskLevel + riskReasons
resource + effectiveScope
affectedObjectCount + bounded object ids/reference
field diff / metadata diff / DDL diff
recipient summary / external target summary
preconditions + object versions
rollback strategy
warnings
previewExpiresAt
```

preview 不包含原始凭据、完整连接串、密码、token 或被字段策略禁止的数据。对象集合太大时保存
服务端 snapshot reference 和摘要，不把全部 ID 发给模型或浏览器。

### 11.5 Payload Hash

hash 对 canonical JSON 计算，至少覆盖：

```text
actorUserId + appId + databaseId + action + resource
+ normalized arguments + affected object snapshot
+ policyVersion + riskLevel + previewVersion + expiresAt
```

字段排序、数字和时间格式必须规范化。任何字段改变都会产生新的 request，旧确认和审批失效。

## 12. 确认与审批

### 12.1 MEDIUM

- 发起人查看 preview 后显式确认；
- 策略可以要求最近一次 step-up；
- 确认有 TTL，只能使用一次；
- 如果对象版本、策略版本、风险或对象数量变化，回到 `PREVIEW_READY`。

### 12.2 HIGH

- 发起人完成 step-up；
- 至少一个独立审批人，审批人不能与发起人相同；
- 审批人必须拥有目标资源上的 `action.approve` 和对应 scope；
- 高风险策略可以要求两名不同审批人；
- 审批记录绑定 payload hash，不能批准模糊目标或未来一批动作；
- 审批人权限在执行时重新检查，权限已被撤销时审批失效。

### 12.3 CRITICAL

Chat 只允许解释风险、生成不可执行的计划和证据清单。系统不为 CRITICAL ActionIntent
创建 execute capability。专用管理流程仍需要 step-up、双人审批和独立审计。

## 13. 执行

### 13.1 执行前重检

进入 `EXECUTING` 前必须重新验证：

- 当前 token/session 有效；
- actor 仍有业务权限；
- approver 仍有审批权限；
- policy version、payload hash 和 preview 未失效；
- 对象版本和影响范围没有变化；
- idempotency key 未被成功使用；
- Handler 和外部依赖处于允许状态。

### 13.2 Handler 约束

每个 `ControlledActionHandler` 只能声明固定 action/resource 组合，并提供：

```java
supports(action, resourceType)
preflight(subject, request)
preview(subject, request)
execute(subject, approvedRequest)
compensate(subject, executionResult) // optional
```

Handler 不接收模型生成的 SQL、bean name 或 class name。Registry 只从 Spring 中已注册的
allowlist bean 建立，未知 action 默认拒绝。

### 13.3 事务与副作用

- 单数据库业务写入在一个事务中完成；
- 批量写入使用明确上限和幂等 key，超过上限重新审批；
- DDL 需要 schema snapshot、生成 SQL、备份/恢复计划和执行后 schema diff；
- DDL 可能隐式提交，不得声称普通事务可以回滚；
- 消息、Webhook 和外部调用使用 outbox/idempotency key，避免数据库已提交但外部状态未知；
- 执行失败时记录 `FAILED`，只有补偿动作实际成功后才能记录 `ROLLED_BACK`；
- 审计写入失败不能让 HIGH 动作继续执行。

## 14. HTTP API

受控动作使用与 Chat 解耦的中性 API，建议路径：

| Method | Path | 作用 |
| --- | --- | --- |
| `POST` | `/api/v1/actions` | 创建 Action Request，执行 preflight。 |
| `POST` | `/api/v1/actions/{id}/preview` | 生成或刷新 preview。 |
| `GET` | `/api/v1/actions/{id}` | 查询当前状态和可见 preview。 |
| `POST` | `/api/v1/actions/{id}/confirm` | 发起人确认 MEDIUM 请求。 |
| `POST` | `/api/v1/actions/{id}/approvals` | 审批或拒绝 HIGH 请求。 |
| `POST` | `/api/v1/actions/{id}/execute` | 执行已满足门禁的请求。 |
| `POST` | `/api/v1/actions/{id}/cancel` | 发起人或管理员取消未执行请求。 |
| `POST` | `/api/v1/auth/step-up` | 建立短时高风险认证证明。 |
| `GET` | `/api/v1/authz/effective-actions` | 为界面返回当前资源可展示的动作，不替代最终鉴权。 |

Agent message 响应可以返回 `actionRequestId`，但模型不能直接调用 confirm、approve 或 execute。
执行 API 需要显式用户交互或受控后台流程。

错误响应使用稳定 reason code，不返回策略 SQL、角色全量、敏感字段值或内部堆栈，例如：

- `AUTHENTICATION_REQUIRED`；
- `AUTHORIZATION_DENIED`；
- `RESOURCE_OUT_OF_SCOPE`；
- `FIELD_NOT_WRITABLE`；
- `STEP_UP_REQUIRED`；
- `APPROVAL_REQUIRED`；
- `APPROVAL_PAYLOAD_MISMATCH`；
- `PREVIEW_EXPIRED`；
- `POLICY_CHANGED`；
- `IDEMPOTENCY_CONFLICT`。

## 15. 数据模型

不修改旧 migration 的历史语义。实施时新增幂等脚本
`docker/mysql/init/016-authorization-and-agent-control.sql`，负责新表、兼容列和回填。

### 15.1 `FOOL_AUTHZ_PERMISSION`

| 字段 | 说明 |
| --- | --- |
| `PERMISSION_ID` | 稳定主键。 |
| `RESOURCE_TYPE` | View、Model、Field、Operation、DataSource、Event 等。 |
| `RESOURCE_PATTERN` | canonical resource 或受限 pattern。 |
| `ACTION_ID` | Action Catalog ID。 |
| `MIN_RISK` | 可选的额外风险下限，只能高于代码下限。 |
| `ENABLED` | 是否启用。 |
| `CREATED_AT` / `UPDATED_AT` | 管理时间。 |

### 15.2 `FOOL_AUTHZ_BINDING`

| 字段 | 说明 |
| --- | --- |
| `BINDING_ID` | 主键。 |
| `SUBJECT_TYPE` / `SUBJECT_ID` | USER、ROLE、DEPARTMENT、COMPANY。 |
| `PERMISSION_ID` | 权限定义。 |
| `EFFECT` | ALLOW 或 DENY。 |
| `APP_ID` / `DATABASE_ID` | 作用域。 |
| `INCLUDE_CHILDREN` | 是否覆盖明确的资源子级。 |
| `DATA_POLICY_ID` | 可选数据策略。 |
| `VALID_FROM` / `VALID_UNTIL` | 临时授权窗口。 |

### 15.3 `FOOL_AUTHZ_DATA_POLICY`

| 字段 | 说明 |
| --- | --- |
| `DATA_POLICY_ID` | 主键。 |
| `SCOPE_TYPE` | OWN、DEPARTMENT、COMPANY、ALL、CUSTOM 等。 |
| `FILTER_JSON` | 受限 DSL，不是 SQL。 |
| `READABLE_FIELDS_JSON` | 可读字段集合。 |
| `WRITABLE_FIELDS_JSON` | 可写字段集合。 |
| `MASK_FIELDS_JSON` | 字段掩码策略。 |
| `MAX_QUERY_ROWS` / `MAX_EXPORT_ROWS` | 查询与导出上限。 |
| `LLM_POLICY_JSON` | 字段分类和 provider 限制。 |

### 15.4 `FOOL_AUTHZ_POLICY_VERSION`

保存当前全局或 app/database policy version。权限、binding、数据策略或资源归属变化时原子递增，
用于缓存失效、preview 绑定和执行前重检。

### 15.5 `FOOL_AGENT_ACTION_REQUEST`

| 字段 | 说明 |
| --- | --- |
| `ACTION_REQUEST_ID` | UUID。 |
| `OWNER_USER_ID` | 发起用户。 |
| `AGENT_SESSION_ID` | 可空；页面直接发起时为空。 |
| `SOURCE` | CHAT、UI、SCHEDULER、API。 |
| `APP_ID` / `DATABASE_ID` | 固定作用域。 |
| `ACTION_ID` / `RESOURCE_KEY` | canonical 动作和资源。 |
| `PAYLOAD_JSON` / `PAYLOAD_HASH` | 规范化请求和 hash。 |
| `PREVIEW_JSON` / `PREVIEW_HASH` | 已脱敏 preview 和 hash。 |
| `RISK_LEVEL` / `RISK_REASONS_JSON` | 服务端风险结果。 |
| `POLICY_VERSION` | 授权策略版本。 |
| `STATUS` | 状态机状态。 |
| `IDEMPOTENCY_KEY` | 单次执行与重试控制。 |
| `EXPIRES_AT` | 请求失效时间。 |
| `CREATED_AT` / `UPDATED_AT` | 时间。 |

### 15.6 `FOOL_AGENT_APPROVAL`

| 字段 | 说明 |
| --- | --- |
| `APPROVAL_ID` | UUID。 |
| `ACTION_REQUEST_ID` | 目标请求。 |
| `APPROVER_USER_ID` | 审批人。 |
| `DECISION` | APPROVE 或 REJECT。 |
| `PAYLOAD_HASH` / `PREVIEW_HASH` | 审批绑定内容。 |
| `COMMENT` | 可审计说明，限制长度并防止日志注入。 |
| `APPROVER_POLICY_VERSION` | 审批时权限版本。 |
| `DECIDED_AT` / `EXPIRES_AT` | 时间。 |

### 15.7 `FOOL_SECURITY_AUDIT_EVENT`

| 字段 | 说明 |
| --- | --- |
| `AUDIT_EVENT_ID` / `TRACE_ID` | 事件和链路标识。 |
| `ACTOR_USER_ID` / `SOURCE` | 谁通过何种入口发起。 |
| `AGENT_SESSION_ID` / `ACTION_REQUEST_ID` | 关联上下文。 |
| `ACTION_ID` / `RESOURCE_KEY` | 动作和资源。 |
| `DECISION` / `REASON_CODE` | ALLOW、DENY、DEFER、SUCCESS、FAIL 等。 |
| `RISK_LEVEL` / `POLICY_VERSION` | 决策上下文。 |
| `BEFORE_REF` / `AFTER_REF` | 受保护的 snapshot 引用，不保存敏感正文。 |
| `REMOTE_ADDRESS_HASH` / `USER_AGENT` | 安全分析字段。 |
| `CREATED_AT` | 服务端时间。 |

安全审计和业务审计分开存储。安全审计采用 append-only 权限，限制读取者，并定期复制到独立
日志系统。后续阶段可以增加 hash chain 或签名，提高篡改可检测性。

### 15.8 Agent Session 迁移

`FOOL_AGENT_SESSION` 迁移步骤：

1. 新增 `OWNER_USER_ID`、`APP_ID`、`DATABASE_ID`、`AUTH_SESSION_ID`；
2. 新会话创建前先认证并写入 owner/scope；
3. 旧会话首次访问时解析现有 token，成功后回填 owner 并清空 `SESSION_TOKEN`；
4. 无法认证的旧会话保持只读或直接失效；
5. 兼容窗口结束后，在后续 migration 删除原始 token 列和索引。

## 16. 当前端点的门禁映射

| 当前端点 | 所需动作 | 额外要求 |
| --- | --- | --- |
| `/api/v1/auth/auth-menus`、`getsubmenu` | `view.discover` | 只返回可发现资源。 |
| `/api/v1/view/get-view`、`getlistview`、`getreaditemview` | `view.read` | 裁剪不可读字段和 operation。 |
| `/api/v1/data/query-list`、`querydata`、`querydatadetail` | `view.query` | 强制行/字段策略。 |
| `/api/v1/data/inputquery`、`getenums` | `view.query` | 防止 lookup 跨范围泄漏。 |
| `/api/v1/report/getmkqview`、`makereport` | `report.preview` | 字段、行数和聚合策略。 |
| `/api/v1/report/saverpt` | `report.save` | MEDIUM Action Request。 |
| `/api/v1/data/initnew` | `data.create` preview | 不能因为只返回空表单而泄露不可写字段。 |
| `/api/v1/data/saveobj` | `data.update` | MEDIUM 或按影响升级。 |
| `/api/v1/data/savenewobj` | `data.create` | MEDIUM。 |
| `/api/v1/data/runoperation` | `operation.execute` | 解析 OperationBaseType、对象范围和副作用。 |
| `/api/v1/agent/providers`、`capabilities` | `agent.use` | 认证后返回允许能力。 |
| `/api/v1/agent/sessions/*` | `agent.use` + session ownership | 不接受原始 token 作为 owner 证明。 |

Controller 负责快速拒绝和参数边界，Service/Handler 负责最终资源和数据授权。只在 Controller
加注解不够，因为同一个 Service 还可能被 Agent、scheduler 或其他内部调用路径使用。

## 17. 前端交互

### 17.1 只读能力

前端根据 `effective-actions` 隐藏或禁用不可用命令，但直接请求仍可能被服务端拒绝。权限变化
后重新拉取 capability，不保留旧的“已允许”判断。

### 17.2 中风险

确认界面展示：

- 动作名称和资源；
- 影响对象数；
- 字段或配置 diff；
- 数据范围和敏感等级摘要；
- rollback 说明；
- 到期时间和风险原因。

确认按钮只能提交 request ID，不重新提交一份可被替换的业务 payload。

### 17.3 高风险

高风险界面展示等待审批状态、审批要求和执行结果。审批人使用独立入口查看同一个 immutable
preview。聊天消息中的“同意”“确认”不能被解析成高风险审批。

## 18. 审计、指标与告警

必须记录：

- 登录成功/失败、token 失效和 step-up；
- 授权成功/失败，尤其是资源越界和字段越权；
- Action Request 创建、preview、确认、审批、取消、过期和执行；
- 权限、策略、角色、凭据引用和 scheduler 变更；
- 报表导出和敏感字段访问；
- provider 选择、数据分类摘要和出站拒绝；
- 幂等冲突、payload hash 不匹配和审批重放。

不能记录：

- token、密码、API Key、连接串和密钥；
- 完整 Chat 敏感正文；
- 未脱敏业务行；
- 可以直接重放的 approval/step-up 凭据。

建议指标：

```text
authz_decision_total{decision,action,resource_type,reason}
agent_action_request_total{risk,status,action}
agent_action_latency_seconds{stage,action}
agent_approval_total{decision,risk,action}
agent_outbound_policy_total{provider,classification,decision}
agent_execution_total{result,action,risk}
```

HIGH 动作失败、权限策略变更、连续越权、审批 hash 不匹配和 audit sink 不可用需要告警。

## 19. 故障与降级策略

| 故障 | 行为 |
| --- | --- |
| token/Redis 不可用 | 受保护请求拒绝。 |
| 授权策略库不可用 | 默认拒绝；健康检查标记 authz unavailable。 |
| policy cache 失效失败 | 直接查询策略库或拒绝，不使用无法证明有效的旧缓存执行 HIGH 动作。 |
| LLM provider 不可用 | 保留本地 deterministic 草案；不自动切换敏感数据到其他 provider。 |
| preview provider 不可用 | 不进入确认或审批。 |
| audit sink 不可用 | LOW read 可按策略降级；MEDIUM/HIGH 拒绝执行。 |
| 外部 side effect 超时 | 标记结果未知，禁止自动重复，依赖 idempotency 查询和人工处理。 |
| approval service 不可用 | HIGH 保持等待，不绕过审批。 |
| object/policy version 变化 | preview 和 approval 失效，重新生成。 |

## 20. 迁移与发布计划

### Phase 0: 身份与默认拒绝

- 引入 bearer 认证过滤器和 `EffectiveSubject`；
- 给 token 增加 TTL、hash 存储、轮换和吊销；
- 移除 token 日志，开始密码 hash 渐进迁移；
- 建立 `AuthorizationService` SPI 和 deny-by-default 实现；
- 明确 public allowlist；
- 新增授权表、policy version 和安全审计；
- Agent 新会话保存 owner/scope，不保存原始 token。

完成条件：未经认证或只有已知 ViewId/SessionId 的请求不能读取或修改受保护资源。

### Phase 1: 统一只读权限

- 菜单、View、查询、详情、lookup、报表和 Agent metadata 使用同一授权；
- 实施行级策略、字段裁剪、掩码和 query/export 上限；
- 完成三个现有 runtime preview/dry-run gate；
- 增加模型出站分类、脱敏和 provider policy；
- 对直接 API 与 Chat 做权限一致性测试。

完成条件：同一用户从页面、报表和 Chat 看到的数据集合及字段完全一致。

### Phase 2: 中风险动作

- 实现 Action Request、risk、preview、payload hash、confirm、idempotency 和审计；
- 首先接入 `report.save`、受限 `report.export`、单对象 `data.create/update`；
- 页面和 Agent 都改走 `/api/v1/actions`；
- 执行时重新鉴权和版本检查。

完成条件：修改 payload、权限变化、对象版本变化、过期和重放都不能执行。

### Phase 3: 高风险动作

- 引入 step-up、独立审批人、双人审批策略和审批权限；
- 逐项接入 delete、批量 update、Operation、非破坏性 DDL、数据源路由、事件启用和消息发送；
- 为外部副作用增加 outbox、idempotency 和人工恢复路径；
- CRITICAL 动作保持 Agent 禁止。

完成条件：每一个开放的 HIGH action 都有专用 Handler、preview、审批、回滚/补偿说明和运行时证据。

### Phase 4: 收口与强化

- 删除 body token 和 Agent session 原始 token 兼容；
- 强化审计防篡改和集中告警；
- 增加故障注入、并发、缓存失效和策略变更测试；
- 对角色和权限做周期性 review；
- 对 Action Catalog 和运行时开放能力做自动差异检查。

## 21. 测试策略

### 21.1 单元测试

- Allow、Deny、无匹配、Deny 优先和资源子级继承；
- 多角色行范围并集、字段 DENY 和最严格配额；
- CUSTOM DSL 字段/操作符 allowlist 和参数化编译；
- risk floor 和所有提高因子；
- canonical JSON 与 payload hash 稳定性；
- 状态机非法迁移、过期、重放和幂等；
- HIGH 自审拒绝、审批权限撤销和 policy version 变化；
- content classification、mask 和 provider deny。

### 21.2 集成测试

- 直接访问已知 ViewId 不能绕过菜单和资源权限；
- 列表、详情、lookup、报表 count、导出和 Agent preview 使用相同行范围；
- 不可读字段不能用于过滤、排序、聚合或发送给模型；
- 保存和 runoperation 在 Controller、Service 和 Handler 路径都被保护；
- Agent Session 不能被其他用户或空 token 读取；
- 修改 action payload 后旧确认/审批失效；
- 同一 idempotency key 不产生重复写入或重复消息；
- audit sink 故障阻止 MEDIUM/HIGH 执行。

### 21.3 运行时验证

- Docker migration 在新库和已有 volume 上幂等；
- `python scripts/runtime_doctor.py` 增加认证、越权、数据范围和 Action 状态检查；
- 浏览器验证普通用户、部门管理员、审批人和系统管理员四种身份；
- 保存 HTTP 请求、授权 decision、preview、approval、执行结果和审计 trace 的证据 bundle；
- 验证 provider request 不包含 token、连接串、RESTRICTED 字段或未授权字段；
- 验证权限撤销后缓存及时失效。

## 22. 验收标准

整体设计只有同时满足以下条件才算落地：

1. 默认拒绝在所有受保护 Controller 和内部 Service 调用路径上生效；
2. Agent 权限不超过发起用户，审批人也不能通过审批扩大资源范围；
3. 页面、报表、导出和 Chat 的行/字段集合一致；
4. LOW、MEDIUM、HIGH、CRITICAL 有代码所有的稳定规则；
5. MEDIUM/HIGH 只能执行与确认/审批 hash 完全一致的 immutable request；
6. HIGH 需要有效 step-up 和独立审批，CRITICAL 不能由 Agent 执行；
7. 凭据和 RESTRICTED 数据不进入模型、session、approval 或普通日志；
8. 每个动作可以通过 trace ID 还原授权、风险、preview、审批和执行结果；
9. 外部副作用具备幂等或明确的人工恢复路径；
10. 兼容旧菜单和旧请求的同时，直接调用接口不能绕过授权；
11. `mvn test`、frontend test/build、repo harness 和 Docker runtime doctor 全部通过；
12. 每个开放的中高风险 action 都有独立验收证据，而不是一次性开放万能执行器。

## 23. 安全基线参考

- [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
  的 default deny、least privilege 和每次请求验证原则；
- [NIST SP 800-162](https://csrc.nist.gov/pubs/sp/800/162/upd2/final)
  的 Subject、Object、Operation 和 Environment 属性授权模型；
- [OWASP LLM06:2025 Excessive Agency](https://genai.owasp.org/llmrisk/llm062025-excessive-agency/)
  的下游系统授权、用户上下文执行和高影响动作人工审批要求；
- [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)
  的认证、授权失败、高风险功能、数据导出和敏感信息排除要求。
