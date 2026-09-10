package com.glowuprizz.lead_crm.repository;

import com.glowuprizz.lead_crm.common.Channel;
import com.glowuprizz.lead_crm.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByCode(String code);
    List<Link> findAllByFormId(Long formId);
    Optional<Link> findByFormIdAndChannel(Long formId, Channel channel);
    boolean existsByCode(String code);
}