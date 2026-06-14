package sn.gestionimmobiliere.backend.property.api;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.gestionimmobiliere.backend.property.domain.PropertyStatus;
import sn.gestionimmobiliere.backend.property.domain.PropertyType;

@Schema(description = "Donnees necessaires a la creation d'un bien immobilier")
public record CreatePropertyRequest(
		@NotNull(message = "Le proprietaire est obligatoire")
		UUID ownerId,

		@NotBlank(message = "Le nom du bien est obligatoire")
		@Size(max = 200, message = "Le nom du bien ne doit pas depasser 200 caracteres")
		String name,

		@Size(max = 2000, message = "La description ne doit pas depasser 2000 caracteres")
		String description,

		@NotBlank(message = "L'adresse est obligatoire")
		@Size(max = 500, message = "L'adresse ne doit pas depasser 500 caracteres")
		String address,

		@NotBlank(message = "La ville est obligatoire")
		@Size(max = 120, message = "La ville ne doit pas depasser 120 caracteres")
		String city,

		@NotNull(message = "Le type de bien est obligatoire")
		PropertyType type,

		@NotNull(message = "Le statut du bien est obligatoire")
		PropertyStatus status) {
}
