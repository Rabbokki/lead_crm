package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.TemplateCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.TemplateResponse;
import com.glowuprizz.lead_crm.config.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("admin")
public class TemplateController {

    private final TemplateService templateService;
    private final CurrentUserProvider currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateResponse create(@Valid @RequestBody TemplateCreateRequest req) {
        return templateService.create(currentUser.currentUserId(), req);
    }

    @GetMapping
    public List<TemplateResponse> list() {
        return templateService.list(currentUser.currentUserId());
    }
}
