-- Keep imported list-view models queryable when legacy metadata owns the row id.

DROP PROCEDURE IF EXISTS `ensure_legacy_list_model_sysids`;

DELIMITER //

CREATE PROCEDURE `ensure_legacy_list_model_sysids`()
BEGIN
  DECLARE finished INT DEFAULT 0;
  DECLARE target_table VARCHAR(255);
  DECLARE target_tables CURSOR FOR
    SELECT DISTINCT model.`table_name`
    FROM `fool_sys_model` model
    JOIN `SW_SYS_VIEW` view_definition
      ON view_definition.`VIEW_MODEL` = model.`id`
     AND view_definition.`VIEW_TYPE` = 0
    JOIN information_schema.`TABLES` runtime_table
      ON runtime_table.`TABLE_SCHEMA` = DATABASE()
     AND BINARY runtime_table.`TABLE_NAME` = BINARY model.`table_name`
    LEFT JOIN information_schema.`COLUMNS` sysid_column
      ON sysid_column.`TABLE_SCHEMA` = DATABASE()
     AND BINARY sysid_column.`TABLE_NAME` = BINARY model.`table_name`
     AND LOWER(sysid_column.`COLUMN_NAME`) = 'sysid'
    WHERE model.`auto_sys_id` = 1
      AND model.`id_property` IS NULL
      AND sysid_column.`COLUMN_NAME` IS NULL
    ORDER BY model.`table_name`;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

  OPEN target_tables;
  add_sysid: LOOP
    FETCH target_tables INTO target_table;
    IF finished = 1 THEN
      LEAVE add_sysid;
    END IF;
    SET @identity_ddl = CONCAT(
      'ALTER TABLE `', REPLACE(target_table, '`', '``'),
      '` ADD COLUMN `SysId` BIGINT NOT NULL AUTO_INCREMENT UNIQUE'
    );
    PREPARE identity_statement FROM @identity_ddl;
    EXECUTE identity_statement;
    DEALLOCATE PREPARE identity_statement;
  END LOOP;
  CLOSE target_tables;
END//

DELIMITER ;

CALL `ensure_legacy_list_model_sysids`();
DROP PROCEDURE `ensure_legacy_list_model_sysids`;
