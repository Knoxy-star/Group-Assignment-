package zw.ac.uz.dpdms.alert.webcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Page shell only; data comes from /api/alerts via api.js. */
@Controller
@RequestMapping("/web")
public class WebController {

    @GetMapping("/alerts")
    public String alerts() {
        return "alerts";
    }
}
