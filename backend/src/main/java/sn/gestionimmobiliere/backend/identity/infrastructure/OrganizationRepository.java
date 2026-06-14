package sn.gestionimmobiliere.backend.identity.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sn.gestionimmobiliere.backend.identity.domain.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
	@Query("select o.id from Organization o where o.status = 'ACTIVE'")
	List<UUID> findActiveIds();
}
