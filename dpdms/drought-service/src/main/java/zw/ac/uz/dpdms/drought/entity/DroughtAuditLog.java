package zw.ac.uz.dpdms.drought.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zw.ac.uz.dpdms.common.BaseAuditLog;

@Entity
@Table(name = "drought_audit_log")
public class DroughtAuditLog extends BaseAuditLog {
}
