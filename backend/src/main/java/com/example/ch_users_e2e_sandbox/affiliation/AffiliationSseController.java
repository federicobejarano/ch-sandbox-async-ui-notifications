package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/affiliations")
public class AffiliationSseController {

	private static final Logger log = LoggerFactory.getLogger(AffiliationSseController.class);

	private static final long EMITTER_TIMEOUT_MILLIS = Duration.ofMinutes(5).toMillis();

	private final SseEmitterRegistry registry;
	private final AffiliationQueryService queryService;

	public AffiliationSseController(
			SseEmitterRegistry registry, AffiliationQueryService queryService) {
		this.registry = registry;
		this.queryService = queryService;
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
			@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
		SseEmitter emitter = createEmitter();
		registry.add(emitter);

		emitter.onCompletion(() -> registry.remove(emitter));
		emitter.onTimeout(() -> {
			emitter.complete();
			registry.remove(emitter);
		});
		emitter.onError(ex -> registry.remove(emitter));

		if (lastEventId != null) {
			queryService.findAfterId(Long.parseLong(lastEventId))
					.forEach(affiliation -> trySend(emitter, affiliation));
		}

		return emitter;
	}

	SseEmitter createEmitter() {
		return new SseEmitter(EMITTER_TIMEOUT_MILLIS);
	}

	private void trySend(SseEmitter emitter, AffiliationResponse affiliation) {
		try {
			emitter.send(SseEmitter.event()
					.id(affiliation.id().toString())
					.name("affiliation-created")
					.data(affiliation));
		}
		catch (IOException ex) {
			log.debug("Failed to replay affiliation {} to SSE client", affiliation.id(), ex);
		}
	}
}
