package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.LinkCreateRequest;
import com.glowuprizz.lead_crm.admin.dto.LinkResponse;
import com.glowuprizz.lead_crm.common.ValidationException;
import com.glowuprizz.lead_crm.config.AppProperties;
import com.glowuprizz.lead_crm.domain.Form;
import com.glowuprizz.lead_crm.domain.Link;
import com.glowuprizz.lead_crm.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private static final String CODE_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_RETRIES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LinkRepository linkRepository;
    private final FormService formService;
    private final AppProperties props;

    @Transactional
    public LinkResponse issue(Long userId, Long formId, LinkCreateRequest req) {
        Form form = formService.getOwned(userId, formId);

        // 멱등: 같은 form + channel 링크가 이미 있으면 기존 것을 그대로 반환
        Link link = linkRepository.findByFormIdAndChannel(form.getId(), req.channel())
                .orElseGet(() -> linkRepository.save(
                        Link.of(form.getId(), req.channel(), generateUniqueCode())));

        return toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> list(Long userId, Long formId) {
        Form form = formService.getOwned(userId, formId);
        return linkRepository.findAllByFormId(form.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = randomCode();
            if (!linkRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ValidationException("링크 코드 생성에 실패했습니다. 다시 시도해 주세요");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private LinkResponse toResponse(Link link) {
        String url = props.publicBaseUrl() + "/f/" + link.getCode();
        return LinkResponse.from(link, url);
    }
}
