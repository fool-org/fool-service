package org.fool.framework.auth.business.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CredentialService {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CredentialService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> passwordHash(String userId) {
        List<String> hashes = jdbcTemplate.queryForList("""
                SELECT `PASSWORD_HASH`
                  FROM `FOOL_AUTH_CREDENTIAL`
                 WHERE `USER_ID` = ?
                   AND `ALGORITHM` = 'BCRYPT'
                 LIMIT 1
                """, String.class, userId);
        return hashes.stream().findFirst();
    }

    public boolean matches(String rawPassword, String hash) {
        return hash != null && encoder.matches(rawPassword, hash);
    }

    public void store(String userId, String rawPassword) {
        jdbcTemplate.update("""
                INSERT INTO `FOOL_AUTH_CREDENTIAL`
                    (`USER_ID`, `PASSWORD_HASH`, `ALGORITHM`, `UPDATED_AT`)
                VALUES (?, ?, 'BCRYPT', CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE
                    `PASSWORD_HASH` = VALUES(`PASSWORD_HASH`),
                    `ALGORITHM` = VALUES(`ALGORITHM`),
                    `UPDATED_AT` = VALUES(`UPDATED_AT`)
                """, userId, encoder.encode(rawPassword));
    }
}
