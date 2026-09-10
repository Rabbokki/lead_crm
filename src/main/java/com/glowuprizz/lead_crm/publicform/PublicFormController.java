package com.glowuprizz.lead_crm.publicform;

import com.glowuprizz.lead_crm.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Controller
@Profile("publicform")
@RequiredArgsConstructor
public class PublicFormController {

    private final PublicFormService service;
    private final AppProperties props;

    /** 바깥 페이지: iframe 하나만 렌더링하고 방문을 추적한다. */
    @GetMapping("/f/{code}")
    public String page(@PathVariable String code,
                       @CookieValue(name = "vid", required = false) String vid,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       Model model) {
        String visitorId = (vid == null || vid.isBlank()) ? UUID.randomUUID().toString() : vid;
        if (vid == null || vid.isBlank()) {
            // first-party 쿠키를 공개 폼 오리진(8081)에서만 심는다.
            ResponseCookie cookie = ResponseCookie.from("vid", visitorId)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        service.recordVisit(code, visitorId,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer"));
        model.addAttribute("code", code);
        return "public-form";
    }

    /** 안쪽 콘텐츠: 가공된 raw HTML을 CSP 헤더와 함께 반환한다. */
    @GetMapping("/f/{code}/content")
    public ResponseEntity<String> content(@PathVariable String code) {
        String html = service.renderContent(code);
        String csp = "default-src 'none'; style-src 'unsafe-inline'; img-src data:; "
                + "form-action " + props.publicBaseUrl() + "; frame-ancestors 'self'";
        return ResponseEntity.ok()
                .header("Content-Security-Policy", csp)
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                .body(html);
    }

    /** 신청 제출: 업로드 HTML의 &lt;form&gt; 이 x-www-form-urlencoded 로 그대로 POST 한다. */
    @PostMapping("/api/public/forms/{formId}/submit")
    public String submit(@PathVariable Long formId,
                         @RequestParam Map<String, String> params,
                         @CookieValue(name = "vid", required = false) String vid) {
        service.submit(formId, params, vid);
        return "submit-success";
    }
}
