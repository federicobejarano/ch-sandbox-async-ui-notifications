package com.example.ch_users_e2e_sandbox.affiliation;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class AffiliationSseHeartbeatScheduler {

	private static final Logger log = LoggerFactory.getLogger(AffiliationSseHeartbeatScheduler.class);

	private final SseEmitterRegistry registry;

	public AffiliationSseHeartbeatScheduler(SseEmitterRegistry registry) {
		this.registry = registry;
	}

	@Scheduled(fixedRate = 30_000)
	public void sendHeartbeats() {
		for (SseEmitter emitter : registry.snapshot()) {
			try {
				emitter.send(SseEmitter.event().comment("heartbeat"));
			}
			catch (IOException ex) {
				log.debug(
						"Failed to send SSE heartbeat; container cleanup will handle disconnect.",
						ex);
			}
		}
	}
}
