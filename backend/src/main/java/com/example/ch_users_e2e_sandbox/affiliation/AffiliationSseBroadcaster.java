package com.example.ch_users_e2e_sandbox.affiliation;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class AffiliationSseBroadcaster {

	private static final Logger log = LoggerFactory.getLogger(AffiliationSseBroadcaster.class);

	private final SseEmitterRegistry registry;
	private final AffiliationQueryService queryService;

	public AffiliationSseBroadcaster(
			SseEmitterRegistry registry, AffiliationQueryService queryService) {
		this.registry = registry;
		this.queryService = queryService;
	}

	@Async
	@EventListener
	public void onAffiliationCreated(AffiliationCreatedEvent event) {
		AffiliationResponse payload = queryService.findById(event.affiliationId());

		for (SseEmitter emitter : registry.snapshot()) {
			try {
				emitter.send(SseEmitter.event()
						.id(String.valueOf(event.affiliationId()))
						.name("affiliation-created")
						.data(payload, MediaType.APPLICATION_JSON));
			}
			catch (IOException ex) {
				log.debug(
						"Failed to fan out affiliation {} to an SSE client; container cleanup will handle disconnect.",
						event.affiliationId(),
						ex);
			}
		}
	}
}
