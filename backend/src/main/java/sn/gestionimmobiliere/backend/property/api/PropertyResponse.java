package sn.gestionimmobiliere.backend.property.api;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import sn.gestionimmobiliere.backend.property.domain.PropertyStatus;
import sn.gestionimmobiliere.backend.property.domain.PropertyType;

@Schema(description = "Representation d'un bien immobilier")
public record PropertyResponse(
		UUID id,
		UUID ownerId,
		String name,
		String description,
		String address,
		String city,
		PropertyType type,
		PropertyStatus status,
		Instant createdAt,
		Instant updatedAt) {
}
