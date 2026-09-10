package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.CampaignResponse;
import com.glowuprizz.lead_crm.admin.dto.FormCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.FormResponse;
import com.glowuprizz.lead_crm.config.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("admin")
public class CampaignController {

    private final CampaignService campaignService;
    private final FormService formService;
    private final CurrentUserProvider currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@Valid @RequestBody CampaignCreateRequest req) {
        return campaignService.create(currentUser.currentUserId(), req);
    }

    @GetMapping
    public List<CampaignResponse> list() {
        return campaignService.list(currentUser.currentUserId());
    }

    @GetMapping("/{id}")
    public CampaignResponse get(@PathVariable Long id) {
        return campaignService.get(currentUser.currentUserId(), id);
    }

    @PostMapping("/{id}/forms")
    @ResponseStatus(HttpStatus.CREATED)
    public FormResponse createForm(@PathVariable Long id,
                                   @Valid @RequestBody FormCreateRequest req) {
        return formService.create(currentUser.currentUserId(), id, req);
    }

    @GetMapping("/{id}/forms")
    public List<FormResponse> listForms(@PathVariable Long id) {
        return formService.listByCampaign(currentUser.currentUserId(), id);
    }
}
