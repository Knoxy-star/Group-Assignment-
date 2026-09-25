package zw.ac.uz.dpdms.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the dashboard page shell only. All data loading and
 * RBAC enforcement happens client-side via api.js calling each
 * hazard service's REST API through the gateway.
 */
@Controller
@RequestMapping("/web")
public class WebController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}