package zw.ac.uz.dpdms.alert.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.alert.event.IncidentApprovedEvent;
import zw.ac.uz.dpdms.alert.service.AlertService;

/**
 * Receives "incident approved" events from RabbitMQ. Kept deliberately
 * thin: all the logic lives in AlertService so it can be tested
 * without RabbitMQ.
 */
@Component
public class IncidentApprovedListener {

    private final AlertService alertService;

    public IncidentApprovedListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = "${dpdms.alerts.queue}")
    public void onIncidentApproved(IncidentApprovedEvent event) {
        alertService.handle(event);
    }
}
