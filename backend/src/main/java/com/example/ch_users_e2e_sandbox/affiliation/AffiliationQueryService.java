package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliationQueryService {

	private final AffiliationRepository repository;

	public AffiliationQueryService(AffiliationRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<AffiliationResponse> findChangesSince(Instant since) {
		return repository.findByCreatedAtAfterOrderByCreatedAtAsc(since).stream()
				.map(AffiliationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public String computeEtag() {
		long epochMillis = repository
				.findFirstByOrderByCreatedAtDesc()
				.map(Affiliation::getCreatedAt)
				.map(Instant::toEpochMilli)
				.orElse(0L);
		return "W/\"%d\"".formatted(epochMillis);
	}
}
