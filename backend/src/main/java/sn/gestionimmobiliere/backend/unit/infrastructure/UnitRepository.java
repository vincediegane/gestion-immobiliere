package sn.gestionimmobiliere.backend.unit.infrastructure;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.gestionimmobiliere.backend.unit.domain.Unit;
public interface UnitRepository extends JpaRepository<Unit, UUID> {
	Page<Unit> findAllByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);
	Page<Unit> findAllByPropertyIdAndOrganizationIdAndDeletedAtIsNull(UUID propertyId, UUID organizationId, Pageable pageable);
	Optional<Unit> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);
	long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
