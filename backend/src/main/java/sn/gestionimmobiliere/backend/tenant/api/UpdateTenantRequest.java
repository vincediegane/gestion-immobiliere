package sn.gestionimmobiliere.backend.tenant.api;
import jakarta.validation.constraints.*;
public record UpdateTenantRequest(@NotBlank @Size(max=200) String fullName,@NotBlank @Pattern(regexp="^\\+?[0-9]{9,15}$") String phone,@Email String email,@Size(max=500) String address,@Size(max=100) String identityReference){}
