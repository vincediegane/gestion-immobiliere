package sn.gestionimmobiliere.backend.property.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sn.gestionimmobiliere.backend.property.domain.Property;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

	Page<Property> findAllByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

	Page<Property> findAllByOwnerIdAndOrganizationIdAndDeletedAtIsNull(UUID ownerId, UUID organizationId,
			Pageable pageable);

	Optional<Property> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

	long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
