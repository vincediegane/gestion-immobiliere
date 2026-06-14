package sn.gestionimmobiliere.backend.owner.api;

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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sn.gestionimmobiliere.backend.owner.application.DuplicateOwnerEmailException;
import sn.gestionimmobiliere.backend.owner.application.OwnerNotFoundException;
import sn.gestionimmobiliere.backend.owner.application.OwnerService;
import sn.gestionimmobiliere.backend.shared.api.ApiExceptionHandler;

@WebMvcTest(OwnerController.class)
@Import(ApiExceptionHandler.class)
class OwnerControllerTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final Instant CREATED_AT = Instant.parse("2026-06-14T10:15:30Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerService ownerService;

	@Test
	void createsOwner() throws Exception {
		CreateOwnerRequest request = new CreateOwnerRequest(
				"Awa Diop", "+221771234567", "awa@example.com", "Dakar");
		when(ownerService.create(any(CreateOwnerRequest.class))).thenReturn(ownerResponse());

		mockMvc.perform(post("/api/owners")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/owners/" + OWNER_ID))
				.andExpect(jsonPath("$.id").value(OWNER_ID.toString()))
				.andExpect(jsonPath("$.fullName").value("Awa Diop"));
	}

	@Test
	void rejectsInvalidOwner() throws Exception {
		CreateOwnerRequest request = new CreateOwnerRequest(" ", "770000000", "invalid", null);

		mockMvc.perform(post("/api/owners")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"))
				.andExpect(jsonPath("$.fieldErrors.fullName").exists())
				.andExpect(jsonPath("$.fieldErrors.phone").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists());
	}

	@Test
	void rejectsJsonThatIsNotEncodedInUtf8() throws Exception {
		String json = """
				{"fullName":"Awa Senegal","phone":"+221771234567","email":null,"address":"Senegal"}
				""".replace("Senegal", "Sénégal");

		mockMvc.perform(post("/api/owners")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.getBytes(StandardCharsets.ISO_8859_1)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("malformed-json"))
				.andExpect(jsonPath("$.detail").value(
						"Le corps de la requete doit contenir un JSON valide encode en UTF-8"));
	}

	@Test
	void listsOwners() throws Exception {
		when(ownerService.findAll(any())).thenReturn(new PageImpl<>(List.of(ownerResponse())));

		mockMvc.perform(get("/api/owners"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(OWNER_ID.toString()));
	}

	@Test
	void rejectsUnknownOwnerSortProperty() throws Exception {
		mockMvc.perform(get("/api/owners")
				.param("page", "0")
				.param("size", "1")
				.param("sort", "[\"string\"]"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("invalid-sort-property"))
				.andExpect(jsonPath("$.detail").value("Le champ de tri '[\"string\"]' n'est pas autorise"));
	}

	@Test
	void returnsOwner() throws Exception {
		when(ownerService.findById(OWNER_ID)).thenReturn(ownerResponse());

		mockMvc.perform(get("/api/owners/{id}", OWNER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("awa@example.com"));
	}

	@Test
	void updatesOwner() throws Exception {
		UpdateOwnerRequest request = new UpdateOwnerRequest(
				"Awa Ndiaye", "+221771234567", "awa@example.com", "Dakar");
		OwnerResponse updated = new OwnerResponse(
				OWNER_ID, "Awa Ndiaye", "+221771234567", "awa@example.com", "Dakar", CREATED_AT,
				CREATED_AT.plusSeconds(60));
		when(ownerService.update(eq(OWNER_ID), any(UpdateOwnerRequest.class))).thenReturn(updated);

		mockMvc.perform(put("/api/owners/{id}", OWNER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Awa Ndiaye"));
	}

	@Test
	void deletesOwner() throws Exception {
		doNothing().when(ownerService).delete(OWNER_ID);

		mockMvc.perform(delete("/api/owners/{id}", OWNER_ID))
				.andExpect(status().isNoContent());
	}

	@Test
	void returnsNotFoundProblem() throws Exception {
		when(ownerService.findById(OWNER_ID)).thenThrow(new OwnerNotFoundException(OWNER_ID));

		mockMvc.perform(get("/api/owners/{id}", OWNER_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("owner-not-found"));
	}

	@Test
	void returnsConflictForDuplicateEmail() throws Exception {
		CreateOwnerRequest request = new CreateOwnerRequest(
				"Awa Diop", "+221771234567", "awa@example.com", null);
		when(ownerService.create(any(CreateOwnerRequest.class)))
				.thenThrow(new DuplicateOwnerEmailException("awa@example.com"));

		mockMvc.perform(post("/api/owners")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("owner-email-conflict"));
	}

	private OwnerResponse ownerResponse() {
		return new OwnerResponse(
				OWNER_ID,
				"Awa Diop",
				"+221771234567",
				"awa@example.com",
				"Dakar",
				CREATED_AT,
				CREATED_AT);
	}
}
