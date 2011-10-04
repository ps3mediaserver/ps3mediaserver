package net.pms.util;

public class StringUtil {
    /**Appends "&lt;<u>tag</u> " to the StringBuilder. This is a typical HTML/DIDL/XML tag opening.
     * @param sb String to append the tag beginning to.
     * @param tag String that represents the tag
     */
    public static void openTag(StringBuilder sb, String tag) {
            sb.append("&lt;");
            sb.append(tag);
    }

    /**Appends the closing symbol &gt; to the StringBuilder. This is a typical HTML/DIDL/XML tag closing.
     * @param sb String to append the ending character of a tag.
     */
    public static void endTag(StringBuilder sb) {
            sb.append("&gt;");
    }

    /**Appends "&lt;/<u>tag</u>&gt;" to the StringBuilder. This is a typical closing HTML/DIDL/XML tag.
     * @param sb
     * @param tag
     */
    public static void closeTag(StringBuilder sb, String tag) {
            sb.append("&lt;/");
            sb.append(tag);
            sb.append("&gt;");
    }

    public static void addAttribute(StringBuilder sb, String attribute, Object value) {
            sb.append(" ");
            sb.append(attribute);
            sb.append("=\"");
            sb.append(value);
            sb.append("\"");
    }

    public static void addXMLTagAndAttribute(StringBuilder sb, String tag, Object value) {
            sb.append("&lt;");
            sb.append(tag);
            sb.append("&gt;");
            sb.append(value);
            sb.append("&lt;/");
            sb.append(tag);
            sb.append("&gt;");
    }


}
