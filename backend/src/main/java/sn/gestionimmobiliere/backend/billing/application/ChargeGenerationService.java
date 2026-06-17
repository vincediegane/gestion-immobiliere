package sn.gestionimmobiliere.backend.billing.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sn.gestionimmobiliere.backend.billing.infrastructure.RentChargeRepository;
import sn.gestionimmobiliere.backend.billing.domain.ChargeStatus;
import sn.gestionimmobiliere.backend.identity.infrastructure.OrganizationRepository;
import sn.gestionimmobiliere.backend.lease.domain.LeaseStatus;
import sn.gestionimmobiliere.backend.lease.infrastructure.LeaseRepository;
import sn.gestionimmobiliere.backend.owner.application.OwnerProperties;

@Service
public class ChargeGenerationService {
	private static final Logger log = LoggerFactory.getLogger(ChargeGenerationService.class);
	private final LeaseRepository leases;
	private final RentChargeRepository charges;
	private final OwnerProperties properties;
	private final OrganizationRepository organizations;
	private final Clock clock;
	private final TransactionTemplate transactions;

	public ChargeGenerationService(LeaseRepository leases, RentChargeRepository charges, OwnerProperties properties,
			OrganizationRepository organizations, Clock clock, TransactionTemplate transactions) {
		this.leases = leases;
		this.charges = charges;
		this.properties = properties;
		this.organizations = organizations;
		this.clock = clock;
		this.transactions = transactions;
	}

	@Transactional
	public int generateCurrentMonth() {
		return generateForOrganization(properties.organizationId());
	}

	@Transactional
	public int generateForOrganization(UUID organizationId) {
		LocalDate today = LocalDate.now(clock.withZone(java.time.ZoneId.of("Africa/Dakar")));
		YearMonth currentPeriod = YearMonth.from(today);
		Instant now = clock.instant();
		int created = 0;

		for (var lease : leases.findAllByOrganizationIdAndStatusIn(organizationId,
				java.util.List.of(LeaseStatus.ACTIVE, LeaseStatus.TERMINATED))) {
			YearMonth firstPeriod = YearMonth.from(lease.getStartDate());
			YearMonth lastPeriod = lease.getEndDate() == null ? currentPeriod
					: YearMonth.from(lease.getEndDate()).isBefore(currentPeriod) ? YearMonth.from(lease.getEndDate()) : currentPeriod;
			for (YearMonth period = firstPeriod; !period.isAfter(lastPeriod); period = period.plusMonths(1)) {
				LocalDate periodStart = period.atDay(1);
				LocalDate dueDate = period.atDay(Math.min(lease.getDueDay(), period.lengthOfMonth()));
				ChargeStatus status = dueDate.isBefore(today) ? ChargeStatus.OVERDUE
						: dueDate.isEqual(today) ? ChargeStatus.DUE : ChargeStatus.UPCOMING;
				created += charges.insertIfAbsent(UUID.randomUUID(), organizationId, lease.getId(), periodStart, dueDate,
						lease.getMonthlyRent(), status.name(), now);
			}
		}
		return created;
	}

	@Scheduled(cron = "0 5 0 * * *", zone = "Africa/Dakar")
	public int generateAllOrganizations() {
		int created = 0;
		for (UUID organizationId : organizations.findActiveIds()) {
			try {
				created += transactions.execute(status -> generateForOrganization(organizationId));
			} catch (RuntimeException exception) {
				log.warn("Rent charge generation failed for organization {}", organizationId, exception);
			}
		}
		return created;
	}
}
