package sn.gestionimmobiliere.backend.property.api;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sn.gestionimmobiliere.backend.property.application.PropertyService;
import sn.gestionimmobiliere.backend.shared.api.SortValidator;

@Validated
@RestController
@Tag(name = "Properties", description = "Gestion des biens immobiliers")
public class PropertyController {
	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
			"id", "ownerId", "name", "address", "city", "type", "status", "createdAt", "updatedAt");

	private final PropertyService propertyService;

	public PropertyController(PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	@PostMapping("/api/properties")
	@Operation(summary = "Creer un bien immobilier")
	public ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request) {
		PropertyResponse property = propertyService.create(request);
		return ResponseEntity.created(URI.create("/api/properties/" + property.id())).body(property);
	}

	@GetMapping("/api/properties")
	@Operation(summary = "Lister les biens immobiliers")
	@Parameters({
			@Parameter(name = "page", in = ParameterIn.QUERY, description = "Numero de page, commence a 0",
					schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")),
			@Parameter(name = "size", in = ParameterIn.QUERY, description = "Nombre d'elements par page",
					schema = @Schema(type = "integer", defaultValue = "20", minimum = "1")),
			@Parameter(name = "sort", in = ParameterIn.QUERY,
					description = "Tri au format champ,direction. Exemple: name,asc",
					example = "name,asc")
	})
	public Page<PropertyResponse> findAll(
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name") Pageable pageable) {
		SortValidator.validate(pageable, ALLOWED_SORT_PROPERTIES);
		return propertyService.findAll(pageable);
	}

	@GetMapping("/api/properties/{id}")
	@Operation(summary = "Consulter un bien immobilier")
	public PropertyResponse findById(@PathVariable UUID id) {
		return propertyService.findById(id);
	}

	@PutMapping("/api/properties/{id}")
	@Operation(summary = "Modifier un bien immobilier")
	public PropertyResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePropertyRequest request) {
		return propertyService.update(id, request);
	}

	@DeleteMapping("/api/properties/{id}")
	@Operation(summary = "Supprimer un bien immobilier")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		propertyService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/api/owners/{ownerId}/properties")
	@Operation(summary = "Lister les biens d'un proprietaire")
	@Parameters({
			@Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
			@Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
			@Parameter(name = "sort", in = ParameterIn.QUERY,
					description = "Tri au format champ,direction. Exemple: name,asc", example = "name,asc")
	})
	public Page<PropertyResponse> findByOwnerId(
			@PathVariable UUID ownerId,
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name") Pageable pageable) {
		SortValidator.validate(pageable, ALLOWED_SORT_PROPERTIES);
		return propertyService.findByOwnerId(ownerId, pageable);
	}
}
