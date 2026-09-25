package zw.ac.uz.dpdms.report.model;

import zw.ac.uz.dpdms.common.Hazard;

import java.util.List;
import java.util.Map;

/**
 * The part of a report for one hazard.
 *
 * fullColumns    - every shared metadata field + the hazard's indicators
 *                  (used by CSV and Excel, where width doesn't matter)
 * compactColumns - fits a page: GPS combined, province/reporter left out
 *                  (used by PDF and Word)
 * rows           - incidents exactly as the hazard service returned them
 * unavailable    - set when the hazard service could not be reached; the
 *                  rest of the report is still produced
 */
public record HazardSection(
        Hazard hazard,
        String serviceName,
        List<Column> fullColumns,
        List<Column> compactColumns,
        List<Map<String, Object>> rows,
        String unavailable
) {
    public String title() {
        return displayName(hazard);
    }

    public boolean isUnavailable() {
        return unavailable != null;
    }

    public static String displayName(Hazard hazard) {
        String s = hazard.name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
