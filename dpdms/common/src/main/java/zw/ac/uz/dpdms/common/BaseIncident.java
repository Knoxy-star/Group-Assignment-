package zw.ac.uz.dpdms.common;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Fields required on EVERY hazard incident record, per the brief:
 * "A shared incident metadata model (ward, district, province,
 * date/time of occurrence, reporter, severity, status, gps
 * coordinates) must be consistent across all five services."
 *
 * Each hazard service's own incident entity extends this and adds its
 * own 5 hazard-specific fields on top. Do not rename or remove any
 * field here when copying the pattern - the dashboard/report/map
 * aggregation depends on every hazard service exposing these under
 * the same names.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ward;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    /**
     * The auth-service user id of the ward recorder who submitted this
     * record. Not a foreign key (auth_db is a different schema/service)
     * - just stored as a plain id, matching microservice data ownership
     * rules (each service owns its own data, references other
     * services' entities by id only).
     */
    @Column(nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /**
     * Set by a supervisor on REJECTED or CORRECTIONS_REQUESTED. Null
     * otherwise. Shown back to the recorder so they know what to fix.
     */
    @Column(length = 1000)
    private String reviewNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = IncidentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
