package sn.gestionimmobiliere.backend.billing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import sn.gestionimmobiliere.backend.billing.infrastructure.RentChargeRepository;
import sn.gestionimmobiliere.backend.identity.infrastructure.OrganizationRepository;
import sn.gestionimmobiliere.backend.lease.domain.Lease;
import sn.gestionimmobiliere.backend.lease.domain.LeaseStatus;
import sn.gestionimmobiliere.backend.lease.infrastructure.LeaseRepository;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;

@ExtendWith(MockitoExtension.class)
class ChargeGenerationServiceTests {
	private static final UUID ORGANIZATION_ID = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");

	@Mock LeaseRepository leases;
	@Mock RentChargeRepository charges;
	@Mock OrganizationRepository organizations;
	@Mock TransactionTemplate transactions;
	private ChargeGenerationService service;

	@BeforeEach
	void setUp() {
		service = new ChargeGenerationService(leases, charges, new OwnerProperties(ORGANIZATION_ID), organizations,
				Clock.fixed(NOW, ZoneOffset.UTC), transactions);
	}

	@Test
	void generatesEveryMissingMonthUpToCurrentMonth() {
		Lease lease = new Lease(UUID.randomUUID(), ORGANIZATION_ID, UUID.randomUUID(), UUID.randomUUID(),
				LocalDate.of(2026, 4, 10), null, 150_000, 0, 5, LeaseStatus.ACTIVE, NOW);
		when(leases.findAllByOrganizationIdAndStatusIn(ORGANIZATION_ID, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED))).thenReturn(List.of(lease));
		when(charges.insertIfAbsent(any(), eq(ORGANIZATION_ID), eq(lease.getId()), any(), any(), anyLong(),
				anyString(), any())).thenReturn(1);

		assertThat(service.generateForOrganization(ORGANIZATION_ID)).isEqualTo(3);
		verify(charges, times(3)).insertIfAbsent(any(), eq(ORGANIZATION_ID), eq(lease.getId()), any(), any(),
				anyLong(), anyString(), any());
	}

	@Test
	void generatesMissingMonthsForTerminatedLeaseUntilTerminationMonth() {
		Lease lease = new Lease(UUID.randomUUID(), ORGANIZATION_ID, UUID.randomUUID(), UUID.randomUUID(),
				LocalDate.of(2026, 2, 10), LocalDate.of(2026, 4, 20), 150_000, 0, 5, LeaseStatus.TERMINATED, NOW);
		when(leases.findAllByOrganizationIdAndStatusIn(ORGANIZATION_ID, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED))).thenReturn(List.of(lease));
		when(charges.insertIfAbsent(any(), eq(ORGANIZATION_ID), eq(lease.getId()), any(), any(), anyLong(),
				anyString(), any())).thenReturn(1);

		assertThat(service.generateForOrganization(ORGANIZATION_ID)).isEqualTo(3);
		verify(charges, times(3)).insertIfAbsent(any(), eq(ORGANIZATION_ID), eq(lease.getId()), any(), any(),
				anyLong(), anyString(), any());
	}

	@Test
	void schedulerProcessesEveryActiveOrganization() {
		UUID secondOrganization = UUID.randomUUID();
		when(organizations.findActiveIds()).thenReturn(List.of(ORGANIZATION_ID, secondOrganization));
		when(transactions.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Integer>>getArgument(0).doInTransaction(null));
		when(leases.findAllByOrganizationIdAndStatusIn(any(), eq(List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED)))).thenReturn(List.of());

		assertThat(service.generateAllOrganizations()).isZero();
		verify(leases).findAllByOrganizationIdAndStatusIn(ORGANIZATION_ID, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED));
		verify(leases).findAllByOrganizationIdAndStatusIn(secondOrganization, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED));
	}

	@Test
	void schedulerContinuesAfterOneOrganizationFails() {
		UUID secondOrganization = UUID.randomUUID();
		when(organizations.findActiveIds()).thenReturn(List.of(ORGANIZATION_ID, secondOrganization));
		when(transactions.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Integer>>getArgument(0).doInTransaction(null));
		when(leases.findAllByOrganizationIdAndStatusIn(ORGANIZATION_ID, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED)))
				.thenThrow(new IllegalStateException("database timeout"));
		when(leases.findAllByOrganizationIdAndStatusIn(secondOrganization, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED)))
				.thenReturn(List.of());

		assertThat(service.generateAllOrganizations()).isZero();
		verify(leases).findAllByOrganizationIdAndStatusIn(secondOrganization, List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED));
	}
}
