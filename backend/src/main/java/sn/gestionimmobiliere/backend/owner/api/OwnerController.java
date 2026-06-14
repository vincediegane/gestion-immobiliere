package sn.gestionimmobiliere.backend.owner.api;

import java.net.URI;
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

import jakarta.validation.Valid;
import sn.gestionimmobiliere.backend.owner.application.OwnerService;

@Validated
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

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
	public Page<OwnerResponse> findAll(@PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
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
