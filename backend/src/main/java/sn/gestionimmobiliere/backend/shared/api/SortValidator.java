package sn.gestionimmobiliere.backend.shared.api;

import java.util.Set;

import org.springframework.data.domain.Pageable;

public final class SortValidator {

	private SortValidator() {
	}

	public static void validate(Pageable pageable, Set<String> allowedProperties) {
		pageable.getSort().forEach(order -> {
			if (!allowedProperties.contains(order.getProperty())) {
				throw new InvalidSortPropertyException(order.getProperty());
			}
		});
	}
}
