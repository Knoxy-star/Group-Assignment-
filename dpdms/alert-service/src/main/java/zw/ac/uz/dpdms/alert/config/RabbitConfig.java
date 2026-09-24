package zw.ac.uz.dpdms.alert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the RabbitMQ topology. Spring creates these in RabbitMQ
 * automatically on startup if they don't exist yet.
 *
 *   hazard services --publish--> [dpdms.incidents] (topic exchange)
 *        routing key incident.approved.<hazard>
 *                                   |
 *                 binding: incident.approved.#
 *                                   v
 *               [alert-service.incident-approved] (durable queue)
 *                                   |
 *                                   v
 *                    IncidentApprovedListener
 *
 * A topic exchange (not a direct queue) means report-service or
 * dashboard-service can later bind their own queues to the same
 * events without any hazard service changing.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange incidentsExchange(AlertProperties props) {
        return new TopicExchange(props.exchange(), true, false);
    }

    @Bean
    public Queue incidentApprovedQueue(AlertProperties props) {
        return QueueBuilder.durable(props.queue()).build();
    }

    @Bean
    public Binding incidentApprovedBinding(Queue incidentApprovedQueue, TopicExchange incidentsExchange,
                                           AlertProperties props) {
        return BindingBuilder.bind(incidentApprovedQueue).to(incidentsExchange).with(props.routingKey());
    }

    /** Messages travel as JSON. Uses Spring's ObjectMapper so LocalDateTime works. */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
