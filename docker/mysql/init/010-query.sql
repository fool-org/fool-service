CREATE TABLE IF NOT EXISTS `SE_COMPARETYPE` (
  `SysID` bigint NOT NULL,
  `SE_COMPARESHOW` varchar(64) NOT NULL,
  `SE_COMPAREEXP` varchar(64) NOT NULL,
  PRIMARY KEY (`SysID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `SE_COMPARETYPE_PROPERTYINDEX` (
  `COMPARETYPE_ID` bigint NOT NULL,
  `PROPERTYTYPE_VALUE` int NOT NULL,
  PRIMARY KEY (`COMPARETYPE_ID`, `PROPERTYTYPE_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `SE_COMPARETYPE` (`SysID`, `SE_COMPARESHOW`, `SE_COMPAREEXP`) VALUES
  (1, '等于', '{0} = {1}'),
  (2, '不等于', '{0} <> {1}'),
  (3, '大于', '{0} > {1}'),
  (4, '大于等于', '{0} >= {1}'),
  (5, '小于', '{0} < {1}'),
  (6, '小于等于', '{0} <= {1}'),
  (7, '包含', '{0} LIKE {1}')
ON DUPLICATE KEY UPDATE
  `SE_COMPARESHOW` = VALUES(`SE_COMPARESHOW`),
  `SE_COMPAREEXP` = VALUES(`SE_COMPAREEXP`);

INSERT IGNORE INTO `SE_COMPARETYPE_PROPERTYINDEX` (`COMPARETYPE_ID`, `PROPERTYTYPE_VALUE`) VALUES
  (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1),
  (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (6, 3),
  (1, 5), (2, 5), (3, 5), (4, 5), (5, 5), (6, 5),
  (1, 6), (2, 6), (3, 6), (4, 6), (5, 6), (6, 6),
  (1, 7), (2, 7), (3, 7), (4, 7), (5, 7), (6, 7),
  (1, 8), (2, 8),
  (1, 11), (2, 11), (7, 11),
  (1, 12), (2, 12), (3, 12), (4, 12), (5, 12), (6, 12),
  (1, 14), (2, 14), (3, 14), (4, 14), (5, 14), (6, 14),
  (1, 15), (2, 15);

CREATE TABLE IF NOT EXISTS `SE_SELECTEDTYPE` (
  `SysID` bigint NOT NULL,
  `SE_SELECTEDSHOW` varchar(64) NOT NULL,
  `SE_SELECTEDEXP` varchar(64) NOT NULL,
  `SE_REQUIREGROUP` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`SysID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `SE_SELECTEDTYPE_PROPERTYINDEX` (
  `SELECTEDTYPE_ID` bigint NOT NULL,
  `PROPERTYTYPE_VALUE` int NOT NULL,
  PRIMARY KEY (`SELECTEDTYPE_ID`, `PROPERTYTYPE_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `SE_SELECTEDTYPE` (`SysID`, `SE_SELECTEDSHOW`, `SE_SELECTEDEXP`, `SE_REQUIREGROUP`) VALUES
  (1, '原值', '{0}', b'1'),
  (2, '计数', 'COUNT({0})', b'0'),
  (3, '求和', 'SUM({0})', b'0'),
  (4, '平均', 'AVG({0})', b'0'),
  (5, '最大', 'MAX({0})', b'0'),
  (6, '最小', 'MIN({0})', b'0')
ON DUPLICATE KEY UPDATE
  `SE_SELECTEDSHOW` = VALUES(`SE_SELECTEDSHOW`),
  `SE_SELECTEDEXP` = VALUES(`SE_SELECTEDEXP`),
  `SE_REQUIREGROUP` = VALUES(`SE_REQUIREGROUP`);

INSERT IGNORE INTO `SE_SELECTEDTYPE_PROPERTYINDEX` (`SELECTEDTYPE_ID`, `PROPERTYTYPE_VALUE`) VALUES
  (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1),
  (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (6, 3),
  (1, 5), (2, 5), (3, 5), (4, 5), (5, 5), (6, 5),
  (1, 6), (2, 6), (3, 6), (4, 6), (5, 6), (6, 6),
  (1, 7), (2, 7), (3, 7), (4, 7), (5, 7), (6, 7),
  (1, 8), (2, 8),
  (1, 11), (2, 11),
  (1, 12), (2, 12), (5, 12), (6, 12),
  (1, 14), (2, 14), (5, 14), (6, 14),
  (1, 15), (2, 15);
