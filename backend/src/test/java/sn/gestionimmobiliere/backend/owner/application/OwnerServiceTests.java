package sn.gestionimmobiliere.backend.owner.application;

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

import sn.gestionimmobiliere.backend.owner.api.CreateOwnerRequest;
import sn.gestionimmobiliere.backend.owner.api.UpdateOwnerRequest;
import sn.gestionimmobiliere.backend.owner.domain.Owner;
import sn.gestionimmobiliere.backend.owner.infrastructure.OwnerRepository;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTests {

	private static final UUID ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final Instant NOW = Instant.parse("2026-06-14T10:15:30Z");

	@Mock
	private OwnerRepository ownerRepository;

	private OwnerService ownerService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
		ownerService = new OwnerService(ownerRepository, new OwnerProperties(ORGANIZATION_ID), clock);
	}

	@Test
	void createsAndNormalizesOwner() {
		CreateOwnerRequest request = new CreateOwnerRequest(
				"  Awa Diop  ",
				"  +221771234567  ",
				"  AWA.DIOP@example.com  ",
				"  Dakar Plateau  ");
		when(ownerRepository.saveAndFlush(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = ownerService.create(request);

		assertThat(response.fullName()).isEqualTo("Awa Diop");
		assertThat(response.phone()).isEqualTo("+221771234567");
		assertThat(response.email()).isEqualTo("awa.diop@example.com");
		assertThat(response.address()).isEqualTo("Dakar Plateau");
		assertThat(response.createdAt()).isEqualTo(NOW);
		assertThat(response.updatedAt()).isEqualTo(NOW);
		verify(ownerRepository).existsByOrganizationIdAndEmailIgnoreCaseAndDeletedAtIsNull(
				ORGANIZATION_ID, "awa.diop@example.com");
	}

	@Test
	void rejectsDuplicateEmailOnCreate() {
		CreateOwnerRequest request = new CreateOwnerRequest(
				"Awa Diop", "+221771234567", "awa@example.com", null);
		when(ownerRepository.existsByOrganizationIdAndEmailIgnoreCaseAndDeletedAtIsNull(
				ORGANIZATION_ID, "awa@example.com")).thenReturn(true);

		assertThatThrownBy(() -> ownerService.create(request))
				.isInstanceOf(DuplicateOwnerEmailException.class);
		verify(ownerRepository, never()).saveAndFlush(any(Owner.class));
	}

	@Test
	void rejectsDuplicateEmailOnUpdate() {
		UUID ownerId = UUID.randomUUID();
		Owner owner = owner(ownerId, "old@example.com");
		when(ownerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(ownerId, ORGANIZATION_ID))
				.thenReturn(Optional.of(owner));
		when(ownerRepository.existsByOrganizationIdAndEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(
				ORGANIZATION_ID, "used@example.com", ownerId)).thenReturn(true);

		UpdateOwnerRequest request = new UpdateOwnerRequest(
				"Awa Diop", "+221771234567", "used@example.com", "Dakar");

		assertThatThrownBy(() -> ownerService.update(ownerId, request))
				.isInstanceOf(DuplicateOwnerEmailException.class);
		verify(ownerRepository, never()).saveAndFlush(any(Owner.class));
	}

	@Test
	void throwsWhenOwnerDoesNotExist() {
		UUID ownerId = UUID.randomUUID();
		when(ownerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(ownerId, ORGANIZATION_ID))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> ownerService.findById(ownerId))
				.isInstanceOf(OwnerNotFoundException.class)
				.hasMessageContaining(ownerId.toString());
	}

	@Test
	void softDeletesOwner() {
		UUID ownerId = UUID.randomUUID();
		Owner owner = owner(ownerId, "awa@example.com");
		when(ownerRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(ownerId, ORGANIZATION_ID))
				.thenReturn(Optional.of(owner));

		ownerService.delete(ownerId);

		ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
		verify(ownerRepository).save(captor.capture());
		assertThat(captor.getValue().getDeletedAt()).isEqualTo(NOW);
		assertThat(captor.getValue().getUpdatedAt()).isEqualTo(NOW);
	}

	private Owner owner(UUID id, String email) {
		return Owner.create(id, ORGANIZATION_ID, "Awa Diop", "+221771234567", email, "Dakar", NOW.minusSeconds(60));
	}
}
