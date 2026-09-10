package com.glowuprizz.lead_crm.admin;

import com.glowuprizz.lead_crm.admin.dto.LeadResponse;
import com.glowuprizz.lead_crm.domain.Form;
import com.glowuprizz.lead_crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final FormService formService;

    @Transactional(readOnly = true)
    public List<LeadResponse> listByForm(Long userId, Long formId) {
        Form form = formService.getOwned(userId, formId);
        return leadRepository.findAllByFormIdOrderByIdDesc(form.getId()).stream()
                .map(LeadResponse::from)
                .toList();
    }
}
