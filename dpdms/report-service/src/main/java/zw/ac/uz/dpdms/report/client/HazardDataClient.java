package zw.ac.uz.dpdms.report.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reads incidents from a hazard service's own REST API:
 *   GET http://{hazard-service}/api/incidents?status=APPROVED
 *
 * The caller's identity (the X-User-* headers the gateway added) is passed
 * on unchanged, so each hazard service applies ITS OWN access rules to the
 * report request exactly as it would to a page request. report-service
 * never reads another service's database.
 *
 * Rows are read as generic maps, so every hazard's indicators (water
 * level, area burned, pathogen, ...) become report columns without this
 * service knowing any hazard's DTO.
 */
@Component
public class HazardDataClient {

    private static final Map<Hazard, String> SERVICES = new EnumMap<>(Hazard.class);

    static {
        SERVICES.put(Hazard.FLOOD, "flood-service");
        SERVICES.put(Hazard.DROUGHT, "drought-service");
        SERVICES.put(Hazard.FIRE, "fire-service");
        SERVICES.put(Hazard.ZOONOTIC_DISEASE, "zoonotic-disease-service");
        SERVICES.put(Hazard.MINING_ACCIDENT, "mining-accident-service");
    }

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HazardDataClient(RestClient.Builder loadBalancedRestClientBuilder, ObjectMapper objectMapper) {
        this.restClient = loadBalancedRestClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public static String serviceName(Hazard hazard) {
        return SERVICES.get(hazard);
    }

    /**
     * @throws HazardServiceException if the service answered with an error
     * @throws RuntimeException        (other) if it could not be reached
     */
    public List<Map<String, Object>> fetchIncidents(Hazard hazard, RequestContext ctx, IncidentStatus status) {
        String service = serviceName(hazard);
        try {
            List<Map<String, Object>> rows = restClient.get()
                    .uri("http://" + service + "/api/incidents?status={status}", status.name())
                    .headers(h -> {
                        h.set("X-User-Id", String.valueOf(ctx.userId()));
                        h.set("X-User-Role", ctx.role().name());
                        if (ctx.hazard() != null) {
                            h.set("X-User-Hazard", ctx.hazard().name());
                        }
                        if (ctx.ward() != null) {
                            h.set("X-User-Ward", ctx.ward());
                        }
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return rows == null ? List.of() : rows;
        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            throw new HazardServiceException(
                    service + " refused the request (HTTP " + code + "): " + errorMessage(e), code == 403);
        }
    }

    /** Hazard services answer errors as {"error": "..."}; fall back to the raw body. */
    private String errorMessage(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        try {
            Object error = objectMapper.readValue(body, Map.class).get("error");
            if (error != null) {
                return String.valueOf(error);
            }
        } catch (Exception ignored) {
            // not JSON
        }
        return body.length() > 200 ? body.substring(0, 200) : body;
    }
}
