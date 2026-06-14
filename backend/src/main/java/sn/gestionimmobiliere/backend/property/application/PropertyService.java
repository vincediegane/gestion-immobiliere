package sn.gestionimmobiliere.backend.property.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;
import sn.gestionimmobiliere.backend.owner.application.OwnerService;
import sn.gestionimmobiliere.backend.property.api.CreatePropertyRequest;
import sn.gestionimmobiliere.backend.property.api.PropertyResponse;
import sn.gestionimmobiliere.backend.property.api.UpdatePropertyRequest;
import sn.gestionimmobiliere.backend.property.domain.Property;
import sn.gestionimmobiliere.backend.property.infrastructure.PropertyRepository;

@Service
public class PropertyService {

	private final PropertyRepository propertyRepository;
	private final OwnerService ownerService;
	private final OwnerProperties ownerProperties;
	private final Clock clock;

	public PropertyService(PropertyRepository propertyRepository, OwnerService ownerService,
			OwnerProperties ownerProperties, Clock clock) {
		this.propertyRepository = propertyRepository;
		this.ownerService = ownerService;
		this.ownerProperties = ownerProperties;
		this.clock = clock;
	}

	@Transactional
	public PropertyResponse create(CreatePropertyRequest request) {
		ownerService.findById(request.ownerId());
		Instant now = clock.instant();
		Property property = Property.create(
				UUID.randomUUID(),
				ownerProperties.organizationId(),
				request.ownerId(),
				normalizeRequired(request.name()),
				normalizeOptional(request.description()),
				normalizeRequired(request.address()),
				normalizeRequired(request.city()),
				request.type(),
				request.status(),
				now);

		return toResponse(propertyRepository.saveAndFlush(property));
	}

	@Transactional(readOnly = true)
	public Page<PropertyResponse> findAll(Pageable pageable) {
		return propertyRepository
				.findAllByOrganizationIdAndDeletedAtIsNull(ownerProperties.organizationId(), pageable)
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public PropertyResponse findById(UUID id) {
		return toResponse(findActiveProperty(id));
	}

	@Transactional(readOnly = true)
	public Page<PropertyResponse> findByOwnerId(UUID ownerId, Pageable pageable) {
		ownerService.findById(ownerId);
		return propertyRepository
				.findAllByOwnerIdAndOrganizationIdAndDeletedAtIsNull(
						ownerId, ownerProperties.organizationId(), pageable)
				.map(this::toResponse);
	}

	@Transactional
	public PropertyResponse update(UUID id, UpdatePropertyRequest request) {
		Property property = findActiveProperty(id);
		ownerService.findById(request.ownerId());
		property.update(
				request.ownerId(),
				normalizeRequired(request.name()),
				normalizeOptional(request.description()),
				normalizeRequired(request.address()),
				normalizeRequired(request.city()),
				request.type(),
				request.status(),
				clock.instant());

		return toResponse(propertyRepository.saveAndFlush(property));
	}

	@Transactional
	public void delete(UUID id) {
		Property property = findActiveProperty(id);
		property.archive(clock.instant());
		propertyRepository.save(property);
	}

	private Property findActiveProperty(UUID id) {
		return propertyRepository
				.findByIdAndOrganizationIdAndDeletedAtIsNull(id, ownerProperties.organizationId())
				.orElseThrow(() -> new PropertyNotFoundException(id));
	}

	private PropertyResponse toResponse(Property property) {
		return new PropertyResponse(
				property.getId(),
				property.getOwnerId(),
				property.getName(),
				property.getDescription(),
				property.getAddress(),
				property.getCity(),
				property.getType(),
				property.getStatus(),
				property.getCreatedAt(),
				property.getUpdatedAt());
	}

	private String normalizeRequired(String value) {
		return value.trim();
	}

	private String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
