package zw.ac.uz.dpdms.report.writer;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.report.model.Column;
import zw.ac.uz.dpdms.report.model.HazardSection;
import zw.ac.uz.dpdms.report.model.ReportData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Word. Title, filters, then one heading and table per hazard. */
@Component
public class DocxReportWriter implements ReportWriter {

    @Override
    public ReportFormat format() {
        return ReportFormat.DOCX;
    }

    @Override
    public byte[] write(ReportData data) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            text(doc, data.title(), 18, true, false);
            text(doc, "Generated " + data.generatedAtText() + " by " + data.generatedBy(), 10, false, true);
            for (String line : data.filterLines()) {
                text(doc, line, 10, false, false);
            }
            text(doc, "Total incidents: " + data.totalRows(), 10, true, false);

            for (HazardSection s : data.sections()) {
                text(doc, "", 6, false, false);
                text(doc, s.title() + " (" + s.rows().size() + " incident" + (s.rows().size() == 1 ? "" : "s") + ")",
                        14, true, false);
                if (s.isUnavailable()) {
                    text(doc, "Data unavailable: " + s.unavailable(), 10, false, true);
                    continue;
                }
                if (s.rows().isEmpty()) {
                    text(doc, "No incidents match the filters.", 10, false, true);
                    continue;
                }
                List<Column> cols = s.compactColumns();
                XWPFTable table = doc.createTable(s.rows().size() + 1, cols.size());
                table.setWidth("100%");
                for (int c = 0; c < cols.size(); c++) {
                    cell(table.getRow(0).getCell(c), cols.get(c).label(), true);
                }
                int r = 1;
                for (Map<String, Object> row : s.rows()) {
                    for (int c = 0; c < cols.size(); c++) {
                        cell(table.getRow(r).getCell(c), ReportData.text(row, cols.get(c)), false);
                    }
                    r++;
                }
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    private static void text(XWPFDocument doc, String value, int size, boolean bold, boolean italic) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(value);
        run.setFontSize(size);
        run.setBold(bold);
        run.setItalic(italic);
    }

    private static void cell(XWPFTableCell cell, String value, boolean bold) {
        XWPFRun run = cell.getParagraphs().get(0).createRun();
        run.setText(value);
        run.setFontSize(8);
        run.setBold(bold);
        if (bold) {
            cell.setColor("D9D9D9");
        }
    }
}
