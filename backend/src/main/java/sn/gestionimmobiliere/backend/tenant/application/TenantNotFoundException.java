package sn.gestionimmobiliere.backend.tenant.application; import java.util.UUID;
public class TenantNotFoundException extends RuntimeException{public TenantNotFoundException(UUID id){super("Locataire introuvable: "+id);}}
