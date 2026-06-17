package sn.gestionimmobiliere.backend.billing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sn.gestionimmobiliere.backend.billing.api.CreatePaymentRequest;
import sn.gestionimmobiliere.backend.billing.domain.Payment;
import sn.gestionimmobiliere.backend.billing.domain.PaymentMethod;
import sn.gestionimmobiliere.backend.billing.domain.RentCharge;
import sn.gestionimmobiliere.backend.billing.infrastructure.PaymentRepository;
import sn.gestionimmobiliere.backend.billing.infrastructure.RentChargeRepository;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;

@ExtendWith(MockitoExtension.class)
class BillingServiceTests {
	private static final UUID ORGANIZATION_ID = UUID.randomUUID();
	private static final UUID CHARGE_ID = UUID.randomUUID();
	private static final UUID LEASE_ID = UUID.randomUUID();
	private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");

	@Mock RentChargeRepository charges;
	@Mock PaymentRepository payments;
	private BillingService service;

	@BeforeEach
	void setUp() {
		service = new BillingService(charges, payments, new OwnerProperties(ORGANIZATION_ID),
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void returnsExistingPaymentForSameIdempotencyKey() {
		RentCharge charge = charge(100_000);
		Payment previous = new Payment(UUID.randomUUID(), ORGANIZATION_ID, CHARGE_ID, "payment-1", 50_000,
				LocalDate.of(2026, 6, 14), PaymentMethod.CASH, null, NOW);
		when(charges.findForPayment(CHARGE_ID, ORGANIZATION_ID)).thenReturn(Optional.of(charge));
		when(payments.findByOrganizationIdAndIdempotencyKey(ORGANIZATION_ID, "payment-1"))
				.thenReturn(Optional.of(previous));

		var response = service.pay(CHARGE_ID, request(50_000), "payment-1");

		assertThat(response.id()).isEqualTo(previous.getId());
		assertThat(charge.getAmountPaid()).isZero();
		verify(payments, never()).saveAndFlush(any());
	}

	@Test
	void rejectsSameIdempotencyKeyWithDifferentPayload() {
		RentCharge charge = charge(100_000);
		Payment previous = new Payment(UUID.randomUUID(), ORGANIZATION_ID, CHARGE_ID, "payment-1", 50_000,
				LocalDate.of(2026, 6, 14), PaymentMethod.CASH, null, NOW);
		when(charges.findForPayment(CHARGE_ID, ORGANIZATION_ID)).thenReturn(Optional.of(charge));
		when(payments.findByOrganizationIdAndIdempotencyKey(ORGANIZATION_ID, "payment-1"))
				.thenReturn(Optional.of(previous));

		assertThatThrownBy(() -> service.pay(CHARGE_ID, request(60_000), "payment-1"))
				.isInstanceOf(PaymentConflictException.class)
				.hasMessageContaining("contenu different");
		verify(payments, never()).saveAndFlush(any());
	}

	@Test
	void rejectsAmountAboveLockedRemainingBalance() {
		RentCharge charge = charge(100_000);
		charge.pay(80_000, LocalDate.of(2026, 6, 14), NOW);
		when(charges.findForPayment(CHARGE_ID, ORGANIZATION_ID)).thenReturn(Optional.of(charge));
		when(payments.findByOrganizationIdAndIdempotencyKey(ORGANIZATION_ID, "payment-2"))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.pay(CHARGE_ID, request(30_000), "payment-2"))
				.isInstanceOf(PaymentConflictException.class)
				.hasMessageContaining("depasse");
		verify(payments, never()).saveAndFlush(any());
	}

	private RentCharge charge(long amount) {
		return new RentCharge(CHARGE_ID, ORGANIZATION_ID, LEASE_ID, LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 6, 5), amount, LocalDate.of(2026, 6, 14), NOW);
	}

	private CreatePaymentRequest request(long amount) {
		return new CreatePaymentRequest(amount, LocalDate.of(2026, 6, 14), PaymentMethod.CASH, null);
	}
}
