package sn.gestionimmobiliere.backend.owner.api;

import java.time.Instant;
import java.util.UUID;

public record OwnerResponse(
		UUID id,
		String fullName,
		String phone,
		String email,
		String address,
		Instant createdAt,
		Instant updatedAt) {
}
