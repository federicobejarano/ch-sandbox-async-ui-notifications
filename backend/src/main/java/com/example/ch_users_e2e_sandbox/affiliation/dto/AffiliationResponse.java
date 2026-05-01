package com.example.ch_users_e2e_sandbox.affiliation.dto;

import com.example.ch_users_e2e_sandbox.affiliation.Affiliation;
import java.time.Instant;

public record AffiliationResponse(
		Long id, String fullName, String email, String reason, Instant createdAt) {

	public static AffiliationResponse from(Affiliation entity) {
		return new AffiliationResponse(
				entity.getId(),
				entity.getFullName(),
				entity.getEmail(),
				entity.getReason(),
				entity.getCreatedAt());
	}
}
