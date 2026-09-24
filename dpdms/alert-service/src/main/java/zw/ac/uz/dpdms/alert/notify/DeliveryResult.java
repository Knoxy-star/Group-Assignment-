package zw.ac.uz.dpdms.alert.notify;

import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

public record DeliveryResult(DeliveryStatus status, String detail) {}
