package sn.gestionimmobiliere.backend.owner.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.gestionimmobiliere.backend.owner.api.CreateOwnerRequest;
import sn.gestionimmobiliere.backend.owner.api.OwnerResponse;
import sn.gestionimmobiliere.backend.owner.api.UpdateOwnerRequest;
import sn.gestionimmobiliere.backend.owner.domain.Owner;
import sn.gestionimmobiliere.backend.owner.infrastructure.OwnerRepository;

@Service
public class OwnerService {

	private final OwnerRepository ownerRepository;
	private final OwnerProperties ownerProperties;
	private final Clock clock;

	public OwnerService(OwnerRepository ownerRepository, OwnerProperties ownerProperties, Clock clock) {
		this.ownerRepository = ownerRepository;
		this.ownerProperties = ownerProperties;
		this.clock = clock;
	}

	@Transactional
	public OwnerResponse create(CreateOwnerRequest request) {
		UUID organizationId = ownerProperties.organizationId();
		String email = normalizeOptionalEmail(request.email());
		ensureEmailAvailable(organizationId, email);

		Instant now = clock.instant();
		Owner owner = Owner.create(
				UUID.randomUUID(),
				organizationId,
				normalizeRequired(request.fullName()),
				normalizeRequired(request.phone()),
				email,
				normalizeOptional(request.address()),
				now);

		return toResponse(save(owner, email));
	}

	@Transactional(readOnly = true)
	public Page<OwnerResponse> findAll(Pageable pageable) {
		return ownerRepository.findAllByOrganizationIdAndDeletedAtIsNull(ownerProperties.organizationId(), pageable)
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public OwnerResponse findById(UUID id) {
		return toResponse(findActiveOwner(id));
	}

	@Transactional
	public OwnerResponse update(UUID id, UpdateOwnerRequest request) {
		Owner owner = findActiveOwner(id);
		String email = normalizeOptionalEmail(request.email());

		if (email != null && ownerRepository.existsByOrganizationIdAndEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(
				ownerProperties.organizationId(), email, id)) {
			throw new DuplicateOwnerEmailException(email);
		}

		owner.update(
				normalizeRequired(request.fullName()),
				normalizeRequired(request.phone()),
				email,
				normalizeOptional(request.address()),
				clock.instant());

		return toResponse(save(owner, email));
	}

	@Transactional
	public void delete(UUID id) {
		Owner owner = findActiveOwner(id);
		owner.archive(clock.instant());
		ownerRepository.save(owner);
	}

	private Owner findActiveOwner(UUID id) {
		return ownerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, ownerProperties.organizationId())
				.orElseThrow(() -> new OwnerNotFoundException(id));
	}

	private void ensureEmailAvailable(UUID organizationId, String email) {
		if (email != null && ownerRepository.existsByOrganizationIdAndEmailIgnoreCaseAndDeletedAtIsNull(
				organizationId, email)) {
			throw new DuplicateOwnerEmailException(email);
		}
	}

	private Owner save(Owner owner, String email) {
		try {
			return ownerRepository.saveAndFlush(owner);
		} catch (DataIntegrityViolationException exception) {
			if (email != null) {
				throw new DuplicateOwnerEmailException(email);
			}
			throw exception;
		}
	}

	private OwnerResponse toResponse(Owner owner) {
		return new OwnerResponse(
				owner.getId(),
				owner.getFullName(),
				owner.getPhone(),
				owner.getEmail(),
				owner.getAddress(),
				owner.getCreatedAt(),
				owner.getUpdatedAt());
	}

	private String normalizeRequired(String value) {
		return value.trim();
	}

	private String normalizeOptionalEmail(String value) {
		String normalized = normalizeOptional(value);
		return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
	}

	private String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
