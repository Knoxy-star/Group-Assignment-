package zw.ac.uz.dpdms.mining.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import zw.ac.uz.dpdms.common.BaseAuditLog;

@Entity
@Table(name = "mining_accident_audit_log")
public class MiningAuditLog extends BaseAuditLog {
}
