package zw.ac.uz.dpdms.alert.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import zw.ac.uz.dpdms.alert.config.WhatsAppProperties;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

import java.util.List;
import java.util.Map;

/**
 * Sends alerts through Meta's WhatsApp Cloud API:
 *   POST https://graph.facebook.com/{version}/{phone-number-id}/messages
 *
 * Used only when dpdms.whatsapp.enabled=true (WHATSAPP_ENABLED).
 */
@Component
public class WhatsAppCloudNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudNotifier.class);

    private final WhatsAppProperties props;
    private final RestClient restClient;

    public WhatsAppCloudNotifier(WhatsAppProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .build();
        if (props.enabled() && (isBlank(props.phoneNumberId()) || isBlank(props.accessToken())
                || props.recipientsOrEmpty().isEmpty())) {
            log.warn("WhatsApp is enabled but WHATSAPP_PHONE_NUMBER_ID, WHATSAPP_ACCESS_TOKEN "
                    + "or WHATSAPP_RECIPIENTS is not set - every send will fail.");
        }
    }

    @Override
    public String channelName() {
        return "WHATSAPP";
    }

    @Override
    public boolean isEnabled() {
        return props.enabled();
    }

    @Override
    public List<String> recipients() {
        return props.recipientsOrEmpty();
    }

    /** Never write full phone numbers into logs or the database. */
    @Override
    public String displayRecipient(String number) {
        return number.length() <= 4 ? "****" : "****" + number.substring(number.length() - 4);
    }

    @Override
    public DeliveryResult send(String to, String subject, String messageText) {
        try {
            restClient.post()
                    .uri("/{version}/{phoneNumberId}/messages", props.apiVersion(), props.phoneNumberId())
                    .header("Authorization", "Bearer " + props.accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildBody(to, messageText))
                    .retrieve()
                    .toBodilessEntity();
            log.info("WhatsApp alert sent to {}", displayRecipient(to));
            return new DeliveryResult(DeliveryStatus.SENT, "Accepted by WhatsApp Cloud API");
        } catch (RestClientResponseException e) {
            // Meta's error JSON explains the problem (expired token,
            // recipient not verified, template not found, ...)
            log.error("WhatsApp send to {} failed: HTTP {} {}", displayRecipient(to),
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return new DeliveryResult(DeliveryStatus.FAILED,
                    truncate("HTTP " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), 1000));
        } catch (Exception e) {
            log.error("WhatsApp send to {} failed: {}", displayRecipient(to), e.getMessage());
            return new DeliveryResult(DeliveryStatus.FAILED, truncate(String.valueOf(e.getMessage()), 1000));
        }
    }

    private Map<String, Object> buildBody(String to, String messageText) {
        if ("text".equalsIgnoreCase(props.messageType())) {
            return Map.of(
                    "messaging_product", "whatsapp",
                    "to", to,
                    "type", "text",
                    "text", Map.of("body", messageText));
        }
        // Template mode. hello_world has no variables, so the alert text
        // itself is only stored in alert_db / the log, not in the message.
        return Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", props.templateName(),
                        "language", Map.of("code", props.templateLanguage())));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
