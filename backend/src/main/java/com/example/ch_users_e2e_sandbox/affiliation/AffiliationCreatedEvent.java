package com.example.ch_users_e2e_sandbox.affiliation;

import java.time.Instant;

public record AffiliationCreatedEvent(Long affiliationId, Instant occurredAt) {}
