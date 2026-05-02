package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationCreateRequest;
import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/affiliations")
public class AffiliationController {

	private final AffiliationService affiliationService;

	public AffiliationController(AffiliationService affiliationService) {
		this.affiliationService = affiliationService;
	}

	@PostMapping
	public ResponseEntity<AffiliationResponse> create(@Valid @RequestBody AffiliationCreateRequest req) {
		AffiliationResponse body = affiliationService.register(req);
		var location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(body.id())
				.toUri();
		return ResponseEntity.created(location).body(body);
	}
}
