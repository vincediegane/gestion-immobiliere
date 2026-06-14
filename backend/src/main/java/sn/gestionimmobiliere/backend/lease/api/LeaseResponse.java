package sn.gestionimmobiliere.backend.lease.api;
import java.time.*;import java.util.UUID;import sn.gestionimmobiliere.backend.lease.domain.LeaseStatus;
public record LeaseResponse(UUID id,UUID unitId,UUID tenantId,LocalDate startDate,LocalDate endDate,long monthlyRent,long depositAmount,int dueDay,LeaseStatus status,Instant createdAt,Instant updatedAt){}
