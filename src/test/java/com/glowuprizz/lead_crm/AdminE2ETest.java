package com.glowuprizz.lead_crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glowuprizz.lead_crm.config.AppProperties;
import com.glowuprizz.lead_crm.publicform.PublicFormService;
import com.glowuprizz.lead_crm.repository.CampaignRepository;
import com.glowuprizz.lead_crm.repository.FormRepository;
import com.glowuprizz.lead_crm.repository.HtmlTemplateRepository;
import com.glowuprizz.lead_crm.repository.LeadRepository;
import com.glowuprizz.lead_crm.repository.LinkRepository;
import com.glowuprizz.lead_crm.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 성공 흐름 E2E + 미인증 실패(401). admin 프로파일로 실행한다.
 * 관리자 API는 MockMvc로 호출하고, 공개 폼 방문/제출은 프로파일 무관 서비스(PublicFormService)로 재현한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("admin")
class AdminE2ETest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    AppProperties props;
    @Autowired
    PublicFormService publicFormService;

    @Autowired
    LeadRepository leadRepository;
    @Autowired
    VisitRepository visitRepository;
    @Autowired
    LinkRepository linkRepository;
    @Autowired
    FormRepository formRepository;
    @Autowired
    CampaignRepository campaignRepository;
    @Autowired
    HtmlTemplateRepository htmlTemplateRepository;

    private static final String TEMPLATE_HTML = """
            <!DOCTYPE html><html><body>
            <form>
              <input type="text" name="name" required>
              <input type="email" name="email" required>
              <input type="tel" name="phone">
              <button type="submit">신청</button>
            </form>
            </body></html>
            """;

    @BeforeEach
    void clean() {
        leadRepository.deleteAllInBatch();
        visitRepository.deleteAllInBatch();
        linkRepository.deleteAllInBatch();
        formRepository.deleteAllInBatch();
        campaignRepository.deleteAllInBatch();
        htmlTemplateRepository.deleteAllInBatch();
    }

    private String adminEmail() {
        return props.admin().email();
    }

    private long extractId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asLong();
    }

    private String issueLink(long formId, String channel) throws Exception {
        String body = mockMvc.perform(post("/api/admin/forms/{id}/links", formId)
                        .with(user(adminEmail()))
                        .contentType(APPLICATION_JSON)
                        .content("{\"channel\":\"" + channel + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("code").asText();
    }

    @Test
    void 성공_흐름_E2E_통계까지() throws Exception {
        // 1) 템플릿 등록
        String tplJson = mockMvc.perform(post("/api/admin/templates")
                        .with(user(adminEmail()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "전자책 템플릿", "rawHtml", TEMPLATE_HTML))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long templateId = extractId(tplJson);

        // 2) 캠페인 생성
        String campJson = mockMvc.perform(post("/api/admin/campaigns")
                        .with(user(adminEmail()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "9월 캠페인", "description", "리드마그넷"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long campaignId = extractId(campJson);

        // 3) 폼 생성
        String formJson = mockMvc.perform(post("/api/admin/campaigns/{id}/forms", campaignId)
                        .with(user(adminEmail()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("templateId", templateId, "name", "무료 신청 폼"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long formId = extractId(formJson);

        // 4) 채널 4개 링크 발급
        String instaCode = issueLink(formId, "INSTAGRAM");
        String youtubeCode = issueLink(formId, "YOUTUBE");
        issueLink(formId, "X");
        issueLink(formId, "THREADS");

        // 5) 서로 다른 vid로 방문 — 인스타 3명, 유튜브 2명 (총 방문 5, 방문자 5)
        publicFormService.recordVisit(instaCode, "ig-1", null, null, null);
        publicFormService.recordVisit(instaCode, "ig-2", null, null, null);
        publicFormService.recordVisit(instaCode, "ig-3", null, null, null);
        publicFormService.recordVisit(youtubeCode, "yt-1", null, null, null);
        publicFormService.recordVisit(youtubeCode, "yt-2", null, null, null);

        // 6) 인스타 1명, 유튜브 1명 제출 (총 신청 2)
        publicFormService.submit(formId,
                Map.of("name", "김철수", "email", "chulsoo@example.com", "__link", instaCode), "ig-1");
        publicFormService.submit(formId,
                Map.of("name", "이영희", "email", "younghee@example.com", "__link", youtubeCode), "yt-1");

        // 7) 캠페인 통계 검증: visits=5, visitors=5, submissions=2, 전환율=40.0
        mockMvc.perform(get("/api/admin/campaigns/{id}/stats", campaignId).with(user(adminEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visits").value(5))
                .andExpect(jsonPath("$.visitors").value(5))
                .andExpect(jsonPath("$.submissions").value(2))
                .andExpect(jsonPath("$.conversionRate").value(40.0));

        // 8) 채널별 통계 검증: 인스타 33.3, 유튜브 50.0, X 0.0, 스레드 0.0 (순서: INSTAGRAM, X, YOUTUBE, THREADS)
        mockMvc.perform(get("/api/admin/campaigns/{id}/stats/channels", campaignId).with(user(adminEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].channel").value("INSTAGRAM"))
                .andExpect(jsonPath("$[0].visitors").value(3))
                .andExpect(jsonPath("$[0].submissions").value(1))
                .andExpect(jsonPath("$[0].conversionRate").value(33.3))
                .andExpect(jsonPath("$[1].channel").value("X"))
                .andExpect(jsonPath("$[1].conversionRate").value(0.0))
                .andExpect(jsonPath("$[2].channel").value("YOUTUBE"))
                .andExpect(jsonPath("$[2].visitors").value(2))
                .andExpect(jsonPath("$[2].submissions").value(1))
                .andExpect(jsonPath("$[2].conversionRate").value(50.0))
                .andExpect(jsonPath("$[3].channel").value("THREADS"))
                .andExpect(jsonPath("$[3].conversionRate").value(0.0));
    }

    @Test
    void 미인증_관리자_API_접근은_401() throws Exception {
        mockMvc.perform(get("/api/admin/campaigns"))
                .andExpect(status().isUnauthorized());
    }
}
