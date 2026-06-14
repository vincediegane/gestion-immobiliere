package sn.gestionimmobiliere.backend.unit.api;
import java.time.Instant; import java.util.UUID; import sn.gestionimmobiliere.backend.unit.domain.*;
public record UnitResponse(UUID id, UUID propertyId, String name, UnitType type, UnitStatus status,
		String description, long monthlyRent, Instant createdAt, Instant updatedAt) {}
