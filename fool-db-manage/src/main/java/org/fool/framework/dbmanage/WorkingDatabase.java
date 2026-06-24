package org.fool.framework.dbmanage;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@Table("WorkDataBase")
@Data
public class WorkingDatabase {
    @Column("DBID")
    private Long dbId;
    @Column("DBName")
    private String name;
    @Column("DBYear")
    private String year;
    @Column("DBSysName")
    private String sysName;
    @Column("IsActive")
    private boolean active;
    @Id
    @Column("DBNo")
    private String code;
    @Column("pwd1")
    private byte[] encryptedKey;
    @Column("pwd2")
    private byte[] encryptedKeyIndex;
    @Column("pwd3")
    private byte[] initializationVector;
    @Column("pwd4")
    private byte[] initializationVectorIndex;
    @Column("pwd5")
    private String encryptedPassword;
    @Column("UserName")
    private String userName;
    @Column("CompanyName")
    private String companyName;
    @Column("ServerIp")
    private String serverIp;
    @Column("IsLocal")
    private boolean local;

    private String password;

    public LegacyPasswordCipher.EncryptedPassword toEncryptedPassword() {
        return new LegacyPasswordCipher.EncryptedPassword(
                encryptedKey,
                encryptedKeyIndex,
                initializationVector,
                initializationVectorIndex,
                encryptedPassword);
    }

    public void applyEncryptedPassword(LegacyPasswordCipher.EncryptedPassword encryptedPassword) {
        this.encryptedKey = encryptedPassword.getEncryptedKey();
        this.encryptedKeyIndex = encryptedPassword.getEncryptedKeyIndex();
        this.initializationVector = encryptedPassword.getInitializationVector();
        this.initializationVectorIndex = encryptedPassword.getInitializationVectorIndex();
        this.encryptedPassword = encryptedPassword.getEncryptedPassword();
    }

    public String buildConnectionString(String masterConnectionString, String applicationName) {
        Map<String, String> base = parseConnectionString(masterConnectionString);
        String dataSource = local ? base.get("data source") : serverIp;

        Map<String, String> result = new LinkedHashMap<>();
        putIfNotBlank(result, "Data Source", dataSource);
        putIfNotBlank(result, "Initial Catalog", sysName);
        putIfNotBlank(result, "User ID", userName);
        putIfNotBlank(result, "Password", password);
        putIfNotBlank(result, "Application Name", applicationName);
        return renderConnectionString(result);
    }

    public void update() {
        throw new UnsupportedOperationException(
                "Legacy WorkingDataBase.Update maps to FoolFrame NotImplementedException");
    }

    private static Map<String, String> parseConnectionString(String connectionString) {
        Map<String, String> result = new LinkedHashMap<>();
        if (connectionString == null || connectionString.isBlank()) {
            return result;
        }
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            int equalsIndex = part.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = part.substring(0, equalsIndex).trim().toLowerCase(Locale.ROOT);
            String value = part.substring(equalsIndex + 1).trim();
            result.put(key, value);
        }
        return result;
    }

    private static String renderConnectionString(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner(";");
        values.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    private static void putIfNotBlank(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) {
            values.put(key, value);
        }
    }
}
