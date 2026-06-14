package sn.gestionimmobiliere.backend.tenant.domain;
import java.time.Instant; import java.util.UUID; import jakarta.persistence.*;
@Entity @Table(name="tenants")
public class Tenant {
	@Id private UUID id; @Column(name="organization_id",nullable=false) private UUID organizationId;
	@Column(name="full_name",nullable=false) private String fullName; @Column(nullable=false) private String phone;
	private String email; private String address; @Column(name="identity_reference") private String identityReference;
	@Column(name="created_at",nullable=false) private Instant createdAt; @Column(name="updated_at",nullable=false) private Instant updatedAt; @Column(name="deleted_at") private Instant deletedAt;
	protected Tenant(){} public Tenant(UUID id,UUID organizationId,String fullName,String phone,String email,String address,String identityReference,Instant now){this.id=id;this.organizationId=organizationId;this.fullName=fullName;this.phone=phone;this.email=email;this.address=address;this.identityReference=identityReference;this.createdAt=now;this.updatedAt=now;}
	public void update(String fullName,String phone,String email,String address,String identityReference,Instant now){this.fullName=fullName;this.phone=phone;this.email=email;this.address=address;this.identityReference=identityReference;this.updatedAt=now;}
	public void archive(Instant now){deletedAt=now;updatedAt=now;}
	public UUID getId(){return id;} public String getFullName(){return fullName;} public String getPhone(){return phone;} public String getEmail(){return email;} public String getAddress(){return address;} public String getIdentityReference(){return identityReference;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
