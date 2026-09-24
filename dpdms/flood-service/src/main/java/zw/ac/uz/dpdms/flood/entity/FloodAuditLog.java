package zw.ac.uz.dpdms.flood.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zw.ac.uz.dpdms.common.BaseAuditLog;

@Entity
@Table(name = "flood_audit_log")
public class FloodAuditLog extends BaseAuditLog {
}
