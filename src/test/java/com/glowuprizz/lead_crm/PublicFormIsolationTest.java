package com.glowuprizz.lead_crm;

import com.glowuprizz.lead_crm.admin.CampaignService;
import com.glowuprizz.lead_crm.admin.FormService;
import com.glowuprizz.lead_crm.admin.LinkService;
import com.glowuprizz.lead_crm.admin.StatsService;
import com.glowuprizz.lead_crm.admin.TemplateService;
import com.glowuprizz.lead_crm.admin.dto.CampaignCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.CampaignResponse;
import com.glowuprizz.lead_crm.admin.dto.CampaignStatsResponse;
import com.glowuprizz.lead_crm.admin.dto.FormCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.FormResponse;
import com.glowuprizz.lead_crm.admin.dto.LinkCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.LinkResponse;
import com.glowuprizz.lead_crm.admin.dto.TemplateResponse;
import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.config.AppProperties;
import com.glowuprizz.lead_crm.repository.CampaignRepository;
import com.glowuprizz.lead_crm.repository.FormRepository;
import com.glowuprizz.lead_crm.repository.HtmlTemplateRepository;
import com.glowuprizz.lead_crm.repository.LeadRepository;
import com.glowuprizz.lead_crm.repository.LinkRepository;
import com.glowuprizz.lead_crm.repository.UserRepository;
import com.glowuprizz.lead_crm.repository.VisitRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 공개 폼 서버(publicform 프로파일)의 실패/격리 흐름 검증.
 * 테스트 데이터는 프로파일 무관 서비스로 생성하고, 공개 엔드포인트는 MockMvc로 실제 호출한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("publicform")
class PublicFormIsolationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AppProperties props;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TemplateService templateService;
    @Autowired
    CampaignService campaignService;
    @Autowired
    FormService formService;
    @Autowired
    LinkService linkService;
    @Autowired
    StatsService statsService;

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

    private static final String SIMPLE_HTML = """
            <!DOCTYPE html><html><body>
            <form>
              <input type="text" name="name" required>
              <input type="email" name="email" required>
              <button type="submit">신청</button>
            </form>
            </body></html>
            """;

    private Long adminUserId;

    @BeforeEach
    void setup() {
        leadRepository.deleteAllInBatch();
        visitRepository.deleteAllInBatch();
        linkRepository.deleteAllInBatch();
        formRepository.deleteAllInBatch();
        campaignRepository.deleteAllInBatch();
        htmlTemplateRepository.deleteAllInBatch();
        adminUserId = userRepository.findByEmail(props.admin().email()).orElseThrow().getId();
    }

    /** 템플릿·캠페인·폼·링크(1채널)를 만들고 링크 코드/폼id/캠페인id를 반환한다. */
    private Fixture createFixture(String html, Channel channel) {
        TemplateResponse tpl = templateService.create(adminUserId,
                new com.glowuprizz.lead_crm.admin.dto.TemplateCreateRequest("템플릿", html));
        CampaignResponse camp = campaignService.create(adminUserId,
                new CampaignCreateRequest("캠페인", "설명"));
        FormResponse form = formService.create(adminUserId, camp.id(),
                new FormCreateRequest(tpl.id(), "폼"));
        LinkResponse link = linkService.issue(adminUserId, form.id(), new LinkCreateRequest(channel));
        return new Fixture(camp.id(), form.id(), link.code());
    }

    private record Fixture(Long campaignId, Long formId, String code) {
    }

    @Test
    void 존재하지_않는_링크_코드는_404() throws Exception {
        mockMvc.perform(get("/f/{code}", "zzzznope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 필수_필드_누락_제출은_422() throws Exception {
        Fixture fx = createFixture(SIMPLE_HTML, Channel.INSTAGRAM);
        // name(required) 누락, email만 전송
        mockMvc.perform(post("/api/public/forms/{id}/submit", fx.formId())
                        .param("email", "a@b.com")
                        .param("__link", fx.code()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void 악성_HTML은_CSP와_sandbox로_격리된다() throws Exception {
        String malicious = Files.readString(Path.of("docs/samples/malicious.html"));
        Fixture fx = createFixture(malicious, Channel.INSTAGRAM);

        // 콘텐츠 응답에 default-src 'none' CSP 헤더가 존재해야 한다.
        mockMvc.perform(get("/f/{code}/content", fx.code()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));

        // 바깥 페이지의 iframe sandbox에 allow-same-origin이 포함되면 안 된다.
        mockMvc.perform(get("/f/{code}", fx.code()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sandbox")))
                .andExpect(content().string(not(containsString("allow-same-origin"))));
    }

    @Test
    void 동일_방문자_3회_방문은_방문3_방문자1() throws Exception {
        Fixture fx = createFixture(SIMPLE_HTML, Channel.INSTAGRAM);
        Cookie vid = new Cookie("vid", "same-visitor");
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/f/{code}", fx.code()).cookie(vid))
                    .andExpect(status().isOk());
        }

        CampaignStatsResponse stats = statsService.campaignStats(adminUserId, fx.campaignId());
        assertEquals(3, stats.visits());
        assertEquals(1, stats.visitors());
    }

    @Test
    void sandbox_iframe_제출은_Origin_null_이어도_200_이고_XFrameOptions_없음() throws Exception {
        Fixture fx = createFixture(SIMPLE_HTML, Channel.INSTAGRAM);
        // allow-same-origin 없는 sandbox iframe의 폼 제출은 Origin: null 로 전송된다.
        // CSRF 비활성 + X-Frame-Options 제거로, 제출이 성공(200)하고 성공 페이지가
        // 같은 iframe 안에서 렌더링될 수 있어야 한다(X-Frame-Options: DENY가 없어야 함).
        mockMvc.perform(post("/api/public/forms/{id}/submit", fx.formId())
                        .header("Origin", "null")
                        .param("name", "홍길동")
                        .param("email", "hong@example.com")
                        .param("__link", fx.code()))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Frame-Options"));
    }
}
