package zw.ac.uz.dpdms.report.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Everything a ReportWriter needs, independent of the output format, so
 * the four writers (PDF, DOCX, XLSX, CSV) never fetch or filter data
 * themselves.
 */
public record ReportData(
        String title,
        LocalDateTime generatedAt,
        String generatedBy,
        List<String> filterLines,
        List<HazardSection> sections
) {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public int totalRows() {
        return sections.stream().mapToInt(s -> s.rows().size()).sum();
    }

    public List<String> warnings() {
        return sections.stream()
                .filter(HazardSection::isUnavailable)
                .map(s -> s.title() + " data unavailable: " + s.unavailable())
                .toList();
    }

    public String generatedAtText() {
        return generatedAt.format(DATE_TIME);
    }

    /** The raw value of a column (Number, Boolean, String or null). */
    public static Object raw(Map<String, Object> row, Column column) {
        if (Column.GPS.equals(column.key())) {
            Object lat = row.get("latitude");
            Object lon = row.get("longitude");
            return (lat == null || lon == null) ? null : lat + ", " + lon;
        }
        return row.get(column.key());
    }

    /** A column's value as display text, identical in every format. */
    public static String text(Map<String, Object> row, Column column) {
        Object v = raw(row, column);
        if (v == null) {
            return "";
        }
        if (v instanceof Boolean b) {
            return b ? "Yes" : "No";
        }
        String s = String.valueOf(v);
        // ISO date-times from the hazard services -> "2026-09-23 04:33"
        if (s.length() >= 16 && s.charAt(4) == '-' && s.charAt(10) == 'T') {
            try {
                return LocalDateTime.parse(s).format(DATE_TIME);
            } catch (DateTimeParseException ignored) {
                // not a date after all - fall through
            }
        }
        // Enum values like CORRECTIONS_REQUESTED -> "CORRECTIONS REQUESTED"
        if (s.equals(s.toUpperCase()) && s.indexOf('_') >= 0) {
            return s.replace('_', ' ');
        }
        return s;
    }
}
