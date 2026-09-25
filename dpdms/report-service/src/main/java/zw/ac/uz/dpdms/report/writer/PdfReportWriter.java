package zw.ac.uz.dpdms.report.writer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.report.model.Column;
import zw.ac.uz.dpdms.report.model.HazardSection;
import zw.ac.uz.dpdms.report.model.ReportData;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** PDF (landscape A4). Title, filters, one table per hazard, page numbers. */
@Component
public class PdfReportWriter implements ReportWriter {

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font NOTE = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);
    private static final Font TABLE_HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font TABLE_BODY = FontFactory.getFont(FontFactory.HELVETICA, 7);

    @Override
    public ReportFormat format() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] write(ReportData data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 36);
        try {
            PdfWriter.getInstance(doc, out);
            HeaderFooter footer = new HeaderFooter(new Phrase("Rushinga DPDMS - page ", NOTE), true);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorder(Rectangle.NO_BORDER);
            doc.setFooter(footer);
            doc.open();

            doc.add(new Paragraph(data.title(), TITLE));
            doc.add(new Paragraph("Generated " + data.generatedAtText() + " by " + data.generatedBy(), NOTE));
            for (String line : data.filterLines()) {
                doc.add(new Paragraph(line, NORMAL));
            }
            doc.add(new Paragraph("Total incidents: " + data.totalRows(), HEADING));

            for (HazardSection s : data.sections()) {
                Paragraph heading = new Paragraph(s.title() + " (" + s.rows().size() + " incident"
                        + (s.rows().size() == 1 ? "" : "s") + ")", HEADING);
                heading.setSpacingBefore(12f);
                heading.setSpacingAfter(4f);
                doc.add(heading);

                if (s.isUnavailable()) {
                    doc.add(new Paragraph("Data unavailable: " + s.unavailable(), NOTE));
                    continue;
                }
                if (s.rows().isEmpty()) {
                    doc.add(new Paragraph("No incidents match the filters.", NOTE));
                    continue;
                }
                List<Column> cols = s.compactColumns();
                PdfPTable table = new PdfPTable(cols.size());
                table.setWidthPercentage(100);
                table.setHeaderRows(1);
                for (Column c : cols) {
                    PdfPCell cell = new PdfPCell(new Phrase(c.label(), TABLE_HEAD));
                    cell.setBackgroundColor(new Color(217, 217, 217));
                    cell.setPadding(3f);
                    table.addCell(cell);
                }
                for (Map<String, Object> row : s.rows()) {
                    for (Column c : cols) {
                        PdfPCell cell = new PdfPCell(new Phrase(ReportData.text(row, c), TABLE_BODY));
                        cell.setPadding(3f);
                        table.addCell(cell);
                    }
                }
                doc.add(table);
            }
        } catch (DocumentException e) {
            throw new IOException("Could not build PDF: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return out.toByteArray();
    }
}
