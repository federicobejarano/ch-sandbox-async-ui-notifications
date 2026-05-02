package com.example.ch_users_e2e_sandbox.affiliation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ch_users_e2e_sandbox.affiliation.dto.AffiliationResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class AffiliationSseControllerTest {

	@Test
	void subscribeRegistersEmitterAndCleansItUpOnCompletion() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		AffiliationQueryService queryService = mock(AffiliationQueryService.class);
		RecordingSseEmitter emitter = new RecordingSseEmitter();
		AffiliationSseController controller =
				new TestAffiliationSseController(registry, queryService, emitter);

		SseEmitter returned = controller.subscribe(null);

		assertThat(returned).isSameAs(emitter);
		assertThat(registry.snapshot()).containsExactly(emitter);

		emitter.triggerCompletion();

		assertThat(registry.snapshot()).isEmpty();
	}

	@Test
	void subscribeRemovesEmitterOnErrorCallback() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		AffiliationQueryService queryService = mock(AffiliationQueryService.class);
		RecordingSseEmitter emitter = new RecordingSseEmitter();
		AffiliationSseController controller =
				new TestAffiliationSseController(registry, queryService, emitter);

		controller.subscribe(null);

		emitter.triggerError(new IOException("broken pipe"));

		assertThat(registry.snapshot()).isEmpty();
	}

	@Test
	void subscribeReplaysEventsAfterLastReceivedId() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		AffiliationQueryService queryService = mock(AffiliationQueryService.class);
		RecordingSseEmitter emitter = new RecordingSseEmitter();
		AffiliationSseController controller =
				new TestAffiliationSseController(registry, queryService, emitter);
		AffiliationResponse first = new AffiliationResponse(
				42L, "Ada Lovelace", "ada@example.com", "Replay 1", Instant.parse("2026-05-02T10:00:00Z"));
		AffiliationResponse second = new AffiliationResponse(
				43L, "Grace Hopper", "grace@example.com", "Replay 2", Instant.parse("2026-05-02T10:01:00Z"));
		when(queryService.findAfterId(41L)).thenReturn(List.of(first, second));

		controller.subscribe("41");

		verify(queryService).findAfterId(41L);
		assertThat(emitter.sentEventsCount()).isEqualTo(2);
	}

	private static final class TestAffiliationSseController extends AffiliationSseController {

		private final SseEmitter emitter;

		private TestAffiliationSseController(
				SseEmitterRegistry registry, AffiliationQueryService queryService, SseEmitter emitter) {
			super(registry, queryService);
			this.emitter = emitter;
		}

		@Override
		SseEmitter createEmitter() {
			return emitter;
		}
	}

	private static final class RecordingSseEmitter extends SseEmitter {

		private Runnable completionCallback;
		private Runnable timeoutCallback;
		private Consumer<Throwable> errorCallback;
		private int sentEventsCount;

		@Override
		public synchronized void send(SseEventBuilder builder) throws IOException {
			sentEventsCount++;
		}

		@Override
		public void onCompletion(Runnable callback) {
			this.completionCallback = callback;
		}

		@Override
		public void onTimeout(Runnable callback) {
			this.timeoutCallback = callback;
		}

		@Override
		public void onError(Consumer<Throwable> callback) {
			this.errorCallback = callback;
		}

		int sentEventsCount() {
			return sentEventsCount;
		}

		void triggerCompletion() {
			assertThat(completionCallback).isNotNull();
			completionCallback.run();
		}

		void triggerTimeout() {
			assertThat(timeoutCallback).isNotNull();
			timeoutCallback.run();
		}

		void triggerError(Throwable error) {
			assertThat(errorCallback).isNotNull();
			errorCallback.accept(error);
		}
	}
}
