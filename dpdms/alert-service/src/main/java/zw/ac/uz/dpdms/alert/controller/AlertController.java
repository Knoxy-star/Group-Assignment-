package zw.ac.uz.dpdms.alert.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.ac.uz.dpdms.alert.dto.AlertResponse;
import zw.ac.uz.dpdms.alert.service.AlertService;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.common.RequestContextResolver;

import java.util.List;
import java.util.Map;

/** Reached through the gateway as /alert-service/api/alerts. */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;
    private final RequestContextResolver contextResolver;

    public AlertController(AlertService service, RequestContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list(HttpServletRequest request) {
        RequestContext ctx = contextResolver.resolve(request);
        return ResponseEntity.ok(service.list(ctx));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
