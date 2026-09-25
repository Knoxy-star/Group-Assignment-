package zw.ac.uz.dpdms.report.writer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.report.model.Column;
import zw.ac.uz.dpdms.report.model.HazardSection;
import zw.ac.uz.dpdms.report.model.ReportData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel. A "Summary" sheet (filters, counts per hazard, warnings) and one
 * sheet per hazard with all columns, numbers stored as real numbers, a
 * frozen header row and auto-filters.
 */
@Component
public class XlsxReportWriter implements ReportWriter {

    @Override
    public ReportFormat format() {
        return ReportFormat.XLSX;
    }

    @Override
    public byte[] write(ReportData data) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle title = style(wb, true, (short) 14, false);
            CellStyle bold = style(wb, true, (short) 11, false);
            CellStyle header = style(wb, true, (short) 11, true);

            Sheet summary = wb.createSheet("Summary");
            int r = 0;
            set(summary.createRow(r++), 0, data.title(), title);
            set(summary.createRow(r++), 0, "Generated " + data.generatedAtText() + " by " + data.generatedBy(), null);
            r++;
            for (String line : data.filterLines()) {
                set(summary.createRow(r++), 0, line, null);
            }
            r++;
            Row h = summary.createRow(r++);
            set(h, 0, "Hazard", header);
            set(h, 1, "Incidents", header);
            set(h, 2, "Note", header);
            for (HazardSection s : data.sections()) {
                Row row = summary.createRow(r++);
                set(row, 0, s.title(), null);
                row.createCell(1).setCellValue(s.rows().size());
                set(row, 2, s.isUnavailable() ? "Data unavailable: " + s.unavailable() : "", null);
            }
            Row total = summary.createRow(r);
            set(total, 0, "Total", bold);
            total.createCell(1).setCellValue(data.totalRows());
            for (int c = 0; c < 3; c++) {
                summary.autoSizeColumn(c);
            }

            for (HazardSection s : data.sections()) {
                Sheet sheet = wb.createSheet(s.title());
                List<Column> cols = s.fullColumns();
                Row head = sheet.createRow(0);
                for (int c = 0; c < cols.size(); c++) {
                    set(head, c, cols.get(c).label(), header);
                }
                int rowIndex = 1;
                if (s.isUnavailable()) {
                    set(sheet.createRow(rowIndex), 0, "Data unavailable: " + s.unavailable(), null);
                }
                for (Map<String, Object> item : s.rows()) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int c = 0; c < cols.size(); c++) {
                        Object raw = ReportData.raw(item, cols.get(c));
                        Cell cell = row.createCell(c);
                        if (raw instanceof Number n) {
                            cell.setCellValue(n.doubleValue());
                        } else {
                            cell.setCellValue(ReportData.text(item, cols.get(c)));
                        }
                    }
                }
                sheet.createFreezePane(0, 1);
                if (!s.rows().isEmpty()) {
                    sheet.setAutoFilter(new CellRangeAddress(0, s.rows().size(), 0, cols.size() - 1));
                }
                for (int c = 0; c < cols.size(); c++) {
                    sheet.autoSizeColumn(c);
                }
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private static CellStyle style(XSSFWorkbook wb, boolean bold, short size, boolean filled) {
        Font font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        if (filled) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private static void set(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
