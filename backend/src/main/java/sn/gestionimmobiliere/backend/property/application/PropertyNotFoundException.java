package sn.gestionimmobiliere.backend.property.application;

import java.util.UUID;

public class PropertyNotFoundException extends RuntimeException {

	public PropertyNotFoundException(UUID id) {
		super("Aucun bien immobilier actif ne correspond a l'identifiant " + id);
	}
}
