package sn.gestionimmobiliere.backend.owner.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import sn.gestionimmobiliere.backend.owner.domain.Owner;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {

	Page<Owner> findAllByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

	Optional<Owner> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

	boolean existsByOrganizationIdAndEmailIgnoreCaseAndDeletedAtIsNull(UUID organizationId, String email);

	boolean existsByOrganizationIdAndEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID organizationId, String email,
			UUID id);
}
