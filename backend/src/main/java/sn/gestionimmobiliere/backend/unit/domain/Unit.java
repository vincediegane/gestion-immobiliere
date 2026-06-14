package sn.gestionimmobiliere.backend.unit.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "units")
public class Unit {
	@Id private UUID id;
	@Column(name="organization_id", nullable=false) private UUID organizationId;
	@Column(name="property_id", nullable=false) private UUID propertyId;
	@Column(nullable=false, length=200) private String name;
	@Enumerated(EnumType.STRING) @Column(nullable=false) private UnitType type;
	@Enumerated(EnumType.STRING) @Column(nullable=false) private UnitStatus status;
	@Column(length=1000) private String description;
	@Column(name="monthly_rent", nullable=false) private long monthlyRent;
	@Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
	@Column(name="updated_at", nullable=false) private Instant updatedAt;
	@Column(name="deleted_at") private Instant deletedAt;
	protected Unit() {}
	public Unit(UUID id, UUID organizationId, UUID propertyId, String name, UnitType type, UnitStatus status,
			String description, long monthlyRent, Instant now) {
		this.id=id; this.organizationId=organizationId; this.propertyId=propertyId; this.name=name; this.type=type;
		this.status=status; this.description=description; this.monthlyRent=monthlyRent; this.createdAt=now; this.updatedAt=now;
	}
	public void update(String name, UnitType type, UnitStatus status, String description, long monthlyRent, Instant now) {
		this.name=name; this.type=type; this.status=status; this.description=description; this.monthlyRent=monthlyRent; this.updatedAt=now;
	}
	public void archive(Instant now) { deletedAt=now; updatedAt=now; }
	public void occupy(Instant now) { status=UnitStatus.OCCUPIED; updatedAt=now; }
	public void release(Instant now) { status=UnitStatus.AVAILABLE; updatedAt=now; }
	public UUID getId(){return id;} public UUID getOrganizationId(){return organizationId;} public UUID getPropertyId(){return propertyId;}
	public String getName(){return name;} public UnitType getType(){return type;} public UnitStatus getStatus(){return status;}
	public String getDescription(){return description;} public long getMonthlyRent(){return monthlyRent;}
	public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
