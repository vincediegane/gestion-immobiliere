package sn.gestionimmobiliere.backend.unit.api;
import java.net.URI; import java.util.UUID;
import org.springframework.data.domain.*; import org.springframework.data.web.PageableDefault; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid; import sn.gestionimmobiliere.backend.unit.application.UnitService;
@RestController @Tag(name="Units")
public class UnitController {
	private final UnitService service; public UnitController(UnitService service){this.service=service;}
	@PostMapping("/api/properties/{propertyId}/units") public ResponseEntity<UnitResponse> create(@PathVariable UUID propertyId,@Valid @RequestBody CreateUnitRequest r){UnitResponse u=service.create(propertyId,r);return ResponseEntity.created(URI.create("/api/units/"+u.id())).body(u);}
	@GetMapping("/api/units") public Page<UnitResponse> all(@PageableDefault(size=20,sort="name") Pageable p){return service.findAll(p);}
	@GetMapping("/api/properties/{propertyId}/units") public Page<UnitResponse> byProperty(@PathVariable UUID propertyId,@PageableDefault(size=20,sort="name") Pageable p){return service.findByProperty(propertyId,p);}
	@GetMapping("/api/units/{id}") public UnitResponse one(@PathVariable UUID id){return service.findById(id);}
	@PutMapping("/api/units/{id}") public UnitResponse update(@PathVariable UUID id,@Valid @RequestBody UpdateUnitRequest r){return service.update(id,r);}
	@DeleteMapping("/api/units/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id){service.delete(id);return ResponseEntity.noContent().build();}
}
