package zw.ac.uz.dpdms.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publishes "incident approved" events to RabbitMQ so alert-service (and
 * any later consumer) can react, without the hazard service ever calling
 * another service over HTTP.
 *
 *   hazard service --publishApproved()--> [dpdms.incidents] topic exchange
 *                  routing key: incident.approved.<hazard>   e.g. incident.approved.flood
 *
 * Usage in a hazard service's approve() method, AFTER the incident is saved:
 *
 *   eventPublisher.publishApproved(new IncidentApprovedEvent(
 *           SERVICE_HAZARD, incident.getId(), incident.getWard(),
 *           incident.getDistrict(), incident.getProvince(),
 *           incident.getSeverity(), incident.getOccurredAt(),
 *           "short hazard-specific summary"));
 *
 * Design rules:
 * - NEVER throws. If RabbitMQ is down the approval still succeeds; the
 *   failure is logged as a warning. Alerts are a side effect, not part
 *   of the approval itself.
 * - If called inside a database transaction, the message is only sent
 *   after that transaction commits, so an alert is never sent for an
 *   approval that was rolled back.
 * - Builds the JSON itself with Spring Boot's ObjectMapper (dates as
 *   "2026-09-23T04:33:00") and sets content_type=application/json, the
 *   exact format alert-service expects. It deliberately does not
 *   register a MessageConverter bean, so it can't clash with the one
 *   alert-service already defines.
 *
 * Picked up automatically because every service's @ComponentScan
 * already includes zw.ac.uz.dpdms.common.
 */
@Component
public class IncidentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(IncidentEventPublisher.class);

    public static final String APPROVED_ROUTING_PREFIX = "incident.approved.";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public IncidentEventPublisher(RabbitTemplate rabbitTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${dpdms.events.exchange:dpdms.incidents}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    public void publishApproved(IncidentApprovedEvent event) {
        if (event == null || event.hazard() == null || event.incidentId() == null) {
            log.warn("Not publishing incident-approved event with missing hazard/incidentId: {}", event);
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(IncidentApprovedEvent event) {
        String routingKey = APPROVED_ROUTING_PREFIX + event.hazard().name().toLowerCase();
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            Message message = MessageBuilder.withBody(body)
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setContentEncoding("UTF-8")
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.send(exchange, routingKey, message);
            log.info("Published {} incident {} to exchange '{}' with routing key '{}'",
                    event.hazard(), event.incidentId(), exchange, routingKey);
        } catch (Exception e) {
            log.warn("Could not publish {} incident {} to RabbitMQ (approval is still saved): {}",
                    event.hazard(), event.incidentId(), e.getMessage());
        }
    }
}
