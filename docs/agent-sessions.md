# Agent Sessions

This document defines the current backend boundary for agent-assisted metadata
configuration. The agent layer is intentionally a controlled coordinator over
existing Fool Service metadata modules, not a free-form code generator.
`docs/authorization-and-agent-risk-control.md` is the design source for the
authentication, authorization, data-scope, approval, execution, and audit
boundary required before medium- or high-risk actions are enabled. `tasks.md`
tracks delivery status for both documents.

## Capability Order

Agent sessions must move through capabilities in this order:

1. `report-query` - report and query drafts over `fool-report` and `fool-query`.
2. `form-view` - form, list, detail, and operation View drafts over `fool-view`.
3. `model` - model fields, relations, defaults, validation, and DDL dry-run
   planning over `fool-model`.
4. `data-source` - working database, connection, credential-reference, and
   routing plans over `fool-db-manage`.
5. `event-automation` - event definitions, recipients, idempotency, and audit
   plans over `fool-event`.

The order is part of the contract: a session starts at `report-query`, messages
must target the current capability, and `/advance` moves one step at a time.

## API Surface

- `GET /api/v1/agent/capabilities`
  returns the ordered capability catalog.
- `GET /api/v1/agent/providers`
  returns the DeepSeek/OpenAI model catalog and configured/default flags. API
  keys are never included in this response.
- `POST /api/v1/agent/sessions`
  creates a session with optional `token` / `Token` and `title`.
- `POST /api/v1/agent/sessions/{sessionId}`
  returns the session when the supplied token matches the session token.
- `POST /api/v1/agent/sessions/{sessionId}/messages`
  appends a user message to the current capability and records the selected
  provider reply plus a controlled `draft` payload. Optional `context` can pass
  values such as `ViewId`; optional `provider` selects `deepseek`, `openai`, or
  the explicit deterministic `local` fallback.
- `POST /api/v1/agent/sessions/{sessionId}/advance`
  moves to the next capability or completes the session after
  `event-automation`.

## Provider Configuration

Both providers use the OpenAI-compatible `/chat/completions` wire format through
one server-side client. The default Docker settings are:

| Provider | Base URL | Default model | Required secret |
| --- | --- | --- | --- |
| DeepSeek | `https://api.deepseek.com` | `deepseek-v4-flash` | `DEEPSEEK_API_KEY` |
| OpenAI | `https://api.openai.com/v1` | `gpt-5-mini` | `OPENAI_API_KEY` |

`FOOL_AGENT_DEFAULT_PROVIDER` selects the default. Base URL and model can be
overridden with `DEEPSEEK_BASE_URL`, `DEEPSEEK_MODEL`, `OPENAI_BASE_URL`, and
`OPENAI_MODEL`. Secrets remain in backend environment/config binding and are
not stored in agent sessions or exposed by the provider catalog.

If neither key is configured and the request omits `provider`, the existing
deterministic draft summary remains available as a visibly labeled local
fallback. Explicitly requesting an unconfigured provider fails with a clear
configuration error instead of silently changing providers.

The Vue workspace exposes this flow at `/agent`, including provider selection,
ordered stage progression, conversation history, draft summary, risk level,
and validation steps.

## Current Boundary

The implementation provides the capability/provider catalogs, ordered session
state, message history, token matching, JDBC-backed persistence when
`JdbcTemplate` is available, OpenAI-compatible DeepSeek/OpenAI replies, and
deterministic draft output. It does not mutate metadata.

The `report-query` stage currently returns a low-risk read-only draft with:

- `/api/v1/report/getmkqview`
- `/api/v1/report/makereport`
- `/api/v1/data/querydata`
- a View-scoped draft request with default `ReportCols`
- a read-only metadata snapshot from `fool_sys_view`,
  `fool_sys_view_item`, `fool_sys_model`, and `fool_sys_model_property`
- candidate columns marked as reportable or non-reportable, so collection
  fields are visible but excluded from default report execution drafts
- validation steps for report columns and read-only execution evidence

The `form-view` stage currently returns a medium-risk draft-only payload with:

- `/api/v1/view/getlistview`
- `/api/v1/view/getreaditemview`
- `/api/v1/data/querydatadetail`
- `/api/v1/data/initnew`
- `/api/v1/data/save`
- `/api/v1/data/runoperation`
- a View-scoped field list from the same `fool_sys_view_item` and
  `fool_sys_model_property` metadata used by the report/query stage
- child collection hints from `list_view_id`, `edit_view_id`,
  `selected_view_id`, and collection property metadata
- operation button metadata from `SW_SYS_VIEW_OPERATION` and
  `SW_SYS_OPERATIONVIEW`, including operation id, location, selection
  requirement, and confirmation/success/error text
- validation steps that require `getlistview` / `getreaditemview` comparison
  before any save or operation endpoint is called

The `model` stage currently returns a high-risk dry-run-required draft with:

- a model snapshot from `fool_sys_model`
- property metadata from `fool_sys_model_property`, including DB column,
  property type, nullability, generation/default fields, collection flags, and
  validation flags
- relation metadata from `SW_SYS_RELATION`, with source/target properties and
  relation table/column bindings
- operation metadata from `SW_SYS_OPERATION` and command counts from
  `SW_SYS_COMMANDS`
- a read-only DDL diff plan that lists target table column checks and requires
  generated SQL evidence plus human approval before any metadata or DDL write

The `data-source` stage currently returns a high-risk credential-boundary draft
with:

- a working database catalog from `WorkDataBase`
- application routing from `DB_App` and `DB_AppDB`
- data-source key routing from `DS_DataSourceSet`
- selected route resolution by `DataSourceKey`, `DBNo`, or application name
- a credential policy that exposes only `CredentialConfigured` and a
  `WorkDataBase.DBNo=<db-no>/pwd*` reference, never plaintext `pwd*` values or
  raw connection strings
- a read-only connectivity check plan using `SELECT 1` after credentials are
  resolved server-side

The `event-automation` stage currently returns a high-risk dry-run-required
draft with:

- event definitions from `SW_EVT_DEF`
- model and View resolution through `fool_sys_model`,
  `fool_sys_model_property`, and `fool_sys_view`
- recipient relation counts from the `SW_APP_AUTH_*_SW_EVT_DEF` tables
- a matched-object query preview generated from the configured model table and
  filter
- an idempotency plan using `SW_EVT_EVENT.EVT_Defination + SW_EVT_EVENT.EVT_DEF`
- an audit plan over `SW_EVT_EVENT` and `SW_SYS_MSG`
- `SchedulerMutation=false`, so the stage plans evidence and does not create
  events or send messages

This keeps the first integration safe while the legacy migration is still
closing gaps: all five stages can generate controlled drafts and dry-run
evidence before any model, View, data-source, or event configuration is applied.

## Storage

The runtime auto-configuration uses `JdbcAgentSessionStore` when a `JdbcTemplate`
bean exists. Docker/MySQL schema lives in `docker/mysql/init/011-agent.sql`:

- `FOOL_AGENT_SESSION`
- `FOOL_AGENT_MESSAGE`

Tests and non-database embedded contexts fall back to `InMemoryAgentSessionStore`.

## Required Follow-Up

The authorization and execution prerequisites for every item below are defined
in `docs/authorization-and-agent-risk-control.md`. Completing a preview or
dry-run comparison does not by itself enable the related write path.

- Compare the agent's hydrated `ReportCols` against `/api/v1/report/getmkqview`
  in runtime smoke checks and add a small read-only preview gate before any
  report definitions are saved.
- Compare the agent's hydrated form/view field and operation draft against
  `/api/v1/view/getlistview` in runtime smoke checks.
- Compare the agent's hydrated model draft against `fool_sys_model`,
  `fool_sys_model_property`, and a generated DDL diff before enabling model
  metadata writes.
- Add dry-run, diff, approval, rollback, and audit evidence before allowing
  data-source route/credential writes, event creation, message sending, or
  scheduler mutation.
