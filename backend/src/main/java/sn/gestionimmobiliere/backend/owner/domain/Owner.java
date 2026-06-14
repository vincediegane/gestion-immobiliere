package sn.gestionimmobiliere.backend.owner.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "owners")
public class Owner {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "full_name", nullable = false, length = 200)
	private String fullName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(length = 320)
	private String email;

	@Column(length = 500)
	private String address;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected Owner() {
	}

	private Owner(UUID id, UUID organizationId, String fullName, String phone, String email, String address,
			Instant createdAt) {
		this.id = id;
		this.organizationId = organizationId;
		this.fullName = fullName;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public static Owner create(UUID id, UUID organizationId, String fullName, String phone, String email,
			String address, Instant createdAt) {
		return new Owner(id, organizationId, fullName, phone, email, address, createdAt);
	}

	public void update(String fullName, String phone, String email, String address, Instant updatedAt) {
		this.fullName = fullName;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.updatedAt = updatedAt;
	}

	public void archive(Instant deletedAt) {
		this.deletedAt = deletedAt;
		this.updatedAt = deletedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getAddress() {
		return address;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
}
