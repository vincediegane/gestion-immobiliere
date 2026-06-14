package sn.gestionimmobiliere.backend.owner.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOwnerRequest(
		@NotBlank(message = "Le nom complet est obligatoire")
		@Size(max = 200, message = "Le nom complet ne doit pas depasser 200 caracteres")
		String fullName,

		@NotBlank(message = "Le telephone est obligatoire")
		@Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le telephone doit etre au format E.164")
		String phone,

		@Email(message = "L'adresse email n'est pas valide")
		@Size(max = 320, message = "L'adresse email ne doit pas depasser 320 caracteres")
		String email,

		@Size(max = 500, message = "L'adresse ne doit pas depasser 500 caracteres")
		String address) {
}
