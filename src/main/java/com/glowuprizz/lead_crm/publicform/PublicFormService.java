package com.glowuprizz.lead_crm.publicform;

import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.common.NotFoundException;
import com.glowuprizz.lead_crm.common.ValidationException;
import com.glowuprizz.lead_crm.config.AppProperties;
import com.glowuprizz.lead_crm.domain.FieldDef;
import com.glowuprizz.lead_crm.domain.Form;
import com.glowuprizz.lead_crm.domain.HtmlTemplate;
import com.glowuprizz.lead_crm.domain.Lead;
import com.glowuprizz.lead_crm.domain.Link;
import com.glowuprizz.lead_crm.domain.Visit;
import com.glowuprizz.lead_crm.repository.FormRepository;
import com.glowuprizz.lead_crm.repository.HtmlTemplateRepository;
import com.glowuprizz.lead_crm.repository.LeadRepository;
import com.glowuprizz.lead_crm.repository.LinkRepository;
import com.glowuprizz.lead_crm.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicFormService {

    private final LinkRepository linkRepository;
    private final FormRepository formRepository;
    private final HtmlTemplateRepository htmlTemplateRepository;
    private final VisitRepository visitRepository;
    private final LeadRepository leadRepository;
    private final AppProperties props;

    /**
     * GET /f/{code} 진입 시 방문 1건 기록. code가 없으면 404.
     * 방문(행)은 매번 늘고 visitor_id(vid)는 동일하게 유지되어 방문/방문자 구분의 근거가 된다.
     */
    @Transactional
    public void recordVisit(String code, String visitorId, String ip, String userAgent, String referrer) {
        Link link = requireLink(code);
        visitRepository.save(Visit.of(
                link.getFormId(), link.getId(), link.getChannel(),
                visitorId, ip, userAgent, referrer));
    }

    /**
     * GET /f/{code}/content 의 본문.
     * 업로드된 raw HTML을 jsoup으로 파싱해 &lt;form&gt; 의 action/method를 서버가 덮어쓰고
     * hidden input(__link)을 주입한 뒤 반환한다. code가 없으면 404.
     */
    @Transactional(readOnly = true)
    public String renderContent(String code) {
        Link link = requireLink(code);
        Form form = formRepository.findById(link.getFormId())
                .orElseThrow(() -> new NotFoundException("form not found: " + link.getFormId()));
        HtmlTemplate template = htmlTemplateRepository.findById(form.getTemplateId())
                .orElseThrow(() -> new NotFoundException("template not found: " + form.getTemplateId()));
        return processHtml(template.getRawHtml(), form.getId(), code);
    }

    /**
     * POST /api/public/forms/{formId}/submit 처리.
     * - required=true 필드가 비어 있으면 422
     * - fieldSchema에 정의되지 않은 필드는 무시
     * - __link 로 링크를 조회해 link_id/channel 결정(없으면 null)
     */
    @Transactional
    public void submit(Long formId, Map<String, String> params, String visitorId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new NotFoundException("form not found: " + formId));

        Map<String, Object> payload = new LinkedHashMap<>();
        for (FieldDef field : form.getFieldSchema()) {
            String value = params.get(field.name());
            if (field.required() && (value == null || value.isBlank())) {
                throw new ValidationException("필수 필드가 누락되었습니다: " + field.name());
            }
            if (params.containsKey(field.name())) {
                payload.put(field.name(), value);
            }
        }

        Long linkId = null;
        Channel channel = null;
        String code = params.get("__link");
        if (code != null && !code.isBlank()) {
            Link link = linkRepository.findByCode(code).orElse(null);
            if (link != null) {
                linkId = link.getId();
                channel = link.getChannel();
            }
        }

        leadRepository.save(Lead.of(formId, linkId, channel, visitorId, payload));
    }

    private Link requireLink(String code) {
        return linkRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("link not found: " + code));
    }

    private String processHtml(String rawHtml, Long formId, String code) {
        Document doc = Jsoup.parse(rawHtml);
        String action = props.publicBaseUrl() + "/api/public/forms/" + formId + "/submit";
        for (Element form : doc.select("form")) {
            form.attr("action", action);
            form.attr("method", "post");
            Element hidden = doc.createElement("input")
                    .attr("type", "hidden")
                    .attr("name", "__link")
                    .attr("value", code);
            form.prependChild(hidden);
        }
        return doc.outerHtml();
    }
}
