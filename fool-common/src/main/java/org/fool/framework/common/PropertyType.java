package org.fool.framework.common;

public enum PropertyType {
    IdentifyId(0),
    Int(1),
    UInt(2),
    Long(3),
    ULong(4),
    Float(5),
    Double(6),
    Decimal(7),
    Boolean(8),
    Char(9),
    Byte(10),
    String(11),
    Date(12),
    Time(13),
    DateTime(14),
    Enum(15),
    BusinessObject(16),
    SerialNo(17),
    MD5(18),
    Radom(19),
    RadomDECS(20),
    Guid(21);

    private final int code;

    PropertyType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
