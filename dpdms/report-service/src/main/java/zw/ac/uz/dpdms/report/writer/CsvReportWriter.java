package zw.ac.uz.dpdms.report.writer;

import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.report.model.Column;
import zw.ac.uz.dpdms.report.model.HazardSection;
import zw.ac.uz.dpdms.report.model.ReportData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CSV. One hazard: every field gets its own column. Several hazards: the
 * shared metadata as columns plus a Hazard column and one "Indicators"
 * column (hazards have different indicators, and a CSV has one header).
 * Starts with a UTF-8 byte-order mark so Excel opens it correctly.
 */
@Component
public class CsvReportWriter implements ReportWriter {

    @Override
    public ReportFormat format() {
        return ReportFormat.CSV;
    }

    @Override
    public byte[] write(ReportData data) {
        StringBuilder out = new StringBuilder("\uFEFF");
        List<HazardSection> sections = data.sections().stream().filter(s -> !s.isUnavailable()).toList();

        if (sections.size() == 1) {
            HazardSection s = sections.get(0);
            line(out, s.fullColumns().stream().map(Column::label).toList());
            for (Map<String, Object> row : s.rows()) {
                List<String> cells = new ArrayList<>();
                for (Column c : s.fullColumns()) {
                    cells.add(safe(ReportData.raw(row, c), ReportData.text(row, c)));
                }
                line(out, cells);
            }
        } else {
            List<Column> metadata = sections.isEmpty() ? List.of() : metadataOf(sections.get(0));
            List<String> header = new ArrayList<>();
            header.add("Hazard");
            metadata.forEach(c -> header.add(c.label()));
            header.add("Indicators");
            line(out, header);
            for (HazardSection s : sections) {
                List<Column> indicators = s.fullColumns().subList(metadata.size(), s.fullColumns().size());
                for (Map<String, Object> row : s.rows()) {
                    List<String> cells = new ArrayList<>();
                    cells.add(s.title());
                    for (Column c : metadata) {
                        cells.add(safe(ReportData.raw(row, c), ReportData.text(row, c)));
                    }
                    List<String> parts = new ArrayList<>();
                    for (Column c : indicators) {
                        parts.add(c.label() + ": " + ReportData.text(row, c));
                    }
                    cells.add(safe(null, String.join("; ", parts)));
                    line(out, cells);
                }
            }
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** The shared metadata columns (the first 10 of every section). */
    private static List<Column> metadataOf(HazardSection s) {
        return s.fullColumns().subList(0, Math.min(10, s.fullColumns().size()));
    }

    /**
     * Stops "CSV injection": text starting with = + - @ could run as a
     * formula when opened in Excel, so it is prefixed with an apostrophe.
     * Real numbers (e.g. a negative latitude) are left untouched.
     */
    private static String safe(Object raw, String text) {
        if (!(raw instanceof Number) && !text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            return "'" + text;
        }
        return text;
    }

    private static void line(StringBuilder out, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            String cell = cells.get(i);
            if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
                out.append('"').append(cell.replace("\"", "\"\"")).append('"');
            } else {
                out.append(cell);
            }
        }
        out.append("\r\n");
    }
}
