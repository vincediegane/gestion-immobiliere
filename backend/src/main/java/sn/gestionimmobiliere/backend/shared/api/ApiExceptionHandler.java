package sn.gestionimmobiliere.backend.shared.api;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;

import sn.gestionimmobiliere.backend.owner.application.DuplicateOwnerEmailException;
import sn.gestionimmobiliere.backend.owner.application.OwnerNotFoundException;
import sn.gestionimmobiliere.backend.property.application.PropertyNotFoundException;
import sn.gestionimmobiliere.backend.tenant.application.TenantNotFoundException;
import sn.gestionimmobiliere.backend.unit.application.UnitNotFoundException;
import sn.gestionimmobiliere.backend.billing.application.RentChargeNotFoundException;
import sn.gestionimmobiliere.backend.lease.application.LeaseConflictException;
import sn.gestionimmobiliere.backend.lease.application.LeaseNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(OwnerNotFoundException.class)
	ProblemDetail handleOwnerNotFound(OwnerNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "owner-not-found", "Proprietaire introuvable", exception.getMessage());
	}

	@ExceptionHandler(PropertyNotFoundException.class)
	ProblemDetail handlePropertyNotFound(PropertyNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "property-not-found", "Bien immobilier introuvable",
				exception.getMessage());
	}

	@ExceptionHandler({UnitNotFoundException.class, TenantNotFoundException.class, LeaseNotFoundException.class,
			RentChargeNotFoundException.class})
	ProblemDetail handleReferenceNotFound(RuntimeException exception) {
		return problem(HttpStatus.NOT_FOUND, "resource-not-found", "Ressource introuvable", exception.getMessage());
	}

	@ExceptionHandler({LeaseConflictException.class, IllegalArgumentException.class})
	ProblemDetail handleBusinessConflict(RuntimeException exception) {
		return problem(HttpStatus.CONFLICT, "business-conflict", "Operation impossible", exception.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	ProblemDetail handleBadCredentials() {
		return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Authentification refusee", "Identifiants invalides");
	}

	@ExceptionHandler(DuplicateOwnerEmailException.class)
	ProblemDetail handleDuplicateOwnerEmail(DuplicateOwnerEmailException exception) {
		return problem(HttpStatus.CONFLICT, "owner-email-conflict", "Adresse email deja utilisee",
				exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		ProblemDetail detail = problem(
				HttpStatus.BAD_REQUEST,
				"validation-error",
				"Donnees invalides",
				"Un ou plusieurs champs sont invalides");

		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		detail.setProperty("fieldErrors", fieldErrors);
		return detail;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail handleUnreadableMessage() {
		return problem(
				HttpStatus.BAD_REQUEST,
				"malformed-json",
				"Corps JSON invalide",
				"Le corps de la requete doit contenir un JSON valide encode en UTF-8");
	}

	@ExceptionHandler(InvalidSortPropertyException.class)
	ProblemDetail handleInvalidSortProperty(InvalidSortPropertyException exception) {
		return problem(
				HttpStatus.BAD_REQUEST,
				"invalid-sort-property",
				"Champ de tri invalide",
				exception.getMessage());
	}

	private ProblemDetail problem(HttpStatus status, String code, String title, String message) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
		detail.setTitle(title);
		detail.setType(URI.create("https://gestion-immobiliere.sn/problems/" + code));
		detail.setProperty("code", code);
		return detail;
	}
}
