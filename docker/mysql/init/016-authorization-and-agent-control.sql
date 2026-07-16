-- Phase 0 authorization, credential, audit, and controlled-action storage.

CREATE TABLE IF NOT EXISTS `FOOL_AUTH_CREDENTIAL` (
  `USER_ID` varchar(64) NOT NULL,
  `PASSWORD_HASH` varchar(255) NOT NULL,
  `ALGORITHM` varchar(32) NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`USER_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AUTHZ_PERMISSION` (
  `PERMISSION_ID` varchar(64) NOT NULL,
  `RESOURCE_TYPE` varchar(64) NOT NULL,
  `RESOURCE_PATTERN` varchar(512) NOT NULL,
  `ACTION_ID` varchar(128) NOT NULL,
  `MIN_RISK` varchar(32) DEFAULT NULL,
  `ENABLED` tinyint(1) NOT NULL DEFAULT 1,
  `CREATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `UPDATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`PERMISSION_ID`),
  KEY `ix_fool_authz_permission_action` (`ACTION_ID`, `RESOURCE_TYPE`, `ENABLED`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AUTHZ_DATA_POLICY` (
  `DATA_POLICY_ID` varchar(64) NOT NULL,
  `SCOPE_TYPE` varchar(32) NOT NULL,
  `FILTER_JSON` json DEFAULT NULL,
  `READABLE_FIELDS_JSON` json DEFAULT NULL,
  `WRITABLE_FIELDS_JSON` json DEFAULT NULL,
  `MASK_FIELDS_JSON` json DEFAULT NULL,
  `MAX_QUERY_ROWS` int DEFAULT NULL,
  `MAX_EXPORT_ROWS` int DEFAULT NULL,
  `LLM_POLICY_JSON` json DEFAULT NULL,
  `CREATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `UPDATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`DATA_POLICY_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AUTHZ_BINDING` (
  `BINDING_ID` varchar(64) NOT NULL,
  `SUBJECT_TYPE` varchar(32) NOT NULL,
  `SUBJECT_ID` varchar(128) NOT NULL,
  `PERMISSION_ID` varchar(64) NOT NULL,
  `EFFECT` varchar(16) NOT NULL,
  `APP_ID` varchar(128) NOT NULL,
  `DATABASE_ID` varchar(128) NOT NULL,
  `INCLUDE_CHILDREN` tinyint(1) NOT NULL DEFAULT 0,
  `DATA_POLICY_ID` varchar(64) DEFAULT NULL,
  `VALID_FROM` datetime(6) DEFAULT NULL,
  `VALID_UNTIL` datetime(6) DEFAULT NULL,
  `CREATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `UPDATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`BINDING_ID`),
  KEY `ix_fool_authz_binding_subject` (`SUBJECT_TYPE`, `SUBJECT_ID`),
  KEY `ix_fool_authz_binding_scope` (`APP_ID`, `DATABASE_ID`, `PERMISSION_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AUTHZ_POLICY_VERSION` (
  `APP_ID` varchar(128) NOT NULL,
  `DATABASE_ID` varchar(128) NOT NULL,
  `POLICY_VERSION` bigint NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`APP_ID`, `DATABASE_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AGENT_ACTION_REQUEST` (
  `ACTION_REQUEST_ID` varchar(64) NOT NULL,
  `OWNER_USER_ID` varchar(64) NOT NULL,
  `AGENT_SESSION_ID` varchar(64) DEFAULT NULL,
  `SOURCE` varchar(32) NOT NULL,
  `APP_ID` varchar(128) NOT NULL,
  `DATABASE_ID` varchar(128) NOT NULL,
  `ACTION_ID` varchar(128) NOT NULL,
  `RESOURCE_KEY` varchar(512) NOT NULL,
  `PAYLOAD_JSON` json NOT NULL,
  `PAYLOAD_HASH` char(64) NOT NULL,
  `PREVIEW_JSON` json DEFAULT NULL,
  `PREVIEW_HASH` char(64) DEFAULT NULL,
  `RISK_LEVEL` varchar(32) NOT NULL,
  `RISK_REASONS_JSON` json NOT NULL,
  `POLICY_VERSION` bigint NOT NULL,
  `STATUS` varchar(32) NOT NULL,
  `IDEMPOTENCY_KEY` varchar(128) DEFAULT NULL,
  `EXPIRES_AT` datetime(6) NOT NULL,
  `CREATED_AT` datetime(6) NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`ACTION_REQUEST_ID`),
  UNIQUE KEY `uk_fool_agent_action_idempotency` (`OWNER_USER_ID`, `IDEMPOTENCY_KEY`),
  KEY `ix_fool_agent_action_owner_status` (`OWNER_USER_ID`, `STATUS`, `UPDATED_AT`),
  KEY `ix_fool_agent_action_session` (`AGENT_SESSION_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_SAVED_REPORT` (
  `SAVED_REPORT_ID` varchar(64) NOT NULL,
  `OWNER_USER_ID` varchar(64) NOT NULL,
  `APP_ID` varchar(128) NOT NULL,
  `DATABASE_ID` varchar(128) NOT NULL,
  `VIEW_ID` varchar(64) NOT NULL,
  `REPORT_NAME` varchar(120) NOT NULL,
  `DEFINITION_JSON` json NOT NULL,
  `VERSION` bigint NOT NULL DEFAULT 1,
  `CREATED_AT` datetime(6) NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`SAVED_REPORT_ID`),
  UNIQUE KEY `uk_fool_saved_report_owner_name`
    (`OWNER_USER_ID`, `APP_ID`, `DATABASE_ID`, `VIEW_ID`, `REPORT_NAME`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AGENT_APPROVAL` (
  `APPROVAL_ID` varchar(64) NOT NULL,
  `ACTION_REQUEST_ID` varchar(64) NOT NULL,
  `APPROVER_USER_ID` varchar(64) NOT NULL,
  `DECISION` varchar(16) NOT NULL,
  `PAYLOAD_HASH` char(64) NOT NULL,
  `PREVIEW_HASH` char(64) NOT NULL,
  `COMMENT` varchar(1000) DEFAULT NULL,
  `APPROVER_POLICY_VERSION` bigint NOT NULL,
  `DECIDED_AT` datetime(6) NOT NULL,
  `EXPIRES_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`APPROVAL_ID`),
  KEY `ix_fool_agent_approval_request` (`ACTION_REQUEST_ID`, `DECIDED_AT`),
  KEY `ix_fool_agent_approval_approver` (`APPROVER_USER_ID`, `DECIDED_AT`)
);

CREATE TABLE IF NOT EXISTS `FOOL_DATASOURCE_CREDENTIAL_REF` (
  `DS_KEY` varchar(128) NOT NULL,
  `CREDENTIAL_REF` varchar(255) NOT NULL,
  `UPDATED_BY` varchar(64) NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`DS_KEY`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AGENT_OUTBOX` (
  `OUTBOX_ID` varchar(64) NOT NULL,
  `ACTION_REQUEST_ID` varchar(64) NOT NULL,
  `EVENT_DEFINITION_ID` varchar(64) NOT NULL,
  `OBJECT_ID` varchar(255) NOT NULL,
  `PAYLOAD_HASH` char(64) NOT NULL,
  `STATUS` varchar(32) NOT NULL,
  `RESULT_JSON` json DEFAULT NULL,
  `CREATED_AT` datetime(6) NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`OUTBOX_ID`),
  UNIQUE KEY `uk_fool_agent_outbox_action` (`ACTION_REQUEST_ID`),
  KEY `ix_fool_agent_outbox_status` (`STATUS`, `UPDATED_AT`)
);

CREATE TABLE IF NOT EXISTS `FOOL_SECURITY_AUDIT_EVENT` (
  `AUDIT_EVENT_ID` varchar(64) NOT NULL,
  `TRACE_ID` varchar(128) NOT NULL,
  `ACTOR_USER_ID` varchar(64) DEFAULT NULL,
  `SOURCE` varchar(32) NOT NULL,
  `AGENT_SESSION_ID` varchar(64) DEFAULT NULL,
  `ACTION_REQUEST_ID` varchar(64) DEFAULT NULL,
  `ACTION_ID` varchar(128) NOT NULL,
  `RESOURCE_KEY` varchar(512) NOT NULL,
  `DECISION` varchar(32) NOT NULL,
  `REASON_CODE` varchar(128) NOT NULL,
  `RISK_LEVEL` varchar(32) DEFAULT NULL,
  `POLICY_VERSION` bigint NOT NULL,
  `BEFORE_REF` varchar(512) DEFAULT NULL,
  `AFTER_REF` varchar(512) DEFAULT NULL,
  `REMOTE_ADDRESS_HASH` char(64) DEFAULT NULL,
  `USER_AGENT` varchar(500) DEFAULT NULL,
  `CREATED_AT` datetime(6) NOT NULL,
  `CHAIN_SEQUENCE` bigint DEFAULT NULL,
  `PREVIOUS_HASH` char(64) DEFAULT NULL,
  `EVENT_HASH` char(64) DEFAULT NULL,
  PRIMARY KEY (`AUDIT_EVENT_ID`),
  KEY `ix_fool_security_audit_trace` (`TRACE_ID`, `CREATED_AT`),
  KEY `ix_fool_security_audit_actor` (`ACTOR_USER_ID`, `CREATED_AT`),
  KEY `ix_fool_security_audit_action` (`ACTION_ID`, `DECISION`, `CREATED_AT`),
  UNIQUE KEY `uk_fool_security_audit_chain_sequence` (`CHAIN_SEQUENCE`)
);

CREATE TABLE IF NOT EXISTS `FOOL_SECURITY_AUDIT_HEAD` (
  `CHAIN_ID` varchar(32) NOT NULL,
  `LAST_EVENT_ID` varchar(64) NOT NULL,
  `LAST_EVENT_HASH` char(64) NOT NULL,
  `EVENT_COUNT` bigint NOT NULL,
  `UPDATED_AT` datetime(6) NOT NULL,
  PRIMARY KEY (`CHAIN_ID`)
);

CREATE TABLE IF NOT EXISTS `FOOL_SECURITY_ALERT` (
  `ALERT_ID` varchar(64) NOT NULL,
  `ALERT_TYPE` varchar(64) NOT NULL,
  `SEVERITY` varchar(16) NOT NULL,
  `REASON_CODE` varchar(128) NOT NULL,
  `DETAILS` varchar(1000) NOT NULL,
  `CREATED_AT` datetime(6) NOT NULL,
  `ACKNOWLEDGED_AT` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ALERT_ID`),
  KEY `ix_fool_security_alert_open` (`ACKNOWLEDGED_AT`, `SEVERITY`, `CREATED_AT`)
);

-- A permanent genesis row makes SELECT ... FOR UPDATE a cross-instance chain
-- serialization point. Existing heads are deliberately never overwritten.
INSERT INTO `FOOL_SECURITY_AUDIT_HEAD`
  (`CHAIN_ID`, `LAST_EVENT_ID`, `LAST_EVENT_HASH`, `EVENT_COUNT`, `UPDATED_AT`)
VALUES ('primary', '', REPEAT('0', 64), 0, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE `CHAIN_ID` = VALUES(`CHAIN_ID`);

DROP PROCEDURE IF EXISTS `ensure_agent_session_subject_columns`;

DROP PROCEDURE IF EXISTS `ensure_agent_approval_unique_approver`;

DROP PROCEDURE IF EXISTS `ensure_security_audit_hash_columns`;

DELIMITER //

CREATE PROCEDURE `ensure_security_audit_hash_columns`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_SECURITY_AUDIT_EVENT'
       AND `COLUMN_NAME` = 'CHAIN_SEQUENCE'
  ) THEN
    ALTER TABLE `FOOL_SECURITY_AUDIT_EVENT` ADD COLUMN `CHAIN_SEQUENCE` bigint DEFAULT NULL;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_SECURITY_AUDIT_EVENT'
       AND `COLUMN_NAME` = 'PREVIOUS_HASH'
  ) THEN
    ALTER TABLE `FOOL_SECURITY_AUDIT_EVENT` ADD COLUMN `PREVIOUS_HASH` char(64) DEFAULT NULL;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_SECURITY_AUDIT_EVENT'
       AND `COLUMN_NAME` = 'EVENT_HASH'
  ) THEN
    ALTER TABLE `FOOL_SECURITY_AUDIT_EVENT` ADD COLUMN `EVENT_HASH` char(64) DEFAULT NULL;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`STATISTICS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_SECURITY_AUDIT_EVENT'
       AND `INDEX_NAME` = 'uk_fool_security_audit_chain_sequence'
  ) THEN
    ALTER TABLE `FOOL_SECURITY_AUDIT_EVENT`
      ADD UNIQUE KEY `uk_fool_security_audit_chain_sequence` (`CHAIN_SEQUENCE`);
  END IF;
END//

DELIMITER ;

CALL `ensure_security_audit_hash_columns`();
DROP PROCEDURE `ensure_security_audit_hash_columns`;

DELIMITER //

CREATE PROCEDURE `ensure_agent_session_subject_columns`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `COLUMN_NAME` = 'OWNER_USER_ID'
  ) THEN
    ALTER TABLE `FOOL_AGENT_SESSION` ADD COLUMN `OWNER_USER_ID` varchar(64) DEFAULT NULL AFTER `SESSION_ID`;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `COLUMN_NAME` = 'APP_ID'
  ) THEN
    ALTER TABLE `FOOL_AGENT_SESSION` ADD COLUMN `APP_ID` varchar(128) DEFAULT NULL AFTER `OWNER_USER_ID`;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `COLUMN_NAME` = 'DATABASE_ID'
  ) THEN
    ALTER TABLE `FOOL_AGENT_SESSION` ADD COLUMN `DATABASE_ID` varchar(128) DEFAULT NULL AFTER `APP_ID`;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `COLUMN_NAME` = 'AUTH_SESSION_ID'
  ) THEN
    ALTER TABLE `FOOL_AGENT_SESSION` ADD COLUMN `AUTH_SESSION_ID` varchar(128) DEFAULT NULL AFTER `DATABASE_ID`;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`STATISTICS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `INDEX_NAME` = 'ix_fool_agent_session_owner'
  ) THEN
    ALTER TABLE `FOOL_AGENT_SESSION`
      ADD KEY `ix_fool_agent_session_owner` (`OWNER_USER_ID`, `APP_ID`, `DATABASE_ID`, `UPDATED_AT`);
  END IF;
END//

DELIMITER ;

CALL `ensure_agent_session_subject_columns`();
DROP PROCEDURE `ensure_agent_session_subject_columns`;

DELIMITER //

CREATE PROCEDURE `ensure_agent_approval_unique_approver`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.`STATISTICS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_APPROVAL'
       AND `INDEX_NAME` = 'uk_fool_agent_approval_request_approver'
  ) THEN
    ALTER TABLE `FOOL_AGENT_APPROVAL`
      ADD UNIQUE KEY `uk_fool_agent_approval_request_approver`
        (`ACTION_REQUEST_ID`, `APPROVER_USER_ID`);
  END IF;
END//

DELIMITER ;

CALL `ensure_agent_approval_unique_approver`();
DROP PROCEDURE `ensure_agent_approval_unique_approver`;

-- Existing raw-token sessions cannot prove an owner after this boundary. Invalidate
-- them, then remove the compatibility column so future code cannot write secrets.
DELIMITER //

CREATE PROCEDURE `remove_agent_session_token_column`()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'FOOL_AGENT_SESSION'
       AND `COLUMN_NAME` = 'SESSION_TOKEN'
  ) THEN
    UPDATE `FOOL_AGENT_SESSION`
       SET `SESSION_TOKEN` = NULL
     WHERE `SESSION_TOKEN` IS NOT NULL;
    ALTER TABLE `FOOL_AGENT_SESSION` DROP COLUMN `SESSION_TOKEN`;
  END IF;
END//

DELIMITER ;

CALL `remove_agent_session_token_column`();
DROP PROCEDURE `remove_agent_session_token_column`;

INSERT INTO `FOOL_AUTHZ_POLICY_VERSION` (`APP_ID`, `DATABASE_ID`, `POLICY_VERSION`, `UPDATED_AT`)
VALUES
  ('*', '*', 1, CURRENT_TIMESTAMP(6)),
  ('fool-service', 'car_wash', 3, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
  `POLICY_VERSION` = GREATEST(`POLICY_VERSION`, VALUES(`POLICY_VERSION`)),
  `UPDATED_AT` = VALUES(`UPDATED_AT`);

INSERT INTO `FOOL_AUTHZ_PERMISSION`
  (`PERMISSION_ID`, `RESOURCE_TYPE`, `RESOURCE_PATTERN`, `ACTION_ID`, `MIN_RISK`, `ENABLED`)
VALUES ('agent-use', 'AgentCapability', 'agent:capability:*', 'agent.use', 'LOW', 1)
ON DUPLICATE KEY UPDATE
  `RESOURCE_TYPE` = VALUES(`RESOURCE_TYPE`),
  `RESOURCE_PATTERN` = VALUES(`RESOURCE_PATTERN`),
  `ACTION_ID` = VALUES(`ACTION_ID`),
  `MIN_RISK` = VALUES(`MIN_RISK`),
  `ENABLED` = VALUES(`ENABLED`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_DATA_POLICY`
  (`DATA_POLICY_ID`, `SCOPE_TYPE`, `FILTER_JSON`, `READABLE_FIELDS_JSON`,
   `WRITABLE_FIELDS_JSON`, `MASK_FIELDS_JSON`, `MAX_QUERY_ROWS`,
   `MAX_EXPORT_ROWS`, `LLM_POLICY_JSON`)
VALUES
  ('admin-read-all', 'ALL', JSON_OBJECT(), JSON_ARRAY('*'),
   JSON_ARRAY(), JSON_OBJECT(), 500, 5000,
   JSON_OBJECT(
     'llmVisibleFields', JSON_ARRAY('*'),
     'allowedProviders', JSON_ARRAY('local', 'deepseek', 'openai'),
     'classifications', JSON_OBJECT())),
  ('admin-medium', 'ALL', JSON_OBJECT(), JSON_ARRAY('*'),
   JSON_ARRAY('*'), JSON_OBJECT(), 500, 500,
   JSON_OBJECT(
     'filterableFields', JSON_ARRAY('*'),
     'sortableFields', JSON_ARRAY('*'),
     'exportableFields', JSON_ARRAY('*'),
     'llmVisibleFields', JSON_ARRAY('*'),
     'allowedProviders', JSON_ARRAY('local', 'deepseek', 'openai'),
     'classifications', JSON_OBJECT()))
ON DUPLICATE KEY UPDATE
  `SCOPE_TYPE` = VALUES(`SCOPE_TYPE`),
  `FILTER_JSON` = VALUES(`FILTER_JSON`),
  `READABLE_FIELDS_JSON` = VALUES(`READABLE_FIELDS_JSON`),
  `WRITABLE_FIELDS_JSON` = VALUES(`WRITABLE_FIELDS_JSON`),
  `MASK_FIELDS_JSON` = VALUES(`MASK_FIELDS_JSON`),
  `MAX_QUERY_ROWS` = VALUES(`MAX_QUERY_ROWS`),
  `MAX_EXPORT_ROWS` = VALUES(`MAX_EXPORT_ROWS`),
  `LLM_POLICY_JSON` = VALUES(`LLM_POLICY_JSON`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_PERMISSION`
  (`PERMISSION_ID`, `RESOURCE_TYPE`, `RESOURCE_PATTERN`, `ACTION_ID`, `MIN_RISK`, `ENABLED`)
VALUES
  ('view-discover', 'View', 'app:fool-service:db:car_wash:view:*', 'view.discover', 'LOW', 1),
  ('view-read', 'View', 'app:fool-service:db:car_wash:view:*', 'view.read', 'LOW', 1),
  ('view-query', 'View', 'app:fool-service:db:car_wash:view:*', 'view.query', 'LOW', 1),
  ('report-preview', 'View', 'app:fool-service:db:car_wash:view:*', 'report.preview', 'LOW', 1),
  ('model-read', 'Model', 'app:fool-service:db:car_wash:model:*', 'model.read', 'LOW', 1),
  ('report-save', 'View', 'app:fool-service:db:car_wash:view:*', 'report.save', 'MEDIUM', 1),
  ('report-export', 'View', 'app:fool-service:db:car_wash:view:*', 'report.export', 'MEDIUM', 1),
  ('data-create', 'View', 'app:fool-service:db:car_wash:view:*', 'data.create', 'MEDIUM', 1),
  ('data-update', 'View', 'app:fool-service:db:car_wash:view:*', 'data.update', 'MEDIUM', 1),
  ('data-delete', 'View', 'app:fool-service:db:car_wash:view:*', 'data.delete', 'HIGH', 1),
  ('operation-execute', 'Operation', 'app:fool-service:db:car_wash:operation:*', 'operation.execute', 'MEDIUM', 1),
  ('model-ddl-execute', 'Model', 'app:fool-service:db:car_wash:model:*', 'model.ddl.execute', 'HIGH', 1),
  ('datasource-route-update', 'DataSource', 'app:fool-service:db:car_wash:datasource:*', 'datasource.route.update', 'HIGH', 1),
  ('datasource-credential-update', 'DataSource', 'app:fool-service:db:car_wash:datasource:*', 'datasource.credential.update', 'HIGH', 1),
  ('event-enable', 'Event', 'app:fool-service:db:car_wash:event:*', 'event.enable', 'HIGH', 1),
  ('message-send', 'Event', 'app:fool-service:db:car_wash:event:*', 'message.send', 'HIGH', 1),
  ('action-approve', '*', 'app:fool-service:db:car_wash:*', 'action.approve', 'HIGH', 1),
  ('audit-verify', 'Auth', 'auth:audit-integrity', 'audit.verify', 'LOW', 1)
ON DUPLICATE KEY UPDATE
  `RESOURCE_TYPE` = VALUES(`RESOURCE_TYPE`),
  `RESOURCE_PATTERN` = VALUES(`RESOURCE_PATTERN`),
  `ACTION_ID` = VALUES(`ACTION_ID`),
  `MIN_RISK` = VALUES(`MIN_RISK`),
  `ENABLED` = VALUES(`ENABLED`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_BINDING`
  (`BINDING_ID`, `SUBJECT_TYPE`, `SUBJECT_ID`, `PERMISSION_ID`, `EFFECT`,
   `APP_ID`, `DATABASE_ID`, `INCLUDE_CHILDREN`, `DATA_POLICY_ID`)
VALUES ('admin-agent-use', 'USER', 'admin', 'agent-use', 'ALLOW',
        'fool-service', 'car_wash', 0, 'admin-read-all')
ON DUPLICATE KEY UPDATE
  `SUBJECT_TYPE` = VALUES(`SUBJECT_TYPE`),
  `SUBJECT_ID` = VALUES(`SUBJECT_ID`),
  `PERMISSION_ID` = VALUES(`PERMISSION_ID`),
  `EFFECT` = VALUES(`EFFECT`),
  `APP_ID` = VALUES(`APP_ID`),
  `DATABASE_ID` = VALUES(`DATABASE_ID`),
  `DATA_POLICY_ID` = VALUES(`DATA_POLICY_ID`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

INSERT INTO `FOOL_AUTHZ_BINDING`
  (`BINDING_ID`, `SUBJECT_TYPE`, `SUBJECT_ID`, `PERMISSION_ID`, `EFFECT`,
   `APP_ID`, `DATABASE_ID`, `INCLUDE_CHILDREN`, `DATA_POLICY_ID`)
VALUES
  ('admin-view-discover', 'USER', 'admin', 'view-discover', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all'),
  ('admin-view-read', 'USER', 'admin', 'view-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all'),
  ('admin-view-query', 'USER', 'admin', 'view-query', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all'),
  ('admin-report-preview', 'USER', 'admin', 'report-preview', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all'),
  ('admin-model-read', 'USER', 'admin', 'model-read', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all'),
  ('admin-report-save', 'USER', 'admin', 'report-save', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-report-export', 'USER', 'admin', 'report-export', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-data-create', 'USER', 'admin', 'data-create', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-data-update', 'USER', 'admin', 'data-update', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-data-delete', 'USER', 'admin', 'data-delete', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-operation-execute', 'USER', 'admin', 'operation-execute', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-model-ddl-execute', 'USER', 'admin', 'model-ddl-execute', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-datasource-route-update', 'USER', 'admin', 'datasource-route-update', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-datasource-credential-update', 'USER', 'admin', 'datasource-credential-update', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-event-enable', 'USER', 'admin', 'event-enable', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-message-send', 'USER', 'admin', 'message-send', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-medium'),
  ('admin-audit-verify', 'USER', 'admin', 'audit-verify', 'ALLOW',
   'fool-service', 'car_wash', 0, 'admin-read-all')
ON DUPLICATE KEY UPDATE
  `SUBJECT_TYPE` = VALUES(`SUBJECT_TYPE`),
  `SUBJECT_ID` = VALUES(`SUBJECT_ID`),
  `PERMISSION_ID` = VALUES(`PERMISSION_ID`),
  `EFFECT` = VALUES(`EFFECT`),
  `APP_ID` = VALUES(`APP_ID`),
  `DATABASE_ID` = VALUES(`DATABASE_ID`),
  `INCLUDE_CHILDREN` = VALUES(`INCLUDE_CHILDREN`),
  `DATA_POLICY_ID` = VALUES(`DATA_POLICY_ID`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);

-- Approval capability is deliberately assigned to a role that has no default member.
-- Operators must explicitly grant this role to an independent human account.
INSERT INTO `auth_role` (`id`, `name`)
VALUES (9001, 'Agent Action Approver')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `FOOL_AUTHZ_BINDING`
  (`BINDING_ID`, `SUBJECT_TYPE`, `SUBJECT_ID`, `PERMISSION_ID`, `EFFECT`,
   `APP_ID`, `DATABASE_ID`, `INCLUDE_CHILDREN`, `DATA_POLICY_ID`)
VALUES ('agent-approver-role', 'ROLE', 'auth:9001', 'action-approve', 'ALLOW',
        'fool-service', 'car_wash', 0, 'admin-read-all')
ON DUPLICATE KEY UPDATE
  `SUBJECT_TYPE` = VALUES(`SUBJECT_TYPE`),
  `SUBJECT_ID` = VALUES(`SUBJECT_ID`),
  `PERMISSION_ID` = VALUES(`PERMISSION_ID`),
  `EFFECT` = VALUES(`EFFECT`),
  `APP_ID` = VALUES(`APP_ID`),
  `DATABASE_ID` = VALUES(`DATABASE_ID`),
  `INCLUDE_CHILDREN` = VALUES(`INCLUDE_CHILDREN`),
  `DATA_POLICY_ID` = VALUES(`DATA_POLICY_ID`),
  `UPDATED_AT` = CURRENT_TIMESTAMP(6);
