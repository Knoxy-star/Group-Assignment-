package zw.ac.uz.dpdms.report.writer;

import zw.ac.uz.dpdms.report.model.ReportData;

import java.io.IOException;

/**
 * One output format. Every writer receives the same ReportData, so the
 * four formats always contain the same incidents. Adding a format means
 * adding one class; nothing else changes.
 */
public interface ReportWriter {

    ReportFormat format();

    byte[] write(ReportData data) throws IOException;
}
