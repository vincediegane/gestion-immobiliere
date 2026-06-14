package sn.gestionimmobiliere.backend.property.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sn.gestionimmobiliere.backend.owner.application.OwnerNotFoundException;
import sn.gestionimmobiliere.backend.property.application.PropertyNotFoundException;
import sn.gestionimmobiliere.backend.property.application.PropertyService;
import sn.gestionimmobiliere.backend.property.domain.PropertyStatus;
import sn.gestionimmobiliere.backend.property.domain.PropertyType;
import sn.gestionimmobiliere.backend.shared.api.ApiExceptionHandler;
import sn.gestionimmobiliere.backend.identity.application.JwtService;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PropertyControllerTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PROPERTY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final Instant CREATED_AT = Instant.parse("2026-06-14T10:15:30Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PropertyService propertyService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void createsProperty() throws Exception {
		when(propertyService.create(any(CreatePropertyRequest.class))).thenReturn(propertyResponse());

		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/properties/" + PROPERTY_ID))
				.andExpect(jsonPath("$.id").value(PROPERTY_ID.toString()))
				.andExpect(jsonPath("$.ownerId").value(OWNER_ID.toString()));
	}

	@Test
	void rejectsInvalidProperty() throws Exception {
		CreatePropertyRequest request = new CreatePropertyRequest(
				null, " ", null, " ", " ", null, null);

		mockMvc.perform(post("/api/properties")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"))
				.andExpect(jsonPath("$.fieldErrors.ownerId").exists())
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.fieldErrors.address").exists())
				.andExpect(jsonPath("$.fieldErrors.city").exists())
				.andExpect(jsonPath("$.fieldErrors.type").exists())
				.andExpect(jsonPath("$.fieldErrors.status").exists());
	}

	@Test
	void listsProperties() throws Exception {
		when(propertyService.findAll(any())).thenReturn(new PageImpl<>(List.of(propertyResponse())));

		mockMvc.perform(get("/api/properties"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Residence Teranga"));
	}

	@Test
	void rejectsUnknownPropertySortProperty() throws Exception {
		mockMvc.perform(get("/api/properties").param("sort", "unknown,desc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("invalid-sort-property"));
	}

	@Test
	void returnsProperty() throws Exception {
		when(propertyService.findById(PROPERTY_ID)).thenReturn(propertyResponse());

		mockMvc.perform(get("/api/properties/{id}", PROPERTY_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("APARTMENT_BUILDING"));
	}

	@Test
	void updatesProperty() throws Exception {
		UpdatePropertyRequest request = new UpdatePropertyRequest(
				OWNER_ID, "Villa Ngor", null, "Route de Ngor", "Dakar", PropertyType.VILLA,
				PropertyStatus.OCCUPIED);
		PropertyResponse updated = new PropertyResponse(
				PROPERTY_ID, OWNER_ID, "Villa Ngor", null, "Route de Ngor", "Dakar", PropertyType.VILLA,
				PropertyStatus.OCCUPIED, CREATED_AT, CREATED_AT.plusSeconds(60));
		when(propertyService.update(eq(PROPERTY_ID), any(UpdatePropertyRequest.class))).thenReturn(updated);

		mockMvc.perform(put("/api/properties/{id}", PROPERTY_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Villa Ngor"))
				.andExpect(jsonPath("$.status").value("OCCUPIED"));
	}

	@Test
	void deletesProperty() throws Exception {
		doNothing().when(propertyService).delete(PROPERTY_ID);

		mockMvc.perform(delete("/api/properties/{id}", PROPERTY_ID))
				.andExpect(status().isNoContent());
	}

	@Test
	void listsPropertiesByOwner() throws Exception {
		when(propertyService.findByOwnerId(eq(OWNER_ID), any()))
				.thenReturn(new PageImpl<>(List.of(propertyResponse())));

		mockMvc.perform(get("/api/owners/{ownerId}/properties", OWNER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].ownerId").value(OWNER_ID.toString()));
	}

	@Test
	void returnsNotFoundForUnknownProperty() throws Exception {
		when(propertyService.findById(PROPERTY_ID)).thenThrow(new PropertyNotFoundException(PROPERTY_ID));

		mockMvc.perform(get("/api/properties/{id}", PROPERTY_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("property-not-found"));
	}

	@Test
	void returnsNotFoundForUnknownOwner() throws Exception {
		when(propertyService.findByOwnerId(eq(OWNER_ID), any())).thenThrow(new OwnerNotFoundException(OWNER_ID));

		mockMvc.perform(get("/api/owners/{ownerId}/properties", OWNER_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("owner-not-found"));
	}

	private CreatePropertyRequest createRequest() {
		return new CreatePropertyRequest(
				OWNER_ID, "Residence Teranga", "Immeuble R+4", "Almadies", "Dakar",
				PropertyType.APARTMENT_BUILDING, PropertyStatus.AVAILABLE);
	}

	private PropertyResponse propertyResponse() {
		return new PropertyResponse(
				PROPERTY_ID,
				OWNER_ID,
				"Residence Teranga",
				"Immeuble R+4",
				"Almadies",
				"Dakar",
				PropertyType.APARTMENT_BUILDING,
				PropertyStatus.AVAILABLE,
				CREATED_AT,
				CREATED_AT);
	}
}
