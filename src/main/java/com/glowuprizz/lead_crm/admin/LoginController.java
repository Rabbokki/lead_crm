package com.glowuprizz.lead_crm.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("admin")
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
