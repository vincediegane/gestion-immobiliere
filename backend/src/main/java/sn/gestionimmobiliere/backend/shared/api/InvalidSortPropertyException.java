package sn.gestionimmobiliere.backend.shared.api;

public class InvalidSortPropertyException extends RuntimeException {

	public InvalidSortPropertyException(String property) {
		super("Le champ de tri '" + property + "' n'est pas autorise");
	}
}
