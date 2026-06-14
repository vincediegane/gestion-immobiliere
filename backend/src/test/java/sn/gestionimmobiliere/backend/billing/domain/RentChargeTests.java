package sn.gestionimmobiliere.backend.billing.domain;

import static org.assertj.core.api.Assertions.*;
import java.time.*;import java.util.UUID;import org.junit.jupiter.api.Test;

class RentChargeTests {
	private static final Instant NOW=Instant.parse("2026-06-14T10:00:00Z");
	@Test void supportsPartialThenFullPayment(){RentCharge charge=new RentCharge(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),LocalDate.of(2026,6,1),LocalDate.of(2026,6,5),100_000,LocalDate.of(2026,6,14),NOW);assertThat(charge.getStatus()).isEqualTo(ChargeStatus.OVERDUE);charge.pay(40_000,LocalDate.of(2026,6,14),NOW);assertThat(charge.getStatus()).isEqualTo(ChargeStatus.PARTIAL);assertThat(charge.getBalance()).isEqualTo(60_000);charge.pay(60_000,LocalDate.of(2026,6,14),NOW);assertThat(charge.getStatus()).isEqualTo(ChargeStatus.PAID);assertThat(charge.getBalance()).isZero();}
	@Test void rejectsOverpayment(){RentCharge charge=new RentCharge(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),LocalDate.of(2026,6,1),LocalDate.of(2026,6,5),100_000,LocalDate.of(2026,6,14),NOW);assertThatThrownBy(()->charge.pay(100_001,LocalDate.of(2026,6,14),NOW)).isInstanceOf(IllegalArgumentException.class);}
}
