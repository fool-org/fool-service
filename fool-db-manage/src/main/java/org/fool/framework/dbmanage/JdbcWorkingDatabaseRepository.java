package org.fool.framework.dbmanage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcWorkingDatabaseRepository implements WorkingDatabaseRepository {
    private static final String SELECT_COLUMNS = """
            SELECT `DBID`, `DBName`, `DBYear`, `DBSysName`, `IsActive`, `DBNo`,
                   `pwd1`, `pwd2`, `pwd3`, `pwd4`, `pwd5`, `UserName`,
                   `CompanyName`, `ServerIp`, `IsLocal`
              FROM `WorkDataBase`
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WorkingDatabase> rowMapper = new WorkingDatabaseRowMapper();

    public JdbcWorkingDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<WorkingDatabase> findAll() {
        return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY `DBNo`", rowMapper);
    }

    @Override
    public List<WorkingDatabase> findByAppName(String appName) {
        return jdbcTemplate.query("""
                        SELECT w.`DBID`, w.`DBName`, w.`DBYear`, w.`DBSysName`, w.`IsActive`, w.`DBNo`,
                               w.`pwd1`, w.`pwd2`, w.`pwd3`, w.`pwd4`, w.`pwd5`, w.`UserName`,
                               w.`CompanyName`, w.`ServerIp`, w.`IsLocal`
                          FROM `WorkDataBase` w
                          JOIN `DB_AppDB` appdb ON w.`DBNo` = appdb.`DBNo`
                          JOIN `DB_App` app ON app.`BO_Id` = appdb.`App_Id`
                         WHERE app.`BO_AppName` = ?
                         ORDER BY w.`DBNo`
                        """,
                rowMapper,
                appName);
    }

    @Override
    public WorkingDatabase findByNameAndYear(String name, String year) {
        return findOne(SELECT_COLUMNS + " WHERE `DBName` = ? AND `DBYear` = ? LIMIT 1", name, year);
    }

    @Override
    public WorkingDatabase findByNameAndYearExceptCode(String name, String year, String code) {
        return findOne(
                SELECT_COLUMNS + " WHERE `DBName` = ? AND `DBYear` = ? AND `DBNo` <> ? LIMIT 1",
                name,
                year,
                code);
    }

    @Override
    public String findMaxCode() {
        return jdbcTemplate.queryForObject("SELECT MAX(`DBNo`) FROM `WorkDataBase`", String.class);
    }

    @Override
    public void insert(WorkingDatabase database) {
        jdbcTemplate.update("""
                        INSERT INTO `WorkDataBase`
                            (`DBName`, `DBYear`, `DBSysName`, `IsActive`, `DBNo`,
                             `pwd1`, `pwd2`, `pwd3`, `pwd4`, `pwd5`,
                             `UserName`, `CompanyName`, `ServerIp`, `IsLocal`)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                database.getName(),
                database.getYear(),
                database.getSysName(),
                database.isActive(),
                database.getCode(),
                database.getEncryptedKey(),
                database.getEncryptedKeyIndex(),
                database.getInitializationVector(),
                database.getInitializationVectorIndex(),
                database.getEncryptedPassword(),
                database.getUserName(),
                database.getCompanyName(),
                database.getServerIp(),
                database.isLocal());
    }

    @Override
    public void update(WorkingDatabase database) {
        jdbcTemplate.update("""
                        UPDATE `WorkDataBase`
                           SET `DBName` = ?,
                               `DBYear` = ?,
                               `DBSysName` = ?,
                               `IsActive` = ?,
                               `pwd1` = ?,
                               `pwd2` = ?,
                               `pwd3` = ?,
                               `pwd4` = ?,
                               `pwd5` = ?,
                               `UserName` = ?,
                               `CompanyName` = ?,
                               `ServerIp` = ?,
                               `IsLocal` = ?
                         WHERE `DBNo` = ?
                        """,
                database.getName(),
                database.getYear(),
                database.getSysName(),
                database.isActive(),
                database.getEncryptedKey(),
                database.getEncryptedKeyIndex(),
                database.getInitializationVector(),
                database.getInitializationVectorIndex(),
                database.getEncryptedPassword(),
                database.getUserName(),
                database.getCompanyName(),
                database.getServerIp(),
                database.isLocal(),
                database.getCode());
    }

    @Override
    public void deleteByCode(String code) {
        jdbcTemplate.update("DELETE FROM `WorkDataBase` WHERE `DBNo` = ?", code);
    }

    private WorkingDatabase findOne(String sql, Object... args) {
        List<WorkingDatabase> items = jdbcTemplate.query(sql, rowMapper, args);
        return items.isEmpty() ? null : items.get(0);
    }

    private static class WorkingDatabaseRowMapper implements RowMapper<WorkingDatabase> {
        @Override
        public WorkingDatabase mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkingDatabase database = new WorkingDatabase();
            database.setDbId(rs.getLong("DBID"));
            database.setName(rs.getString("DBName"));
            database.setYear(rs.getString("DBYear"));
            database.setSysName(rs.getString("DBSysName"));
            database.setActive(rs.getBoolean("IsActive"));
            database.setCode(rs.getString("DBNo"));
            database.setEncryptedKey(rs.getBytes("pwd1"));
            database.setEncryptedKeyIndex(rs.getBytes("pwd2"));
            database.setInitializationVector(rs.getBytes("pwd3"));
            database.setInitializationVectorIndex(rs.getBytes("pwd4"));
            database.setEncryptedPassword(rs.getString("pwd5"));
            database.setUserName(rs.getString("UserName"));
            database.setCompanyName(rs.getString("CompanyName"));
            database.setServerIp(rs.getString("ServerIp"));
            database.setLocal(rs.getBoolean("IsLocal"));
            if (database.getEncryptedPassword() != null) {
                database.setPassword(LegacyPasswordCipher.decrypt(database.toEncryptedPassword()));
            }
            return database;
        }
    }
}
