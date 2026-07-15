-- Reconcile imported display properties with their physical legacy storage.

UPDATE `fool_sys_model_property` property
JOIN `fool_sys_model` model
  ON model.`id` = property.`owner`
SET property.`column` = NULL
WHERE model.`name` = 'WorkingDatabase'
  AND property.`name` = 'password';

UPDATE `fool_sys_model_property` property
JOIN `fool_sys_model` model
  ON model.`id` = property.`owner`
SET property.`column` = NULL
WHERE model.`name` = 'EventDefinition'
  AND property.`name` = 'operationName';

UPDATE `fool_sys_model_property` property
JOIN `fool_sys_model` model
  ON model.`id` = property.`owner`
SET property.`column` = 'EVT_Defination'
WHERE model.`name` = 'EventRecord'
  AND property.`name` = 'definition';
