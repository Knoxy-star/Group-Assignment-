package zw.ac.uz.dpdms.flood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.flood.entity.FloodIncident.Catchment;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test hitting the real REST layer (controller + service +
 * HazardScopeGuard + JPA, against an in-memory H2 database) - exercising
 * hazard-scoping at the HTTP boundary, exactly where the brief says it
 * must be enforced ("never relying on the gateway filter alone"). These
 * requests carry the X-User-* headers directly, the same way the gateway
 * forwards them, without the gateway itself needing to be running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FloodControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> validIncidentBody() {
        return Map.ofEntries(
                Map.entry("district", "Rushinga"),
                Map.entry("province", "Mashonaland Central"),
                Map.entry("occurredAt", LocalDateTime.now().withNano(0).toString()),
                Map.entry("severity", Severity.HIGH.name()),
                Map.entry("latitude", -16.7),
                Map.entry("longitude", 32.15),
                Map.entry("peakWaterLevelMetres", 4.5),
                Map.entry("catchment", Catchment.MAZOWE.name()),
                Map.entry("householdsDisplaced", 50),
                Map.entry("areaFloodedHectares", 20.0),
                Map.entry("inundationDurationDays", 2)
        );
    }

    @Test
    void recorderCanSubmitAnIncidentForTheirOwnWard() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "WARD_RECORDER")
                        .header("X-User-Hazard", "FLOOD")
                        .header("X-User-Ward", "Rushinga Ward 1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validIncidentBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.ward").value("Rushinga Ward 1"));
    }

    @Test
    void droughtRecorderIsRejectedByFloodService() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "WARD_RECORDER")
                        .header("X-User-Hazard", "DROUGHT")
                        .header("X-User-Ward", "Rushinga Ward 2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validIncidentBody())))
                .andExpect(status().isForbidden());
    }

    @Test
    void nationalViewerCannotSubmitIncidents() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "NATIONAL_VIEWER")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validIncidentBody())))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullApprovalJourneyFromSubmissionToApproved() throws Exception {
        String body = mockMvc.perform(post("/api/incidents")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "WARD_RECORDER")
                        .header("X-User-Hazard", "FLOOD")
                        .header("X-User-Ward", "Rushinga Ward 1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validIncidentBody())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(body).get("id").asLong();

        // A fire supervisor has no business here
        mockMvc.perform(post("/api/incidents/" + id + "/approve")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "PROVINCIAL_SUPERVISOR")
                        .header("X-User-Hazard", "FIRE"))
                .andExpect(status().isForbidden());

        // The flood supervisor approves it
        mockMvc.perform(post("/api/incidents/" + id + "/approve")
                        .header("X-User-Id", "3")
                        .header("X-User-Role", "PROVINCIAL_SUPERVISOR")
                        .header("X-User-Hazard", "FLOOD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
