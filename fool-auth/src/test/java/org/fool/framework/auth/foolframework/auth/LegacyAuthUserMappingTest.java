package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.EncryptType;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LegacyAuthUserMappingTest {
    @Test
    public void mapsUserToLegacyAuthTable() throws Exception {
        Table table = User.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_AUTH_USER", table.value());
        assertEquals("USER_", table.columnPrefix());

        assertColumn("userId", "USER_UID", true);
        assertColumn("userGuid", "USER_UUID", false);
        assertColumn("loginName", "USER_LOGINNAME", false);
        assertColumn("phone", "USER_PHONE", false);
        assertColumn("email", "USER_MAIL", false);
        assertColumn("firstName", "USER_FIRSTNAME", false);
        assertColumn("lastName", "USER_LASTNAME", false);
        assertColumn("showName", "USER_SHOWNAME", false);
        assertColumn("title", "USER_TITLE", false);
        assertColumn("avtar", "USER_AVTAR", false);
        assertColumn("password", "USER_PWD", false);
        assertColumn("createTime", "USER_REGTIME", false);
        assertColumn("lastLoginTime", "USER_LASTLOGINTIME", false);
        assertColumn("lastModifyTime", "USER_LASTMODIFYTIME", false);
        assertColumn("sex", "USER_SEX", false);
        assertColumn("defaultViewId", "USER_DEFAULTVIEW", false);

        assertEquals(String.class, field("userGuid").getType());
        assertEquals(LocalDateTime.class, field("createTime").getType());
        assertEquals(Sex.class, field("sex").getType());
    }

    @Test
    public void carriesLegacyKeyGenerationAndEncryptionMetadata() throws Exception {
        assertColumnMetadata("userId", true, "", GenerationType.ON_INSERT, EncryptType.NONE);
        assertEquals(
                SqlGenerateConfig.AUTO_INCREMENT,
                field("userId").getDeclaredAnnotation(SqlGenerate.class).value());
        assertColumnMetadata("userGuid", true, "UUID", GenerationType.NEVER, EncryptType.NONE);
        assertColumnMetadata("loginName", true, "LOGINNAME", GenerationType.NEVER, EncryptType.NONE);
        assertColumnMetadata("phone", true, "PHONE", GenerationType.NEVER, EncryptType.NONE);
        assertColumnMetadata("email", true, "MAIL", GenerationType.NEVER, EncryptType.NONE);
        assertColumnMetadata("password", false, "", GenerationType.NEVER, EncryptType.MD5);
        assertColumnMetadata("createTime", false, "", GenerationType.ON_INSERT_AND_UPDATE, EncryptType.NONE);

        assertEquals(0, Sex.Male.ordinal());
        assertEquals(1, Sex.Female.ordinal());
        assertEquals(0, Sex.Male.code());
        assertEquals(1, Sex.Female.code());
    }

    private static void assertColumn(String fieldName, String columnName, boolean id) throws Exception {
        Field field = field(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull("missing @Column on " + fieldName, column);
        assertEquals(columnName, column.value());
        assertEquals(id, field.getDeclaredAnnotation(Id.class) != null);
        assertTrue(field.getType() != Void.class);
    }

    private static void assertColumnMetadata(
            String fieldName,
            boolean key,
            String keyGroupName,
            GenerationType generationType,
            EncryptType encryptType) throws Exception {
        Column column = field(fieldName).getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(key, column.key());
        assertEquals(keyGroupName, column.keyGroupName());
        assertEquals(generationType, column.generationType());
        assertEquals(encryptType, column.encryptType());
    }

    private static Field field(String fieldName) throws Exception {
        return User.class.getDeclaredField(fieldName);
    }
}
