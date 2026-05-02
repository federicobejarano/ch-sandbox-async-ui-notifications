package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationCreateRequest;
import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/affiliations")
public class AffiliationController {

	private final AffiliationService affiliationService;
	private final AffiliationQueryService affiliationQueryService;

	public AffiliationController(
			AffiliationService affiliationService, AffiliationQueryService affiliationQueryService) {
		this.affiliationService = affiliationService;
		this.affiliationQueryService = affiliationQueryService;
	}

	@GetMapping
	public ResponseEntity<Page<AffiliationResponse>> list(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
			WebRequest webRequest) {
		String eTag = affiliationQueryService.computeEtag();
		if (webRequest.checkNotModified(eTag)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}
		Page<AffiliationResponse> page = affiliationQueryService.findPage(pageable);
		return ResponseEntity.ok().header(HttpHeaders.ETAG, eTag).body(page);
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
