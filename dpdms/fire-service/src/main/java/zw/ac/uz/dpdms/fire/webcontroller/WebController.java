package zw.ac.uz.dpdms.fire.webcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the page shells only. All real data loading and RBAC
 * enforcement happens client-side via api.js calling the REST API in
 * FireController - the server-rendered part here is just the
 * static page structure, not gated on auth (the API calls it makes
 * are what actually get rejected server-side if unauthorized).
 */
@Controller
@org.springframework.web.bind.annotation.RequestMapping("/web")
public class WebController {

    @GetMapping("/submit")
    public String submitForm() {
        return "submit";
    }

    @GetMapping("/my-submissions")
    public String mySubmissions() {
        return "my-submissions";
    }

    @GetMapping("/queue")
    public String supervisorQueue() {
        return "queue";
    }
}
