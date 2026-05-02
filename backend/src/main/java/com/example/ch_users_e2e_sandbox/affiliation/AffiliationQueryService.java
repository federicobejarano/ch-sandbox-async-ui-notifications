package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliationQueryService {

	private final AffiliationRepository repository;

	public AffiliationQueryService(AffiliationRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public Page<AffiliationResponse> findPage(Pageable pageable) {
		return repository.findAll(pageable).map(AffiliationResponse::from);
	}

	@Transactional(readOnly = true)
	public List<AffiliationResponse> findAfterId(Long id) {
		return repository.findByIdGreaterThanOrderByIdAsc(id).stream()
				.map(AffiliationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AffiliationResponse findById(Long id) {
		return repository.findById(id)
				.map(AffiliationResponse::from)
				.orElseThrow(() -> new NoSuchElementException("Affiliation %d was not found.".formatted(id)));
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
