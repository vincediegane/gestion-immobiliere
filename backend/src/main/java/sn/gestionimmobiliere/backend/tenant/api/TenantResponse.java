package sn.gestionimmobiliere.backend.tenant.api;
import java.time.Instant; import java.util.UUID;
public record TenantResponse(UUID id,String fullName,String phone,String email,String address,String identityReference,Instant createdAt,Instant updatedAt){}
