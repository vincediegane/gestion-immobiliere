package sn.gestionimmobiliere.backend.shared.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI realEstateOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Real Estate SaaS API")
				.description("API du MVP de gestion immobiliere pour le Senegal")
				.version("v1"));
	}
}
