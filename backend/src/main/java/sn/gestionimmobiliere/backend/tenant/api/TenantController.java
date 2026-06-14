package sn.gestionimmobiliere.backend.tenant.api;
import java.net.URI;import java.util.UUID;import org.springframework.data.domain.*;import org.springframework.data.web.PageableDefault;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import io.swagger.v3.oas.annotations.tags.Tag;import jakarta.validation.Valid;import sn.gestionimmobiliere.backend.tenant.application.TenantService;
@RestController @RequestMapping("/api/tenants") @Tag(name="Tenants") public class TenantController{
	private final TenantService service;public TenantController(TenantService s){service=s;}
	@PostMapping public ResponseEntity<TenantResponse> create(@Valid @RequestBody CreateTenantRequest r){TenantResponse t=service.create(r);return ResponseEntity.created(URI.create("/api/tenants/"+t.id())).body(t);}
	@GetMapping public Page<TenantResponse> all(@PageableDefault(size=20,sort="fullName") Pageable p){return service.all(p);}
	@GetMapping("/{id}") public TenantResponse one(@PathVariable UUID id){return service.one(id);}
	@PutMapping("/{id}") public TenantResponse update(@PathVariable UUID id,@Valid @RequestBody UpdateTenantRequest r){return service.update(id,r);}
	@DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id){service.delete(id);return ResponseEntity.noContent().build();}
}
