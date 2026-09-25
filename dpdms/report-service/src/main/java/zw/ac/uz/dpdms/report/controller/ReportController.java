package zw.ac.uz.dpdms.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.common.RequestContextResolver;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.report.entity.ReportLog;
import zw.ac.uz.dpdms.report.model.ReportData;
import zw.ac.uz.dpdms.report.model.ReportFilter;
import zw.ac.uz.dpdms.report.service.ReportService;
import zw.ac.uz.dpdms.report.writer.ReportFormat;
import zw.ac.uz.dpdms.report.writer.ReportWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "PDF, Word, Excel and CSV incident reports across hazards")
public class ReportController {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final ReportService service;
    private final RequestContextResolver contextResolver;
    private final Map<ReportFormat, ReportWriter> writers = new EnumMap<>(ReportFormat.class);

    public ReportController(ReportService service, RequestContextResolver contextResolver,
                            List<ReportWriter> writerList) {
        this.service = service;
        this.contextResolver = contextResolver;
        writerList.forEach(w -> writers.put(w.format(), w));
    }

    @Operation(summary = "Generate and download a report",
            description = "All filters are optional. status defaults to APPROVED; only supervisors "
                    + "(own hazard) and the provincial admin may choose another status. "
                    + "Header X-Report-Warnings lists hazards whose data was unavailable.")
    @GetMapping
    public ResponseEntity<byte[]> generate(
            HttpServletRequest request,
            @RequestParam(value = "format", defaultValue = "pdf") String format,
            @RequestParam(value = "hazard", required = false) Hazard hazard,
            @RequestParam(value = "ward", required = false) String ward,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "severity", required = false) Severity severity,
            @RequestParam(value = "status", required = false) IncidentStatus status) throws IOException {

        RequestContext ctx = contextResolver.resolve(request);
        ReportFormat reportFormat = ReportFormat.parse(format);
        ReportFilter filter = new ReportFilter(hazard, ward, district, from, to, severity, status);

        ReportData data = service.build(ctx, filter);
        byte[] body = writers.get(reportFormat).write(data);
        service.record(ctx, reportFormat.name(), data);

        String filename = "dpdms-report-" + (hazard != null ? hazard.name().toLowerCase().replace('_', '-') + "-" : "")
                + LocalDateTime.now().format(FILE_STAMP) + "." + reportFormat.extension();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, reportFormat.contentType());
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.set("X-Report-Rows", String.valueOf(data.totalRows()));
        List<String> warnings = data.warnings();
        if (!warnings.isEmpty()) {
            headers.set("X-Report-Warnings", String.join(" | ", warnings));
        }
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    @Operation(summary = "Last 50 generated reports (provincial admin only)")
    @GetMapping("/history")
    public ResponseEntity<List<ReportLog>> history(HttpServletRequest request) {
        return ResponseEntity.ok(service.history(contextResolver.resolve(request)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleBadParameter(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of("error",
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'"));
    }
}
