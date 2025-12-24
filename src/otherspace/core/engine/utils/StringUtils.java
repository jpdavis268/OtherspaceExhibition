package otherspace.core.engine.utils;

/**
 * Utility class containing methods useful for string related activities.
 */
public final class StringUtils {
    private StringUtils() {}

    /**
     * Insert a character at a given point in a string.
     *
     * @param base Base string.
     * @param toInsert Character to insert.
     * @param index Index to insert at.
     * @return String with inserted character.
     */
    public static String stringInsert(String base, char toInsert, int index) {
        String start = base.substring(0, index);
        String end = base.substring(index);
        return start + toInsert + end;
    }

    /**
     * Insert a string at a given point in a string.
     *
     * @param base Base string.
     * @param toInsert Character to insert.
     * @param index Index to insert at.
     * @return String with inserted character.
     */
    public static String stringInsert(String base, String toInsert, int index) {
        String start = base.substring(0, index);
        String end = base.substring(index);
        return start + toInsert + end;
    }

    /**
     * Remove a section of a string.
     *
     * @param base String to remove section from.
     * @param index Index to start removing at.
     * @param length Length of section to remove.
     * @return Modified string.
     */
    public static String stringRemove(String base, int index, int length) {
        String start = base.substring(0, index);
        String end = base.substring(index + Math.min(length, base.length() - index));
        return start + end;
    }

    /**
     * Get a calculated carat for use in text fields.
     *
     * @return Carat, either "|" or "".
     */
    public static String getFieldCarat() {
        return System.currentTimeMillis() % 500 < 250 ? "|" : "";
    }
}
