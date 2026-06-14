package sn.gestionimmobiliere.backend.owner.api;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import sn.gestionimmobiliere.backend.owner.application.OwnerService;
import sn.gestionimmobiliere.backend.shared.api.SortValidator;

@Validated
@RestController
@RequestMapping("/api/owners")
public class OwnerController {
	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
			"id", "fullName", "phone", "email", "address", "createdAt", "updatedAt");

	private final OwnerService ownerService;

	public OwnerController(OwnerService ownerService) {
		this.ownerService = ownerService;
	}

	@PostMapping
	public ResponseEntity<OwnerResponse> create(@Valid @RequestBody CreateOwnerRequest request) {
		OwnerResponse owner = ownerService.create(request);
		return ResponseEntity.created(URI.create("/api/owners/" + owner.id())).body(owner);
	}

	@GetMapping
	@Operation(summary = "Lister les proprietaires")
	@Parameters({
			@Parameter(name = "page", in = ParameterIn.QUERY, description = "Numero de page, commence a 0",
					schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")),
			@Parameter(name = "size", in = ParameterIn.QUERY, description = "Nombre d'elements par page",
					schema = @Schema(type = "integer", defaultValue = "20", minimum = "1")),
			@Parameter(name = "sort", in = ParameterIn.QUERY,
					description = "Tri au format champ,direction. Champs: id, fullName, phone, email, address, createdAt, updatedAt",
					example = "fullName,asc")
	})
	public Page<OwnerResponse> findAll(
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
		SortValidator.validate(pageable, ALLOWED_SORT_PROPERTIES);
		return ownerService.findAll(pageable);
	}

	@GetMapping("/{id}")
	public OwnerResponse findById(@PathVariable UUID id) {
		return ownerService.findById(id);
	}

	@PutMapping("/{id}")
	public OwnerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateOwnerRequest request) {
		return ownerService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		ownerService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
