package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.CampaignCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.FormCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.LinkCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.TemplateCreateRequest;
import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.config.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 관리자 Thymeleaf 화면 컨트롤러. 조회는 AdminPageService, 생성은 기존 도메인 서비스에 위임한다.
 * 화면용 POST는 /admin/** 경로라 CSRF가 적용되며(csrf ignoring은 /api/** 한정),
 * Thymeleaf th:action 폼에 CSRF hidden 필드가 자동 주입된다.
 */
@Controller
@Profile("admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final AdminPageService adminPageService;
    private final CampaignService campaignService;
    private final TemplateService templateService;
    private final FormService formService;
    private final LinkService linkService;
    private final CurrentUserProvider currentUser;

    @GetMapping("/admin/campaigns")
    public String campaigns(Model model) {
        model.addAttribute("campaigns", adminPageService.campaignRows(currentUser.currentUserId()));
        return "campaigns";
    }

    @PostMapping("/admin/campaigns")
    public String createCampaign(@RequestParam String name,
                                 @RequestParam(required = false) String description) {
        campaignService.create(currentUser.currentUserId(),
                new CampaignCreateRequest(name, description));
        return "redirect:/admin/campaigns";
    }

    @PostMapping("/admin/templates")
    public String createTemplate(@RequestParam String name,
                                 @RequestParam String rawHtml) {
        templateService.create(currentUser.currentUserId(),
                new TemplateCreateRequest(name, rawHtml));
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/admin/campaigns/{id}")
    public String campaignDetail(@PathVariable Long id, Model model) {
        model.addAttribute("detail", adminPageService.campaignDetail(currentUser.currentUserId(), id));
        model.addAttribute("campaignId", id);
        return "campaign-detail";
    }

    @PostMapping("/admin/campaigns/{id}/forms")
    public String createForm(@PathVariable Long id,
                             @RequestParam Long templateId,
                             @RequestParam String name) {
        formService.create(currentUser.currentUserId(), id, new FormCreateRequest(templateId, name));
        return "redirect:/admin/campaigns/" + id;
    }

    /** 폼 하나에 대해 채널 4종 링크를 일괄 발급한다(각 채널 멱등). */
    @PostMapping("/admin/forms/{formId}/links")
    public String issueAllLinks(@PathVariable Long formId,
                                @RequestParam Long campaignId) {
        Long userId = currentUser.currentUserId();
        for (Channel channel : Channel.values()) {
            linkService.issue(userId, formId, new LinkCreateRequest(channel));
        }
        return "redirect:/admin/campaigns/" + campaignId;
    }
}
