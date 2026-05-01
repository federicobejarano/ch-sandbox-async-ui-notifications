package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationCreateRequest;
import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliationService {

	private final AffiliationRepository repository;
	private final ApplicationEventPublisher events;

	public AffiliationService(AffiliationRepository repository, ApplicationEventPublisher events) {
		this.repository = repository;
		this.events = events;
	}

	@Transactional
	public AffiliationResponse register(AffiliationCreateRequest req) {
		Affiliation saved = repository.save(req.toEntity());
		events.publishEvent(new AffiliationCreatedEvent(saved.getId(), saved.getCreatedAt()));
		return AffiliationResponse.from(saved);
	}
}
