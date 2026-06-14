package sn.gestionimmobiliere.backend.tenant.infrastructure;
import java.util.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import sn.gestionimmobiliere.backend.tenant.domain.Tenant;
public interface TenantRepository extends JpaRepository<Tenant,UUID>{Page<Tenant> findAllByOrganizationIdAndDeletedAtIsNull(UUID organizationId,Pageable p);Optional<Tenant> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id,UUID organizationId);long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);}
