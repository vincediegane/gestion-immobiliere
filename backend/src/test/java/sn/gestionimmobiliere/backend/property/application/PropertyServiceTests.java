package sn.gestionimmobiliere.backend.property.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import sn.gestionimmobiliere.backend.owner.application.OwnerNotFoundException;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;
import sn.gestionimmobiliere.backend.owner.application.OwnerService;
import sn.gestionimmobiliere.backend.property.api.CreatePropertyRequest;
import sn.gestionimmobiliere.backend.property.api.UpdatePropertyRequest;
import sn.gestionimmobiliere.backend.property.domain.Property;
import sn.gestionimmobiliere.backend.property.domain.PropertyStatus;
import sn.gestionimmobiliere.backend.property.domain.PropertyType;
import sn.gestionimmobiliere.backend.property.infrastructure.PropertyRepository;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTests {

	private static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PROPERTY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final Instant NOW = Instant.parse("2026-06-14T10:15:30Z");

	@Mock
	private PropertyRepository propertyRepository;

	@Mock
	private OwnerService ownerService;

	private PropertyService propertyService;

	@BeforeEach
	void setUp() {
		propertyService = new PropertyService(
				propertyRepository,
				ownerService,
				new OwnerProperties(ORGANIZATION_ID),
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void createsAndNormalizesPropertyForExistingOwner() {
		CreatePropertyRequest request = new CreatePropertyRequest(
				OWNER_ID, "  Residence Teranga  ", "  Immeuble R+4  ", "  Almadies  ", "  Dakar  ",
				PropertyType.APARTMENT_BUILDING, PropertyStatus.AVAILABLE);
		when(propertyRepository.saveAndFlush(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = propertyService.create(request);

		verify(ownerService).findById(OWNER_ID);
		assertThat(response.ownerId()).isEqualTo(OWNER_ID);
		assertThat(response.name()).isEqualTo("Residence Teranga");
		assertThat(response.description()).isEqualTo("Immeuble R+4");
		assertThat(response.address()).isEqualTo("Almadies");
		assertThat(response.city()).isEqualTo("Dakar");
		assertThat(response.createdAt()).isEqualTo(NOW);
	}

	@Test
	void rejectsPropertyWhenOwnerDoesNotExist() {
		CreatePropertyRequest request = createRequest();
		when(ownerService.findById(OWNER_ID)).thenThrow(new OwnerNotFoundException(OWNER_ID));

		assertThatThrownBy(() -> propertyService.create(request))
				.isInstanceOf(OwnerNotFoundException.class);
		verify(propertyRepository, never()).saveAndFlush(any(Property.class));
	}

	@Test
	void throwsWhenPropertyDoesNotExist() {
		when(propertyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(PROPERTY_ID, ORGANIZATION_ID))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> propertyService.findById(PROPERTY_ID))
				.isInstanceOf(PropertyNotFoundException.class)
				.hasMessageContaining(PROPERTY_ID.toString());
	}

	@Test
	void updatesPropertyAndChecksNewOwner() {
		UUID newOwnerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		Property property = property();
		when(propertyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(PROPERTY_ID, ORGANIZATION_ID))
				.thenReturn(Optional.of(property));
		when(propertyRepository.saveAndFlush(property)).thenReturn(property);
		UpdatePropertyRequest request = new UpdatePropertyRequest(
				newOwnerId, "Villa Ngor", null, "Route de Ngor", "Dakar", PropertyType.VILLA,
				PropertyStatus.OCCUPIED);

		var response = propertyService.update(PROPERTY_ID, request);

		verify(ownerService).findById(newOwnerId);
		assertThat(response.ownerId()).isEqualTo(newOwnerId);
		assertThat(response.type()).isEqualTo(PropertyType.VILLA);
		assertThat(response.status()).isEqualTo(PropertyStatus.OCCUPIED);
		assertThat(response.updatedAt()).isEqualTo(NOW);
	}

	@Test
	void validatesOwnerBeforeListingTheirProperties() {
		var pageable = PageRequest.of(0, 20);
		when(ownerService.findById(OWNER_ID)).thenThrow(new OwnerNotFoundException(OWNER_ID));

		assertThatThrownBy(() -> propertyService.findByOwnerId(OWNER_ID, pageable))
				.isInstanceOf(OwnerNotFoundException.class);
		verify(propertyRepository, never())
				.findAllByOwnerIdAndOrganizationIdAndDeletedAtIsNull(any(), any(), any());
	}

	@Test
	void softDeletesProperty() {
		Property property = property();
		when(propertyRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(PROPERTY_ID, ORGANIZATION_ID))
				.thenReturn(Optional.of(property));

		propertyService.delete(PROPERTY_ID);

		ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
		verify(propertyRepository).save(captor.capture());
		assertThat(captor.getValue().getDeletedAt()).isEqualTo(NOW);
		assertThat(captor.getValue().getUpdatedAt()).isEqualTo(NOW);
	}

	private CreatePropertyRequest createRequest() {
		return new CreatePropertyRequest(
				OWNER_ID, "Residence Teranga", null, "Almadies", "Dakar",
				PropertyType.APARTMENT_BUILDING, PropertyStatus.AVAILABLE);
	}

	private Property property() {
		return Property.create(
				PROPERTY_ID, ORGANIZATION_ID, OWNER_ID, "Residence Teranga", null, "Almadies", "Dakar",
				PropertyType.APARTMENT_BUILDING, PropertyStatus.AVAILABLE, NOW.minusSeconds(60));
	}
}
