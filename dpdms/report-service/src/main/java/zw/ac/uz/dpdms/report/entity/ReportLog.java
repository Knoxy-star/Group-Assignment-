package zw.ac.uz.dpdms.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Audit log: one row per generated report - who, when, what, which filters. */
@Entity
@Table(name = "report_log")
@Getter
@Setter
public class ReportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String role;

    /** PDF, DOCX, XLSX or CSV. */
    @Column(nullable = false, length = 10)
    private String format;

    @Column(length = 1000)
    private String filters;

    private int rowCount;

    /** Hazards whose data was unavailable, if any. */
    @Column(length = 1000)
    private String warnings;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
