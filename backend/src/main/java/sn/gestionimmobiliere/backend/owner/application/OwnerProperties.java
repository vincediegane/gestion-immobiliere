package sn.gestionimmobiliere.backend.owner.application;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sn.gestionimmobiliere.backend.identity.application.TenantContext;

@ConfigurationProperties("app.owner")
public record OwnerProperties(UUID organizationId) {
	@Override
	public UUID organizationId() {
		return TenantContext.currentOrganization().orElse(organizationId);
	}
}
