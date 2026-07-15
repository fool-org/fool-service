-- Project imported FoolFrame View metadata into the normalized runtime catalog.
-- Existing normalized records win so local/runtime overrides are not rewritten.

INSERT IGNORE INTO `fool_sys_view` (
  `id`, `view_name`, `view_text`, `view_remark`, `view_title`, `view_type`,
  `view_model`, `filter`, `auto_fresh_interval`, `view_model_class`
)
SELECT
  legacy.`VIEW_ID`,
  legacy.`VIEW_NAME`,
  legacy.`VIEW_NAME`,
  NULL,
  legacy.`VIEW_NAME`,
  legacy.`VIEW_TYPE`,
  CAST(legacy.`VIEW_MODEL` AS CHAR),
  legacy.`VIEW_FILTER`,
  legacy.`VIEW_AUTOFRESHINTERVAL`,
  NULL
FROM `SW_SYS_VIEW` legacy
WHERE legacy.`VIEW_NAME` IS NOT NULL
  AND legacy.`VIEW_NAME` <> '';

INSERT IGNORE INTO `fool_sys_view_item` (
  `id`, `item_name`, `item_label`, `item_legend`, `model_property`,
  `input_type`, `can_edit`, `select_view_name`, `input_regx`, `format_regx`,
  `edit_type`, `show_index`, `width`, `source_expression`, `list_view_id`,
  `edit_view_id`, `selected_view_id`, `view_id`
)
SELECT
  item.`SysId`,
  item.`VIEW_ITEM_NAME`,
  item.`VIEW_ITEM_NAME`,
  COALESCE(item.`VIEW_ITEM_NOTE`, item.`VIEW_ITEM_NAME`),
  COALESCE(NULLIF(property.`PROPERTY_PROPERTYNAME`, ''), property.`PROPERTY_NAME`),
  0,
  NOT item.`VIEW_ITEM_READONLY`,
  selected_view.`VIEW_NAME`,
  NULL,
  item.`VIEW_ITEM_FORMAT`,
  COALESCE(item.`VIEW_ITEM_EDITTYPE`, 0),
  item.`VIEW_ITEM_INDEX`,
  item.`VIEW_ITEM_WIDTH`,
  item.`VIEW_ITEM_SOURCEEXP`,
  item.`VIEW_ITEM_SUBVIEW`,
  item.`VIEW_ITEM_EDITVIEW`,
  item.`VIEW_ITEM_SELECTVIEW`,
  item.`SW_SYS_VIEW_ItemsVIEW_ID`
FROM `SW_SYS_VIEW_ITEM` item
JOIN `SW_SYS_PROPERTY` property
  ON property.`SysId` = item.`VIEW_ITEM_PROPERTY`
LEFT JOIN `SW_SYS_VIEW` selected_view
  ON selected_view.`VIEW_ID` = item.`VIEW_ITEM_SELECTVIEW`
WHERE item.`SW_SYS_VIEW_ItemsVIEW_ID` IS NOT NULL;
