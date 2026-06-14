package sn.gestionimmobiliere.backend.owner.application;

public class DuplicateOwnerEmailException extends RuntimeException {

	public DuplicateOwnerEmailException(String email) {
		super("Un proprietaire utilise deja l'adresse email " + email);
	}
}
