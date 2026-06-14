package sn.gestionimmobiliere.backend.billing.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.gestionimmobiliere.backend.billing.domain.RentCharge;
import sn.gestionimmobiliere.backend.billing.infrastructure.RentChargeRepository;
import sn.gestionimmobiliere.backend.lease.domain.LeaseStatus;
import sn.gestionimmobiliere.backend.lease.infrastructure.LeaseRepository;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;

@Service
public class ChargeGenerationService {
	private final LeaseRepository leases;
	private final RentChargeRepository charges;
	private final OwnerProperties properties;
	private final Clock clock;

	public ChargeGenerationService(LeaseRepository leases, RentChargeRepository charges, OwnerProperties properties,
			Clock clock) {
		this.leases = leases;
		this.charges = charges;
		this.properties = properties;
		this.clock = clock;
	}

	@Transactional
	@Scheduled(cron = "0 5 0 * * *", zone = "Africa/Dakar")
	public int generateCurrentMonth() {
		UUID organizationId = properties.organizationId();
		LocalDate today = LocalDate.now(clock);
		LocalDate periodStart = today.withDayOfMonth(1);
		YearMonth period = YearMonth.from(periodStart);
		Instant now = clock.instant();
		int created = 0;

		for (var lease : leases.findAllByOrganizationIdAndStatus(organizationId, LeaseStatus.ACTIVE)) {
			if (period.isBefore(YearMonth.from(lease.getStartDate()))
					|| lease.getEndDate() != null && period.isAfter(YearMonth.from(lease.getEndDate()))
					|| charges.existsByOrganizationIdAndLeaseIdAndPeriodStart(organizationId, lease.getId(), periodStart)) {
				continue;
			}
			LocalDate dueDate = period.atDay(Math.min(lease.getDueDay(), period.lengthOfMonth()));
			charges.save(new RentCharge(UUID.randomUUID(), organizationId, lease.getId(), periodStart, dueDate,
					lease.getMonthlyRent(), today, now));
			created++;
		}
		return created;
	}
}
