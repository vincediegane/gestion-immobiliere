package sn.gestionimmobiliere.backend.billing.application;

public class PaymentConflictException extends RuntimeException {
	public PaymentConflictException(String message) {
		super(message);
	}
}
