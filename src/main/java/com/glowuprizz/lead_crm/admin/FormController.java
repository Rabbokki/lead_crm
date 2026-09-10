package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.FormResponse;
import com.glowuprizz.lead_crm.admin.dto.LeadResponse;
import com.glowuprizz.lead_crm.admin.dto.LinkCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.LinkResponse;
import com.glowuprizz.lead_crm.config.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/forms")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("admin")
public class FormController {

    private final FormService formService;
    private final LinkService linkService;
    private final LeadService leadService;
    private final CurrentUserProvider currentUser;

    @GetMapping("/{id}")
    public FormResponse get(@PathVariable Long id) {
        return formService.get(currentUser.currentUserId(), id);
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse issueLink(@PathVariable Long id,
                                  @Valid @RequestBody LinkCreateRequest req) {
        return linkService.issue(currentUser.currentUserId(), id, req);
    }

    @GetMapping("/{id}/links")
    public List<LinkResponse> listLinks(@PathVariable Long id) {
        return linkService.list(currentUser.currentUserId(), id);
    }

    @GetMapping("/{id}/leads")
    public List<LeadResponse> listLeads(@PathVariable Long id) {
        return leadService.listByForm(currentUser.currentUserId(), id);
    }
}
