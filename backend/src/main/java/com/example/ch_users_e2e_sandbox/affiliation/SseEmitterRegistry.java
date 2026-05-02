package com.example.ch_users_e2e_sandbox.affiliation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {

	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public void add(SseEmitter emitter) {
		emitters.add(emitter);
	}

	public void remove(SseEmitter emitter) {
		emitters.remove(emitter);
	}

	public List<SseEmitter> snapshot() {
		return List.copyOf(emitters);
	}
}
