package sn.gestionimmobiliere.backend.property.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "properties")
public class Property {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "owner_id", nullable = false)
	private UUID ownerId;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false, length = 500)
	private String address;

	@Column(nullable = false, length = 120)
	private String city;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private PropertyType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private PropertyStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected Property() {
	}

	private Property(UUID id, UUID organizationId, UUID ownerId, String name, String description, String address,
			String city, PropertyType type, PropertyStatus status, Instant createdAt) {
		this.id = id;
		this.organizationId = organizationId;
		this.ownerId = ownerId;
		this.name = name;
		this.description = description;
		this.address = address;
		this.city = city;
		this.type = type;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public static Property create(UUID id, UUID organizationId, UUID ownerId, String name, String description,
			String address, String city, PropertyType type, PropertyStatus status, Instant createdAt) {
		return new Property(id, organizationId, ownerId, name, description, address, city, type, status, createdAt);
	}

	public void update(UUID ownerId, String name, String description, String address, String city, PropertyType type,
			PropertyStatus status, Instant updatedAt) {
		this.ownerId = ownerId;
		this.name = name;
		this.description = description;
		this.address = address;
		this.city = city;
		this.type = type;
		this.status = status;
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

	public UUID getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public PropertyType getType() {
		return type;
	}

	public PropertyStatus getStatus() {
		return status;
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
