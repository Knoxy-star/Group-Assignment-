package zw.ac.uz.dpdms.alert.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Sends alerts to WhatsApp through Green API (free Developer plan).
 * Switched on with GREENAPI_ENABLED=true.
 */
@Component
public class GreenApiWhatsAppNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(GreenApiWhatsAppNotifier.class);

    private final boolean enabled;
    private final String apiUrl;
    private final String idInstance;
    private final String apiToken;
    private final List<String> recipients;
    private final RestClient restClient = RestClient.create();

    public GreenApiWhatsAppNotifier(@Value("${GREENAPI_ENABLED:false}") boolean enabled,
                                    @Value("${GREENAPI_API_URL:}") String apiUrl,
                                    @Value("${GREENAPI_ID_INSTANCE:}") String idInstance,
                                    @Value("${GREENAPI_API_TOKEN:}") String apiToken,
                                    @Value("${GREENAPI_RECIPIENTS:}") String recipients) {
        this.enabled = enabled;
        this.apiUrl = apiUrl == null ? "" : apiUrl.trim().replaceAll("/+$", "");
        this.idInstance = idInstance == null ? "" : idInstance.trim();
        this.apiToken = apiToken == null ? "" : apiToken.trim();
        this.recipients = recipients == null ? List.of()
                : Arrays.stream(recipients.split(",")).map(String::trim).filter(r -> !r.isEmpty()).toList();
        if (enabled && (this.apiUrl.isEmpty() || this.idInstance.isEmpty() || this.apiToken.isEmpty())) {
            log.warn("Green API is enabled but GREENAPI_API_URL, GREENAPI_ID_INSTANCE or GREENAPI_API_TOKEN is not set.");
        }
    }

    @Override
    public String channelName() {
        return "WHATSAPP";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<String> recipients() {
        return recipients;
    }

    /** Never write full phone numbers into logs or the database. */
    @Override
    public String displayRecipient(String number) {
        return number.length() <= 4 ? "****" : "****" + number.substring(number.length() - 4);
    }

    @Override
    public DeliveryResult send(String to, String subject, String messageText) {
        // Green API wants the number with no + and "@c.us" on the end
        String chatId = to.replace("+", "").replace(" ", "") + "@c.us";
        try {
            restClient.post()
                    .uri(apiUrl + "/waInstance{id}/sendMessage/{token}", idInstance, apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chatId", chatId, "message", "*" + subject + "*\n" + messageText))
                    .retrieve()
                    .toBodilessEntity();
            log.info("WhatsApp alert sent to {} via Green API", displayRecipient(to));
            return new DeliveryResult(DeliveryStatus.SENT, "Accepted by Green API");
        } catch (Exception e) {
            log.error("Green API send to {} failed: {}", displayRecipient(to), e.getMessage());
            String detail = String.valueOf(e.getMessage());
            return new DeliveryResult(DeliveryStatus.FAILED, detail.length() > 1000 ? detail.substring(0, 1000) : detail);
        }
    }
}