package zw.ac.uz.dpdms.fire.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zw.ac.uz.dpdms.common.BaseAuditLog;

@Entity
@Table(name = "fire_audit_log")
public class FireAuditLog extends BaseAuditLog {
}
