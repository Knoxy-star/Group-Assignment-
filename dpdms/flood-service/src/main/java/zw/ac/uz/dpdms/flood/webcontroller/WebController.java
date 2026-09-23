package zw.ac.uz.dpdms.flood.webcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the page shells only. Data loading and RBAC enforcement
 * happen via api.js calling the REST API in FloodController.
 */
@Controller
@RequestMapping("/web")
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
