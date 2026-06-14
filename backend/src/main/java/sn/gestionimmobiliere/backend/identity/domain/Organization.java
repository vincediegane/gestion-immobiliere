package sn.gestionimmobiliere.backend.identity.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization {
	@Id
	private UUID id;
	private String status;

	protected Organization() {
	}

	public UUID getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}
}
