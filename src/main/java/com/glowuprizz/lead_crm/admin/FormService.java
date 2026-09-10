package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.FormCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.FormResponse;
import com.glowuprizz.lead_crm.common.NotFoundException;
import com.glowuprizz.lead_crm.common.ValidationException;
import com.glowuprizz.lead_crm.domain.FieldDef;
import com.glowuprizz.lead_crm.domain.Form;
import com.glowuprizz.lead_crm.domain.HtmlTemplate;
import com.glowuprizz.lead_crm.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final CampaignService campaignService;
    private final TemplateService templateService;

    @Transactional
    public FormResponse create(Long userId, Long campaignId, FormCreateRequest req) {
        // 소유권 검증: 캠페인·템플릿 모두 현재 운영자 소유여야 함 (아니면 404)
        campaignService.getOwned(userId, campaignId);
        HtmlTemplate template = templateService.getOwned(userId, req.templateId());

        List<FieldDef> schema = extractFields(template.getRawHtml());
        if (schema.isEmpty()) {
            throw new ValidationException("템플릿 HTML에서 name 속성을 가진 입력 필드를 찾지 못했습니다");
        }

        Form saved = formRepository.save(
                Form.of(campaignId, template.getId(), req.name(), schema));
        return FormResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FormResponse get(Long userId, Long formId) {
        return FormResponse.from(getOwned(userId, formId));
    }

    @Transactional(readOnly = true)
    public List<FormResponse> listByCampaign(Long userId, Long campaignId) {
        campaignService.getOwned(userId, campaignId);
        return formRepository.findAllByCampaignId(campaignId).stream()
                .map(FormResponse::from)
                .toList();
    }

    /** 소유한 폼 엔티티를 반환한다. 폼이 없거나 폼의 캠페인이 타인 소유면 404. */
    @Transactional(readOnly = true)
    public Form getOwned(Long userId, Long formId) {
        Form f = formRepository.findById(formId)
                .orElseThrow(() -> new NotFoundException("form not found: " + formId));
        // 폼 소유권은 소속 캠페인 소유권으로 판단한다.
        campaignService.getOwned(userId, f.getCampaignId());
        return f;
    }

    /**
     * 템플릿 HTML을 파싱해 name 속성을 가진 input/textarea/select 를 FieldDef 로 추출한다.
     * - type: input 은 type 속성(없으면 "text"), textarea 는 "textarea", select 는 "select"
     * - required: required 속성 유무
     * - name 중복 시 첫 번째만 사용 (문서 순서 유지)
     */
    private List<FieldDef> extractFields(String rawHtml) {
        Document doc = Jsoup.parse(rawHtml);
        List<FieldDef> fields = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Element el : doc.select("input[name], textarea[name], select[name]")) {
            String name = el.attr("name").trim();
            if (name.isEmpty() || !seen.add(name)) {
                continue;
            }
            String type = switch (el.tagName().toLowerCase()) {
                case "textarea" -> "textarea";
                case "select" -> "select";
                default -> {
                    String t = el.attr("type").trim();
                    yield t.isEmpty() ? "text" : t.toLowerCase();
                }
            };
            boolean required = el.hasAttr("required");
            fields.add(new FieldDef(name, type, required));
        }
        return fields;
    }
}
