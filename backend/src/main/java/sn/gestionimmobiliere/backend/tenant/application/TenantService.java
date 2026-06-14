package sn.gestionimmobiliere.backend.tenant.application;
import java.time.*; import java.util.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties; import sn.gestionimmobiliere.backend.tenant.api.*; import sn.gestionimmobiliere.backend.tenant.domain.Tenant; import sn.gestionimmobiliere.backend.tenant.infrastructure.TenantRepository;
@Service public class TenantService{
	private final TenantRepository repository;private final OwnerProperties properties;private final Clock clock;public TenantService(TenantRepository r,OwnerProperties p,Clock c){repository=r;properties=p;clock=c;}
	@Transactional public TenantResponse create(CreateTenantRequest r){Instant n=clock.instant();return map(repository.saveAndFlush(new Tenant(UUID.randomUUID(),properties.organizationId(),r.fullName().trim(),r.phone().trim(),opt(r.email()),opt(r.address()),opt(r.identityReference()),n)));}
	@Transactional(readOnly=true) public Page<TenantResponse> all(Pageable p){return repository.findAllByOrganizationIdAndDeletedAtIsNull(properties.organizationId(),p).map(this::map);}
	@Transactional(readOnly=true) public TenantResponse one(UUID id){return map(entity(id));}
	@Transactional public TenantResponse update(UUID id,UpdateTenantRequest r){Tenant t=entity(id);t.update(r.fullName().trim(),r.phone().trim(),opt(r.email()),opt(r.address()),opt(r.identityReference()),clock.instant());return map(repository.saveAndFlush(t));}
	@Transactional public void delete(UUID id){Tenant t=entity(id);t.archive(clock.instant());repository.save(t);}
	public Tenant entity(UUID id){return repository.findByIdAndOrganizationIdAndDeletedAtIsNull(id,properties.organizationId()).orElseThrow(()->new TenantNotFoundException(id));}
	private TenantResponse map(Tenant t){return new TenantResponse(t.getId(),t.getFullName(),t.getPhone(),t.getEmail(),t.getAddress(),t.getIdentityReference(),t.getCreatedAt(),t.getUpdatedAt());}
	private String opt(String s){return s==null||s.isBlank()?null:s.trim();}
}
