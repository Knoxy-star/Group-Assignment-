package zw.ac.uz.dpdms.zoonotic.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zw.ac.uz.dpdms.common.BaseAuditLog;

@Entity
@Table(name = "zoonotic_disease_audit_log")
public class ZoonoticAuditLog extends BaseAuditLog {
}
