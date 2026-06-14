package sn.gestionimmobiliere.backend.unit.application;
import java.time.*; import java.util.*;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties; import sn.gestionimmobiliere.backend.property.application.PropertyService;
import sn.gestionimmobiliere.backend.unit.api.*; import sn.gestionimmobiliere.backend.unit.domain.Unit; import sn.gestionimmobiliere.backend.unit.infrastructure.UnitRepository;
@Service
public class UnitService {
	private final UnitRepository repository; private final PropertyService propertyService; private final OwnerProperties properties; private final Clock clock;
	public UnitService(UnitRepository repository, PropertyService propertyService, OwnerProperties properties, Clock clock){this.repository=repository;this.propertyService=propertyService;this.properties=properties;this.clock=clock;}
	@Transactional public UnitResponse create(UUID propertyId, CreateUnitRequest r){ propertyService.findById(propertyId); Instant now=clock.instant(); return map(repository.saveAndFlush(new Unit(UUID.randomUUID(),properties.organizationId(),propertyId,r.name().trim(),r.type(),r.status(),optional(r.description()),r.monthlyRent(),now))); }
	@Transactional(readOnly=true) public Page<UnitResponse> findAll(Pageable p){return repository.findAllByOrganizationIdAndDeletedAtIsNull(properties.organizationId(),p).map(this::map);}
	@Transactional(readOnly=true) public Page<UnitResponse> findByProperty(UUID propertyId,Pageable p){propertyService.findById(propertyId);return repository.findAllByPropertyIdAndOrganizationIdAndDeletedAtIsNull(propertyId,properties.organizationId(),p).map(this::map);}
	@Transactional(readOnly=true) public UnitResponse findById(UUID id){return map(entity(id));}
	@Transactional public UnitResponse update(UUID id,UpdateUnitRequest r){Unit u=entity(id);u.update(r.name().trim(),r.type(),r.status(),optional(r.description()),r.monthlyRent(),clock.instant());return map(repository.saveAndFlush(u));}
	@Transactional public void delete(UUID id){Unit u=entity(id);u.archive(clock.instant());repository.save(u);}
	@Transactional public void occupy(UUID id){Unit u=entity(id);u.occupy(clock.instant());repository.save(u);}
	@Transactional public void release(UUID id){Unit u=entity(id);u.release(clock.instant());repository.save(u);}
	public Unit entity(UUID id){return repository.findByIdAndOrganizationIdAndDeletedAtIsNull(id,properties.organizationId()).orElseThrow(()->new UnitNotFoundException(id));}
	private UnitResponse map(Unit u){return new UnitResponse(u.getId(),u.getPropertyId(),u.getName(),u.getType(),u.getStatus(),u.getDescription(),u.getMonthlyRent(),u.getCreatedAt(),u.getUpdatedAt());}
	private String optional(String s){return s==null||s.isBlank()?null:s.trim();}
}
