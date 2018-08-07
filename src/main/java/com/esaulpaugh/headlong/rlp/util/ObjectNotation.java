package com.esaulpaugh.headlong.rlp.util;

import com.esaulpaugh.headlong.rlp.DataType;
import com.esaulpaugh.headlong.rlp.DecodeException;

import java.util.Arrays;
import java.util.List;

import static com.esaulpaugh.headlong.rlp.DataType.MIN_LONG_DATA_LEN;
import static com.esaulpaugh.headlong.rlp.util.Strings.HEX;

public class ObjectNotation {

    static final String OBJECT_ARRAY_PREFIX = "{";
    static final String OBJECT_ARRAY_SUFFIX = "}";
    static final String STRING_PREFIX = "\"";
    static final String STRING_SUFFIX = "\"";

    private static final String COMMA_SPACE = ", ";
    private static final String END_ARRAY = OBJECT_ARRAY_SUFFIX + COMMA_SPACE;
    private static final String BEGIN_ARRAY_SHORT = OBJECT_ARRAY_PREFIX + " ";
    private static final String END_ARRAY_SHORT = " " + END_ARRAY;

    private final String value;

    ObjectNotation(String value) {
        if(value == null)
            throw new IllegalArgumentException("value cannot be null");
        this.value = value;
    }

    public List<Object> parse() {
        return Parser.parse(value);
    }

    private static final String[] INDENTATIONS;

    private static final int INDENTATION_CACHE_SIZE = 16;

    private static final String ELEMENT_INDENTATION = newIndentation(1);

    static {
        INDENTATIONS = new String[INDENTATION_CACHE_SIZE];
        for (int i = 0; i < INDENTATIONS.length; i++) {
            INDENTATIONS[i] = newIndentation(i);
        }
    }

    private static int getShortElementEnd(int elementDataIndex, final int elementDataLen, final int containerEnd) throws DecodeException {
        final int end = elementDataIndex + elementDataLen;
        if (end > containerEnd) {
            throw new DecodeException("element @ index " + (elementDataIndex - 1) + " exceeds its container: " + end + " > " + containerEnd);
        }

        return end;
    }

    private static int getLongElementEnd(byte[] data, final int leadByteIndex, final int lengthLen, final int containerEnd) throws DecodeException {
        int lengthIndex = leadByteIndex + 1;
        if (lengthIndex + lengthLen > containerEnd) {
            throw new DecodeException("end of input reached; element @ " + leadByteIndex + " cannot be decoded: " + (lengthIndex + lengthLen) + " > " + containerEnd);
        }
        final long dataLenLong = Integers.getLong(data, leadByteIndex + 1, lengthLen);
        if (dataLenLong > Integer.MAX_VALUE) {
            throw new DecodeException("too much data: " + dataLenLong + " > " + Integer.MAX_VALUE);
        }
        final int dataLen = (int) dataLenLong;
        if (dataLen < MIN_LONG_DATA_LEN) {
            throw new DecodeException("long element data length must be " + MIN_LONG_DATA_LEN + " or greater; found: " + dataLen + " for element @ " + leadByteIndex);
        }
        final int end = lengthIndex + lengthLen + dataLen;
        if (end < 0) {
            throw new DecodeException("overflow");
        }
        if (end > containerEnd) {
            throw new DecodeException("element @ index " + leadByteIndex + " exceeds its container; indices: " + end + " > " + containerEnd);
        }

        return end;
    }

    public static ObjectNotation fromEncoding(byte[] encoding) throws DecodeException {
        return fromEncoding(encoding, 0, encoding.length);
    }

    public static ObjectNotation fromEncoding(byte[] encoding, int index, int end) throws DecodeException {
        StringBuilder sb = new StringBuilder("(");
        buildLongList(
                sb,
                encoding,
                index,
                end,
                0
        );
        return new ObjectNotation(sb.append("\n)").toString());
    }

    private static int buildLongList(final StringBuilder sb, final byte[] data, final int dataIndex, int end, final int depth) throws DecodeException {

        String baseIndentation = getIndentation(depth);

        if(depth != 0) {
            sb.append(OBJECT_ARRAY_PREFIX);
        }

        final int nextDepth = depth + 1;

        int elementDataIndex = -1;
        int lengthLen;
        int elementDataLen;
        int elementEnd = -1;
        boolean hasELement = false;
        int i = dataIndex;
        while (i < end) {
            sb.append("\n").append(baseIndentation);
            byte current = data[i];
            final DataType type = DataType.type(current);
            switch (type) {
            case SINGLE_BYTE:
                break;
            case STRING_SHORT:
            case LIST_SHORT:
                elementDataIndex = i + 1;
                elementDataLen = current - type.offset;
                elementEnd = getShortElementEnd(elementDataIndex, elementDataLen, end);
                break;
            case STRING_LONG:
            case LIST_LONG:
                lengthLen = current - type.offset;
                elementDataIndex = i + 1 + lengthLen;
                elementEnd = getLongElementEnd(data, i, lengthLen, end);
                break;
            default:
                throw new RuntimeException();
            }
            hasELement = true;
            switch (type) {
            case SINGLE_BYTE:
                sb.append(ELEMENT_INDENTATION);
                i = buildByte(sb, data, i);
                break;
            case STRING_SHORT:
                sb.append(ELEMENT_INDENTATION);
                i = buildString(sb, data, elementDataIndex, elementEnd);
                break;
            case LIST_SHORT:
                sb.append(ELEMENT_INDENTATION);
                i = buildShortList(sb, data, elementDataIndex, elementEnd);
                break;
            case STRING_LONG:
                sb.append(ELEMENT_INDENTATION);
                i = buildString(sb, data, elementDataIndex, elementEnd);
                break;
            case LIST_LONG:
                sb.append(ELEMENT_INDENTATION);
                i = buildLongList(sb, data, elementDataIndex, elementEnd, nextDepth);
//            default:
            }
        }

        if (hasELement)
            stripFinalCommaAndSpace(sb);

        if(depth != 0) {
            sb.append('\n')
                    .append(baseIndentation).append(END_ARRAY);
        }

        return end;
    }

    private static int buildShortList(final StringBuilder sb, final byte[] data, final int dataIndex, final int end) throws DecodeException {
        sb.append(BEGIN_ARRAY_SHORT);

        int elementDataIndex = -1;
        int elementDataLen;
        int elementEnd = -1;
        boolean hasElement = false;
        int i = dataIndex;
        while (i < end) {
            byte current = data[i];
            final DataType type = DataType.type(current);
            switch (type) {
            case SINGLE_BYTE:
                break;
            case STRING_SHORT:
            case LIST_SHORT:
                elementDataIndex = i + 1;
                elementDataLen = current - type.offset;
                elementEnd = getShortElementEnd(elementDataIndex, elementDataLen, end);
                break;
            case STRING_LONG:
            case LIST_LONG:
                throw new IllegalStateException("surely, it cannot possibly fit. index: " + i);
            default:
                throw new RuntimeException();
            }
            hasElement = true;
            switch (type) {
            case SINGLE_BYTE:
                i = buildByte(sb, data, i);
                break;
            case STRING_SHORT:
                i = buildString(sb, data, elementDataIndex, elementEnd);
                break;
            case LIST_SHORT:
                i = buildShortList(sb, data, elementDataIndex, elementEnd);
                break;
            case STRING_LONG:
                break;
            case LIST_LONG:
                break;
            default:
            }
        }

        if (hasElement)
            stripFinalCommaAndSpace(sb);
        sb.append(END_ARRAY_SHORT);

        return end;
    }

    private static String newIndentation(int n) {
        char[] spaces = new char[n << 1]; // 2 spaces per
        Arrays.fill(spaces, ' ');
        return String.valueOf(spaces);
    }

    private static String getIndentation(int n) {
        return n >= INDENTATIONS.length ? newIndentation(n) : INDENTATIONS[n];
    }

    private static void stripFinalCommaAndSpace(StringBuilder sb) {
        final int n = sb.length();
        sb.replace(n - 2, n, "");
    }

    private static int buildByte(StringBuilder sb, byte[] data, int i) {
        String string = Strings.encode(data, i, 1, HEX);

        sb.append('\"').append(string).append("\", ");

        return i + 1;
    }

    private static int buildString(StringBuilder sb, byte[] data, int from, int to) {

        final int len = to - from;
        if(len == 1 && data[from] >= 0x00) { // same as (data[from] & 0xFF) < 0x80
            throw new IllegalStateException("invalid rlp for single byte @ " + (from - 1));
        }

        String string = Strings.encode(data, from, len, HEX);

        sb.append('\"').append(string).append("\", ");

        return to;
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof ObjectNotation)) {
            return false;
        }

        return value.equals(((ObjectNotation) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}