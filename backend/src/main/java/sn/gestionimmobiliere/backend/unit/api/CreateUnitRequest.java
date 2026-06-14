package sn.gestionimmobiliere.backend.unit.api;
import jakarta.validation.constraints.*;
import sn.gestionimmobiliere.backend.unit.domain.*;
public record CreateUnitRequest(@NotBlank @Size(max=200) String name, @NotNull UnitType type,
		@NotNull UnitStatus status, @Size(max=1000) String description, @PositiveOrZero long monthlyRent) {}
