-- Project imported FoolFrame model metadata into the normalized runtime catalog.
-- Existing normalized records win so local/runtime overrides are not rewritten.

INSERT IGNORE INTO `fool_sys_model` (
  `id`, `name`, `text`, `remark`, `model_type`, `class_name`, `table_name`,
  `auto_sys_id`, `id_property`, `default_owner`
)
SELECT
  `MODEL_ID`,
  `MODEL_NAME`,
  `MODEL_NAME`,
  NULL,
  COALESCE(`MODEL_TYPE`, 0),
  `MODEL_CLASS`,
  `MODEL_DATABASETABLE`,
  `MODEL_AUTOID`,
  `MODEL_IDPROPERTY`,
  `MODEL_DEFAULTOWNER`
FROM `SW_SYS_MODEL`
WHERE `MODEL_NAME` IS NOT NULL
  AND `MODEL_NAME` <> '';

INSERT IGNORE INTO `fool_sys_model_property` (
  `id`, `name`, `remark`, `property_model`, `is_collection`, `owner`,
  `filter`, `source`, `format`, `column`, `property_type`, `allow_db_null`,
  `is_check`, `ix_group`, `generation_type`, `generation_expression`,
  `default_value`, `multi_map`
)
SELECT
  `SysId`,
  COALESCE(NULLIF(`PROPERTY_PROPERTYNAME`, ''), `PROPERTY_NAME`),
  `PROPERTY_NAME`,
  `PROPERTY_MODEL`,
  `PROPERTY_ISARRAY`,
  `SW_SYS_MODEL_PropertiesSysId`,
  `PROPERTY_FILTER`,
  `PROPERTY_SOURCE`,
  `PROPERTY_FORMAT`,
  `PROPERTY_COLNAME`,
  `PROPERTY_TYPE`,
  `PROPERTY_ALLOWDBNULL`,
  `PROPERTY_ISCHECK`,
  `PROPERTY_IXGRPOUP`,
  `PROPERTY_GENERATIONTYPE`,
  NULL,
  NULL,
  `PROPERTY_MULTIMAP`
FROM `SW_SYS_PROPERTY`
WHERE `SW_SYS_MODEL_PropertiesSysId` IS NOT NULL
  AND COALESCE(NULLIF(`PROPERTY_PROPERTYNAME`, ''), `PROPERTY_NAME`) IS NOT NULL;

INSERT INTO `fool_sys_model_enum` (`name`, `value`, `remark`, `owner`)
SELECT
  legacy.`EMUN_STR`,
  CAST(legacy.`EMUN_VALUE` AS CHAR),
  NULL,
  legacy.`SW_SYS_MODEL_EnumValuesMODEL_ID`
FROM `SW_SYS_EMUNVALUE` legacy
WHERE legacy.`SW_SYS_MODEL_EnumValuesMODEL_ID` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `fool_sys_model_enum` current
    WHERE current.`owner` = legacy.`SW_SYS_MODEL_EnumValuesMODEL_ID`
      AND current.`value` = CAST(legacy.`EMUN_VALUE` AS CHAR)
      AND current.`name` = legacy.`EMUN_STR`
  );
