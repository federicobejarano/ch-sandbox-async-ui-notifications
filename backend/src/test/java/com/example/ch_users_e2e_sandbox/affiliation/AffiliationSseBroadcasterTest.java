package com.example.ch_users_e2e_sandbox.affiliation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class AffiliationSseBroadcasterTest {

	@Test
	void onAffiliationCreatedFansOutPayloadToEveryRegisteredEmitter() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		AffiliationQueryService queryService = mock(AffiliationQueryService.class);
		RecordingSseEmitter first = new RecordingSseEmitter();
		RecordingSseEmitter second = new RecordingSseEmitter();
		AffiliationResponse payload = new AffiliationResponse(
				7L,
				"Hedy Lamarr",
				"hedy@example.com",
				"Broadcast test",
				Instant.parse("2026-05-02T10:00:00Z"));
		when(queryService.findById(7L)).thenReturn(payload);
		registry.add(first);
		registry.add(second);
		AffiliationSseBroadcaster broadcaster = new AffiliationSseBroadcaster(registry, queryService);

		broadcaster.onAffiliationCreated(new AffiliationCreatedEvent(7L, payload.createdAt()));

		verify(queryService).findById(7L);
		assertThat(first.sendAttempts()).isEqualTo(1);
		assertThat(second.sendAttempts()).isEqualTo(1);
	}

	@Test
	void onAffiliationCreatedLogsAndContinuesWhenOneEmitterFails() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		AffiliationQueryService queryService = mock(AffiliationQueryService.class);
		RecordingSseEmitter failing = new RecordingSseEmitter(true);
		RecordingSseEmitter healthy = new RecordingSseEmitter();
		AffiliationResponse payload = new AffiliationResponse(
				8L,
				"Katherine Johnson",
				"katherine@example.com",
				"IOException tolerance",
				Instant.parse("2026-05-02T10:01:00Z"));
		when(queryService.findById(8L)).thenReturn(payload);
		registry.add(failing);
		registry.add(healthy);
		AffiliationSseBroadcaster broadcaster = new AffiliationSseBroadcaster(registry, queryService);

		broadcaster.onAffiliationCreated(new AffiliationCreatedEvent(8L, payload.createdAt()));

		assertThat(failing.sendAttempts()).isEqualTo(1);
		assertThat(failing.completeCalls()).isZero();
		assertThat(failing.completeWithErrorCalls()).isZero();
		assertThat(healthy.sendAttempts()).isEqualTo(1);
	}

	private static final class RecordingSseEmitter extends SseEmitter {

		private final boolean failOnSend;
		private int sendAttempts;
		private int completeCalls;
		private int completeWithErrorCalls;

		private RecordingSseEmitter() {
			this(false);
		}

		private RecordingSseEmitter(boolean failOnSend) {
			this.failOnSend = failOnSend;
		}

		@Override
		public synchronized void send(SseEventBuilder builder) throws IOException {
			sendAttempts++;
			if (failOnSend) {
				throw new IOException("broken pipe");
			}
		}

		@Override
		public synchronized void complete() {
			completeCalls++;
		}

		@Override
		public synchronized void completeWithError(Throwable ex) {
			completeWithErrorCalls++;
		}

		int sendAttempts() {
			return sendAttempts;
		}

		int completeCalls() {
			return completeCalls;
		}

		int completeWithErrorCalls() {
			return completeWithErrorCalls;
		}
	}
}
