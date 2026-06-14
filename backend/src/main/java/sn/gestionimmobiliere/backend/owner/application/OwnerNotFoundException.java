package sn.gestionimmobiliere.backend.owner.application;

import java.util.UUID;

public class OwnerNotFoundException extends RuntimeException {

	public OwnerNotFoundException(UUID id) {
		super("Proprietaire introuvable : " + id);
	}
}
