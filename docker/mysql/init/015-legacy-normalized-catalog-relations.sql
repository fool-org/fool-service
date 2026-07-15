-- Align imported normalized catalog metadata with the physical runtime schema.

UPDATE `fool_sys_model` model
JOIN (
  SELECT property.`owner`, MIN(property.`id`) AS `id_property`
  FROM `fool_sys_model_property` property
  JOIN `fool_sys_model` owner_model
    ON owner_model.`id` = property.`owner`
   AND owner_model.`table_name` LIKE 'fool\_sys\_%'
  JOIN (
    SELECT `TABLE_NAME`
    FROM information_schema.`COLUMNS`
    WHERE `TABLE_SCHEMA` = DATABASE()
      AND `COLUMN_KEY` = 'PRI'
    GROUP BY BINARY `TABLE_NAME`, `TABLE_NAME`
    HAVING COUNT(*) = 1
  ) single_primary_key
    ON BINARY single_primary_key.`TABLE_NAME` = BINARY owner_model.`table_name`
  JOIN information_schema.`COLUMNS` primary_column
    ON primary_column.`TABLE_SCHEMA` = DATABASE()
   AND BINARY primary_column.`TABLE_NAME` = BINARY owner_model.`table_name`
   AND primary_column.`COLUMN_KEY` = 'PRI'
   AND LOWER(primary_column.`COLUMN_NAME`) = LOWER(property.`column`)
  GROUP BY property.`owner`
) physical_key
  ON physical_key.`owner` = model.`id`
SET model.`id_property` = physical_key.`id_property`,
    model.`auto_sys_id` = 0
WHERE model.`id_property` IS NULL
   OR model.`id_property` <> physical_key.`id_property`;

UPDATE `SW_SYS_RELATION` relation_definition
JOIN `fool_sys_model_property` property
  ON property.`id` = relation_definition.`SW_SYS_RELATION_SOURCEPROPERTY`
 AND property.`is_collection` = 1
JOIN `fool_sys_model` target_model
  ON target_model.`id` = property.`property_model`
LEFT JOIN information_schema.`COLUMNS` current_column
  ON current_column.`TABLE_SCHEMA` = DATABASE()
 AND BINARY current_column.`TABLE_NAME` = BINARY target_model.`table_name`
 AND LOWER(current_column.`COLUMN_NAME`) = LOWER(relation_definition.`SW_SYS_RELATION_TARGETCOL`)
JOIN information_schema.`COLUMNS` mapped_column
  ON mapped_column.`TABLE_SCHEMA` = DATABASE()
 AND BINARY mapped_column.`TABLE_NAME` = BINARY target_model.`table_name`
 AND LOWER(mapped_column.`COLUMN_NAME`) = LOWER(property.`column`)
SET relation_definition.`SW_SYS_RELATION_TABLE` = target_model.`table_name`,
    relation_definition.`SW_SYS_RELATION_TARGETCOL` = property.`column`
WHERE relation_definition.`SW_SYS_RELATION_TYPE` = 0
  AND current_column.`COLUMN_NAME` IS NULL;
