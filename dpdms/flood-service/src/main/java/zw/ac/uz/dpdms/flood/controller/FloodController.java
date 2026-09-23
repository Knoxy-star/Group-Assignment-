package zw.ac.uz.dpdms.flood.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.common.RequestContextResolver;
import zw.ac.uz.dpdms.flood.dto.DecisionRequest;
import zw.ac.uz.dpdms.flood.dto.IncidentCreateRequest;
import zw.ac.uz.dpdms.flood.dto.IncidentResponse;
import zw.ac.uz.dpdms.flood.dto.IncidentUpdateRequest;
import zw.ac.uz.dpdms.flood.service.FloodService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Reached through the gateway as /flood-service/api/incidents/**
 * (gateway strips the service-name prefix before forwarding).
 *
 * REFERENCE PATTERN: every controller method resolves the caller's
 * RequestContext first, then hands off to the service layer, which is
 * where the actual RBAC/scoping decisions are made (never trust the
 * gateway filter alone - see HazardScopeGuard in the common module).
 */
@RestController
@RequestMapping("/api/incidents")
public class FloodController {

    private final FloodService service;
    private final RequestContextResolver contextResolver;

    public FloodController(FloodService service, RequestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(HttpServletRequest request, @Valid @RequestBody IncidentCreateRequest req) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(ctx, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getById(HttpServletRequest request, @PathVariable("id") Long id) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.getById(ctx, id));
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> list(HttpServletRequest request,
                                                         @RequestParam(value = "status", required = false) IncidentStatus status) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.list(ctx, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> update(HttpServletRequest request, @PathVariable("id") Long id,
                                                     @Valid @RequestBody IncidentUpdateRequest req) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.update(ctx, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable("id") Long id) {
        RequestContext ctx = contextResolver.resolve(request);
        service.delete(ctx, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<IncidentResponse> approve(HttpServletRequest request, @PathVariable("id") Long id) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.approve(ctx, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<IncidentResponse> reject(HttpServletRequest request, @PathVariable("id") Long id,
                                                      @Valid @RequestBody DecisionRequest req) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.reject(ctx, id, req));
    }

    @PostMapping("/{id}/request-corrections")
    public ResponseEntity<IncidentResponse> requestCorrections(HttpServletRequest request, @PathVariable("id") Long id,
                                                                  @Valid @RequestBody DecisionRequest req) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.requestCorrections(ctx, id, req));
    }

    // ---------- exception mapping ----------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
