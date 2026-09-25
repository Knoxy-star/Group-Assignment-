package zw.ac.uz.dpdms.report.model;

import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDate;

/**
 * The filters the brief requires: hazard type, ward, district, date range,
 * severity and approval status. Any of them may be null (= no filter).
 * A null hazard means "every hazard the caller is allowed to see".
 */
public record ReportFilter(
        Hazard hazard,
        String ward,
        String district,
        LocalDate from,
        LocalDate to,
        Severity severity,
        IncidentStatus status
) {
    public ReportFilter {
        ward = blankToNull(ward);
        district = blankToNull(district);
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be on or before 'to' date");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
