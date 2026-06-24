package org.fool.framework.dbmanage;

public final class LegacySqlLiterals {
    private LegacySqlLiterals() {
    }

    public static String bytesToHexLiteral(byte[] data) {
        if (data == null) {
            return "NULL";
        }
        StringBuilder builder = new StringBuilder("0x");
        for (byte item : data) {
            builder.append(String.format("%02X", item & 0xff));
        }
        return builder.toString();
    }

    public static byte[] hexLiteralToBytes(String literal) {
        if (literal == null) {
            return null;
        }
        String hex = literal.startsWith("0x") || literal.startsWith("0X") ? literal.substring(2) : literal;
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex literal must have an even number of characters.");
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int from = i * 2;
            result[i] = (byte) Integer.parseInt(hex.substring(from, from + 2), 16);
        }
        return result;
    }
}
