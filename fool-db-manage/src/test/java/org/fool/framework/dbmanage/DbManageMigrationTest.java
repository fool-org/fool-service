package org.fool.framework.dbmanage;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;
import org.junit.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DbManageMigrationTest {
    @Test
    public void mapsDbManageEntitiesToLegacyTables() throws Exception {
        assertEquals("DB_App", tableName(DbApplication.class));
        assertColumn(DbApplication.class, "id", "BO_Id", true);
        assertColumn(DbApplication.class, "appName", "BO_AppName", false);

        assertEquals("DS_DataSourceSet", tableName(DataBaseSource.class));
        assertColumn(DataBaseSource.class, "key", "DS_Key", true);
        assertColumn(DataBaseSource.class, "dbNo", "DS_DBNo", false);

        assertEquals("DB_AppDB", tableName(ApplicationDatabase.class));
        assertColumn(ApplicationDatabase.class, "appId", "App_Id", true);
        assertColumn(ApplicationDatabase.class, "dbNo", "DBNo", true);

        assertEquals("WorkDataBase", tableName(WorkingDatabase.class));
        assertColumn(WorkingDatabase.class, "dbId", "DBID", false);
        assertColumn(WorkingDatabase.class, "name", "DBName", false);
        assertColumn(WorkingDatabase.class, "year", "DBYear", false);
        assertColumn(WorkingDatabase.class, "sysName", "DBSysName", false);
        assertColumn(WorkingDatabase.class, "active", "IsActive", false);
        assertColumn(WorkingDatabase.class, "code", "DBNo", true);
        assertColumn(WorkingDatabase.class, "encryptedKey", "pwd1", false);
        assertColumn(WorkingDatabase.class, "encryptedKeyIndex", "pwd2", false);
        assertColumn(WorkingDatabase.class, "initializationVector", "pwd3", false);
        assertColumn(WorkingDatabase.class, "initializationVectorIndex", "pwd4", false);
        assertColumn(WorkingDatabase.class, "encryptedPassword", "pwd5", false);
        assertColumn(WorkingDatabase.class, "userName", "UserName", false);
        assertColumn(WorkingDatabase.class, "companyName", "CompanyName", false);
        assertColumn(WorkingDatabase.class, "serverIp", "ServerIp", false);
        assertColumn(WorkingDatabase.class, "local", "IsLocal", false);
    }

    @Test
    public void exposesFactoryAndJdbcRepositoryAsSpringBeans() {
        assertNotNull(WorkDataBaseFactory.class.getDeclaredAnnotation(Service.class));
        assertNotNull(JdbcWorkingDatabaseRepository.class.getDeclaredAnnotation(Repository.class));
    }

    @Test
    public void buildsLegacyWorkingDatabaseConnectionString() {
        WorkingDatabase db = new WorkingDatabase();
        db.setSysName("tenant_2026");
        db.setUserName("tenant_user");
        db.setPassword("tenant_secret");
        db.setServerIp("10.2.3.4");

        String master = "Data Source=master-db;Initial Catalog=master;User ID=root;Password=rootpw";

        db.setLocal(true);
        String local = db.buildConnectionString(master, "FoolService");
        assertTrue(local.contains("Data Source=master-db"));
        assertTrue(local.contains("Initial Catalog=tenant_2026"));
        assertTrue(local.contains("User ID=tenant_user"));
        assertTrue(local.contains("Password=tenant_secret"));
        assertTrue(local.contains("Application Name=FoolService"));

        db.setLocal(false);
        String remote = db.buildConnectionString(master, "FoolService");
        assertTrue(remote.contains("Data Source=10.2.3.4"));
    }

    @Test
    public void resolvesDataSourceKeyToWorkingDatabaseConnectionString() {
        DataBaseSource source = new DataBaseSource();
        source.setKey("reporting");
        source.setDbNo("02");

        WorkingDatabase db = new WorkingDatabase();
        db.setCode("02");
        db.setSysName("tenant_reporting");
        db.setUserName("report_user");
        db.setPassword("report_pw");
        db.setLocal(true);

        DataBaseSourceResolver resolver = new DataBaseSourceResolver(
                key -> "reporting".equals(key) ? source : null,
                () -> List.of(db),
                "Data Source=master-db;Initial Catalog=master;User ID=root;Password=rootpw",
                "FoolService");

        assertTrue(resolver.getConnectionString("reporting").contains("Initial Catalog=tenant_reporting"));
        assertNull(resolver.getConnectionString("missing"));
    }

    @Test
    public void legacyPasswordCipherRoundTripsPayload() {
        LegacyPasswordCipher.EncryptedPassword encrypted = LegacyPasswordCipher.encrypt("tenant_secret");

        assertEquals(8, encrypted.getEncryptedKey().length);
        assertEquals(8, encrypted.getEncryptedKeyIndex().length);
        assertEquals(8, encrypted.getInitializationVector().length);
        assertEquals(8, encrypted.getInitializationVectorIndex().length);
        assertNotNull(encrypted.getEncryptedPassword());
        assertEquals("tenant_secret", LegacyPasswordCipher.decrypt(encrypted));
    }

    @Test
    public void byteArrayRenderingMatchesLegacyHexSqlLiteral() {
        assertEquals("0x000A7F80", LegacySqlLiterals.bytesToHexLiteral(new byte[]{0x00, 0x0A, 0x7F, (byte) 0x80}));
        assertArrayEquals(new byte[]{0x00, 0x0A, 0x7F, (byte) 0x80},
                LegacySqlLiterals.hexLiteralToBytes("0x000A7F80"));
    }

    @Test
    public void workDatabaseFactoryListsAllAndAppDatabasesInLegacyOrder() {
        FakeWorkingDatabaseRepository repository = new FakeWorkingDatabaseRepository();
        WorkingDatabase db01 = workingDatabase("01", "Primary", "2025", "primary_db", "u1", "p1");
        WorkingDatabase db02 = workingDatabase("02", "Archive", "2026", "archive_db", "u2", "p2");
        repository.items.add(db02);
        repository.items.add(db01);
        repository.link("ERP", "02");

        WorkDataBaseFactory factory = new WorkDataBaseFactory(repository);

        assertEquals(List.of(db01, db02), factory.allList());
        assertEquals(List.of(db02), factory.all("ERP"));
    }

    @Test
    public void workDatabaseFactoryCreateAssignsNextCodeAndEncryptsPassword() {
        FakeWorkingDatabaseRepository repository = new FakeWorkingDatabaseRepository();
        repository.items.add(workingDatabase("01", "Primary", "2025", "primary_db", "u1", "p1"));

        WorkingDatabase created = workingDatabase(null, "Archive", "2026", "archive_db", "archive_user", "archive_pw");
        created.setCompanyName("ACME");
        created.setServerIp("10.10.0.8");

        new WorkDataBaseFactory(repository).create(created);

        assertEquals("02", created.getCode());
        assertEquals(List.of(created), repository.inserted);
        assertNotNull(created.getEncryptedPassword());
        assertEquals("archive_pw", LegacyPasswordCipher.decrypt(created.toEncryptedPassword()));
    }

    @Test
    public void workDatabaseFactoryCreateRejectsDuplicateNameAndYear() {
        FakeWorkingDatabaseRepository repository = new FakeWorkingDatabaseRepository();
        repository.items.add(workingDatabase("01", "Primary", "2025", "primary_db", "u1", "p1"));

        try {
            new WorkDataBaseFactory(repository).create(workingDatabase(null, "Primary", "2025", "other_db", "u2", "p2"));
        } catch (IllegalStateException e) {
            assertEquals("不能创建名称，年度相同的帐套", e.getMessage());
            assertTrue(repository.inserted.isEmpty());
            return;
        }
        throw new AssertionError("expected duplicate create to fail");
    }

    @Test
    public void workDatabaseFactorySaveRejectsDuplicateNameYearOutsideCurrentCodeAndUpdatesPassword() {
        FakeWorkingDatabaseRepository repository = new FakeWorkingDatabaseRepository();
        repository.items.add(workingDatabase("01", "Primary", "2025", "primary_db", "u1", "p1"));
        repository.items.add(workingDatabase("02", "Archive", "2026", "archive_db", "u2", "p2"));

        try {
            new WorkDataBaseFactory(repository).save(workingDatabase("02", "Primary", "2025", "archive_db", "u2", "new_pw"));
        } catch (IllegalStateException e) {
            assertEquals("无法保存登录设置，保存会与现有登录冲突", e.getMessage());
            assertTrue(repository.updated.isEmpty());
        }

        WorkingDatabase updated = workingDatabase("02", "Archive", "2026", "archive_db", "u2", "new_pw");
        new WorkDataBaseFactory(repository).save(updated);

        assertEquals(List.of(updated), repository.updated);
        assertEquals("new_pw", LegacyPasswordCipher.decrypt(updated.toEncryptedPassword()));
    }

    @Test
    public void workDatabaseFactoryDeleteRemovesDatabaseByCode() {
        FakeWorkingDatabaseRepository repository = new FakeWorkingDatabaseRepository();
        WorkingDatabase db = workingDatabase("02", "Archive", "2026", "archive_db", "u2", "p2");

        new WorkDataBaseFactory(repository).delete(db);

        assertEquals(List.of("02"), repository.deletedCodes);
    }

    @Test
    public void sqlConQueriesTablesAndExecutesLegacySqlCommands() {
        RecordingSqlExecutionGateway gateway = new RecordingSqlExecutionGateway();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("DBNo", "02");
        row.put("DBName", "Archive");
        gateway.nextRows = List.of(row);
        gateway.nextAffectedRows = 3;

        SqlCon sqlCon = new SqlCon(gateway);

        SqlResultTable table = sqlCon.getTable("SELECT * FROM WorkDataBase");
        int affectedRows = sqlCon.excuteSql("UPDATE WorkDataBase SET IsActive = 1");
        boolean transactionResult = sqlCon.exuteSqls(new String[]{
                "INSERT INTO WorkDataBase(DBNo) VALUES ('02')",
                "UPDATE WorkDataBase SET IsActive = 1 WHERE DBNo = '02'"
        });

        assertEquals(List.of("SELECT * FROM WorkDataBase"), gateway.queries);
        assertEquals(1, table.getRows().size());
        assertEquals("02", table.getRows().get(0).get("DBNo"));
        assertEquals("Archive", table.getRows().get(0).get("DBName"));
        assertEquals(3, affectedRows);
        assertEquals(List.of("UPDATE WorkDataBase SET IsActive = 1"), gateway.executedSql);
        assertEquals(List.of(List.of(
                "INSERT INTO WorkDataBase(DBNo) VALUES ('02')",
                "UPDATE WorkDataBase SET IsActive = 1 WHERE DBNo = '02'")),
                gateway.transactionBatches);
        assertEquals(true, transactionResult);
    }

    @Test
    public void sqlConReturnsFalseWhenLegacyBatchExecutionRollsBack() {
        RecordingSqlExecutionGateway gateway = new RecordingSqlExecutionGateway();
        gateway.throwOnTransaction = true;

        boolean result = new SqlCon(gateway).exuteSqls(new String[]{"bad sql"});

        assertEquals(false, result);
        assertEquals(List.of(List.of("bad sql")), gateway.transactionBatches);
    }

    @Test
    public void jdbcSqlExecutionGatewayIsASpringRepository() {
        assertNotNull(JdbcSqlExecutionGateway.class.getDeclaredAnnotation(Repository.class));
    }

    @Test
    public void legacyNotImplementedDatabaseOperationsAreExplicitlyExposed() {
        WorkDataBaseFactory factory = new WorkDataBaseFactory(new FakeWorkingDatabaseRepository());
        WorkingDatabase source = workingDatabase("02", "Archive", "2026", "archive_db", "u2", "p2");
        WorkingDatabase destination = workingDatabase("03", "Next", "2027", "next_db", "u3", "p3");

        assertUnsupported("CreateDataBase", () -> factory.createDatabase(source));
        assertUnsupported("ConvertToAutoDataBase", () -> factory.convertToAutoDatabase(source));
        assertUnsupported("CarryForward", () -> factory.carryForward(source, destination, true));
        assertUnsupported("WorkingDataBase.Update", source::update);
    }

    private static String tableName(Class<?> type) {
        Table table = type.getDeclaredAnnotation(Table.class);
        assertNotNull("missing @Table on " + type.getName(), table);
        return table.value();
    }

    private static void assertColumn(Class<?> type, String fieldName, String columnName, boolean key) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull("missing @Column on " + fieldName, column);
        assertEquals(columnName, column.value());
        assertEquals(key, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static void assertUnsupported(String legacyOperation, Runnable action) {
        try {
            action.run();
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains(legacyOperation));
            assertTrue(e.getMessage().contains("NotImplementedException"));
            return;
        }
        throw new AssertionError("expected unsupported legacy operation: " + legacyOperation);
    }

    private static WorkingDatabase workingDatabase(
            String code,
            String name,
            String year,
            String sysName,
            String userName,
            String password) {
        WorkingDatabase database = new WorkingDatabase();
        database.setCode(code);
        database.setName(name);
        database.setYear(year);
        database.setSysName(sysName);
        database.setUserName(userName);
        database.setPassword(password);
        database.setActive(true);
        return database;
    }

    private static final class FakeWorkingDatabaseRepository implements WorkingDatabaseRepository {
        private final List<WorkingDatabase> items = new ArrayList<>();
        private final List<AppDatabaseLink> links = new ArrayList<>();
        private final List<WorkingDatabase> inserted = new ArrayList<>();
        private final List<WorkingDatabase> updated = new ArrayList<>();
        private final List<String> deletedCodes = new ArrayList<>();

        @Override
        public List<WorkingDatabase> findAll() {
            return items.stream()
                    .sorted((left, right) -> left.getCode().compareTo(right.getCode()))
                    .toList();
        }

        @Override
        public List<WorkingDatabase> findByAppName(String appName) {
            List<String> dbNos = links.stream()
                    .filter(link -> appName.equals(link.getAppName()))
                    .map(AppDatabaseLink::getDbNo)
                    .toList();
            return findAll().stream()
                    .filter(db -> dbNos.contains(db.getCode()))
                    .toList();
        }

        @Override
        public WorkingDatabase findByNameAndYear(String name, String year) {
            return items.stream()
                    .filter(db -> Objects.equals(name, db.getName()))
                    .filter(db -> Objects.equals(year, db.getYear()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public WorkingDatabase findByNameAndYearExceptCode(String name, String year, String code) {
            return items.stream()
                    .filter(db -> Objects.equals(name, db.getName()))
                    .filter(db -> Objects.equals(year, db.getYear()))
                    .filter(db -> !Objects.equals(code, db.getCode()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String findMaxCode() {
            return items.stream()
                    .map(WorkingDatabase::getCode)
                    .max(String::compareTo)
                    .orElse(null);
        }

        @Override
        public void insert(WorkingDatabase database) {
            inserted.add(database);
        }

        @Override
        public void update(WorkingDatabase database) {
            updated.add(database);
        }

        @Override
        public void deleteByCode(String code) {
            deletedCodes.add(code);
        }

        private void link(String appName, String dbNo) {
            links.add(new AppDatabaseLink(appName, dbNo));
        }
    }

    private static final class RecordingSqlExecutionGateway implements SqlExecutionGateway {
        private final List<String> queries = new ArrayList<>();
        private final List<String> executedSql = new ArrayList<>();
        private final List<List<String>> transactionBatches = new ArrayList<>();
        private List<Map<String, Object>> nextRows = List.of();
        private int nextAffectedRows;
        private boolean throwOnTransaction;

        @Override
        public List<Map<String, Object>> queryForTable(String sql) {
            queries.add(sql);
            return nextRows;
        }

        @Override
        public int execute(String sql) {
            executedSql.add(sql);
            return nextAffectedRows;
        }

        @Override
        public void executeInTransaction(List<String> sqls) {
            transactionBatches.add(sqls);
            if (throwOnTransaction) {
                throw new IllegalStateException("rollback");
            }
        }
    }

    private static final class AppDatabaseLink {
        private final String appName;
        private final String dbNo;

        private AppDatabaseLink(String appName, String dbNo) {
            this.appName = appName;
            this.dbNo = dbNo;
        }

        private String getAppName() {
            return appName;
        }

        private String getDbNo() {
            return dbNo;
        }
    }
}
