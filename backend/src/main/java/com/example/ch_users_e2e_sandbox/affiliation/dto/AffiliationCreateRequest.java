package com.example.ch_users_e2e_sandbox.affiliation.dto;

import com.example.ch_users_e2e_sandbox.affiliation.Affiliation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AffiliationCreateRequest(
		@NotBlank @Size(max = 100) String fullName,
		@Email String email,
		@Size(max = 500) String reason) {

	public Affiliation toEntity() {
		return new Affiliation(fullName, email, reason);
	}
}
