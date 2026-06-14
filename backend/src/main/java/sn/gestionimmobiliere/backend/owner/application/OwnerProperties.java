package sn.gestionimmobiliere.backend.owner.application;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.owner")
public record OwnerProperties(UUID organizationId) {
}
