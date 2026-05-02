package com.example.ch_users_e2e_sandbox.affiliation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRegistryTest {

	@Test
	void snapshotReflectsRegisteredEmittersAndIsImmutable() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		SseEmitter first = new SseEmitter();
		SseEmitter second = new SseEmitter();

		registry.add(first);
		registry.add(second);

		List<SseEmitter> snapshot = registry.snapshot();

		assertThat(snapshot).containsExactly(first, second);
		assertThatThrownBy(() -> snapshot.add(new SseEmitter()))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void removeExcludesEmitterFromFutureSnapshots() {
		SseEmitterRegistry registry = new SseEmitterRegistry();
		SseEmitter first = new SseEmitter();
		SseEmitter second = new SseEmitter();

		registry.add(first);
		registry.add(second);
		registry.remove(first);

		assertThat(registry.snapshot()).containsExactly(second);
	}
}
