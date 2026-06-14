package sn.gestionimmobiliere.backend.lease.api;
import java.time.LocalDate;import java.util.UUID;import jakarta.validation.constraints.*;
public record CreateLeaseRequest(@NotNull UUID unitId,@NotNull UUID tenantId,@NotNull LocalDate startDate,LocalDate endDate,@Positive long monthlyRent,@PositiveOrZero long depositAmount,@Min(1) @Max(28) int dueDay){}
