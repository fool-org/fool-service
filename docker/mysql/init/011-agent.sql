CREATE TABLE IF NOT EXISTS `FOOL_AGENT_SESSION` (
    `SESSION_ID` varchar(64) NOT NULL,
    `SESSION_TOKEN` varchar(255) DEFAULT NULL,
    `SESSION_TITLE` varchar(255) DEFAULT NULL,
    `CURRENT_CAPABILITY` varchar(64) NOT NULL,
    `SESSION_STATUS` varchar(32) NOT NULL,
    `CREATED_AT` datetime(6) NOT NULL,
    `UPDATED_AT` datetime(6) NOT NULL,
    PRIMARY KEY (`SESSION_ID`),
    KEY `ix_fool_agent_session_token` (`SESSION_TOKEN`),
    KEY `ix_fool_agent_session_status` (`SESSION_STATUS`)
);

CREATE TABLE IF NOT EXISTS `FOOL_AGENT_MESSAGE` (
    `MESSAGE_ID` varchar(64) NOT NULL,
    `SESSION_ID` varchar(64) NOT NULL,
    `MESSAGE_INDEX` int NOT NULL,
    `MSG_ROLE` varchar(32) NOT NULL,
    `CAPABILITY` varchar(64) NOT NULL,
    `CONTENT` text NOT NULL,
    `CREATED_AT` datetime(6) NOT NULL,
    PRIMARY KEY (`MESSAGE_ID`),
    KEY `ix_fool_agent_message_session` (`SESSION_ID`, `MESSAGE_INDEX`)
);
