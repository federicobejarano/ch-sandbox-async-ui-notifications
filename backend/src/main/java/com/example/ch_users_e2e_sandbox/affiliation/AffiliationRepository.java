package com.example.ch_users_e2e_sandbox.affiliation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {

	List<Affiliation> findByIdGreaterThanOrderByIdAsc(Long id);

	List<Affiliation> findByCreatedAtAfterOrderByCreatedAtAsc(Instant since);

	Optional<Affiliation> findFirstByOrderByCreatedAtDesc();

	long countByCreatedAtAfter(Instant since);
}
