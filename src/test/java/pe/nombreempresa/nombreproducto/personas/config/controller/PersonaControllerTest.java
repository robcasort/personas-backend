package pe.nombreempresa.nombreproducto.personas.config.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import pe.nombreempresa.nombreproducto.personas.controller.request.PersonaRequest;
import pe.nombreempresa.nombreproducto.personas.model.Persona;
import pe.nombreempresa.nombreproducto.personas.repository.PersonaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonaControllerTest {
	private final static String BASE_URI = "http://localhost";	
	
	@LocalServerPort
	protected int localPort;
	
	@Autowired
	private PersonaRepository personaRepository;
	
	private static List<Persona> personasFileStore;
	private List<Persona> personasSaveApiService;

	@BeforeAll	
	public static void beforeAll() {
		// Registrar personas de prueba en base de datos
		try {
			String jsonString = new String(new ClassPathResource("persona-data.json").getInputStream().readAllBytes(),
					StandardCharsets.UTF_8);

			JsonMapper jsonMapper = JsonMapper.builder()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();

			personasFileStore = jsonMapper.readValue(jsonString, new TypeReference<List<Persona>>(){});
			personasFileStore.stream().forEach(p -> System.out.println(p));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@BeforeEach	
	void setUp() {
		// Configurar la url y puerto del servicio en testing
		RestAssured.baseURI = BASE_URI;			
		RestAssured.port = localPort;
		
		// Registrar las personas en el repositorio
		personasSaveApiService = new ArrayList<Persona>();
		personasFileStore.stream().forEach(p -> {		
			personasSaveApiService.add(personaRepository.save(p));
		});
	}

	@AfterEach	
	void tearDown() {
		// Borrar las personas del respositorio
		personaRepository.deleteAll();
		personasSaveApiService.clear();
		personasSaveApiService = null;
	}

	@Test
	@Disabled
	@DisplayName("Verificar que el puerto local sea mayor a cero")
    public void verificarPuertoLocal() {
        assertThat(localPort).isGreaterThan(0);
    }
	
	@Test
	@Disabled
	@DisplayName("Verificar el servicio que devuelve la lista de personas")
	public void givenUrl_whenSuccessOnGetsAllPeoplesResponse_thenCorrect() {
		Persona persona = personasSaveApiService.get(0);
		
		RequestSpecification httpRequest = given();
		Response response = httpRequest.get("/apipersonas/personas");
		
		ResponseBody<?> body = response.getBody();			
		
		// Validar codigo de respuesta y que exista contenido
		response
			.then()
			.assertThat()
			.log().all()
			.statusCode(200)			
			.body("size()", greaterThan(0));
			
		// Validar que exista contenido con informacion de personas
		String bodyAsString = body.asString();
		assertEquals(bodyAsString.contains(persona.getId()), true, "No se ha encontrado el Id");
		assertEquals(bodyAsString.contains(persona.getNombre()), true, "No se ha encontrado el nombre");
		assertEquals(bodyAsString.contains(persona.getApellidos()), true, "No se ha encontrado los apellidos");
		assertEquals(bodyAsString.contains(String.valueOf(persona.getEdad())), true, "No se ha encontrado la edad");
		assertEquals(bodyAsString.contains(persona.getEmail()), true, "No se ha encontrado el email");		
	}
	
	@Test
	@Disabled
	@DisplayName("Verificar el servicio que devuelve el detalle de una persona")
	public void givenUrl_whenSuccessOnGetsDetailPeopleResponse_thenCorrect() {
		Persona persona = personasSaveApiService.get(0);
		given()
			.contentType(ContentType.JSON)
			.when()
			.get("/apipersonas/personas/" + persona.getId())
			.then()
			.assertThat()
			.log().all()
			.statusCode(200)
			.body("id", equalTo(persona.getId()))
			.body("nombre", equalTo(persona.getNombre()))
			.body("apellidos", equalTo(persona.getApellidos()))
			.body("edad", equalTo(persona.getEdad()))
			.body("email", equalTo(persona.getEmail()));
	}
	
	@Test
	@Disabled
	@DisplayName("Verificar el servicio que guarda informacion de una persona")
	public void givenUrl_whenSuccessOnPostSavePeopleResponse_thenCorrect() throws JSONException {
		JSONObject newPersona = new JSONObject();
		
		newPersona.put("nombre", "Timmy");
		newPersona.put("apellidos", "Jackson");
		newPersona.put("edad", "36");
		newPersona.put("email", "tjackson@gmail.com");
		
		given()
			.contentType(ContentType.JSON).body(newPersona.toString())
			.when()
			.post("/apipersonas/personas")
			.then()
			.log().all()
			.assertThat()
			.statusCode(201)
			.body("id", notNullValue())
			.body("nombre", equalTo("Timmy"))
			.body("apellidos", equalTo("Jackson"))
			.body("edad", equalTo(36))
			.body("email", equalTo("tjackson@gmail.com"));
	}
	
	 @Test
	 @Disabled
	 @DisplayName("Verificar el servicio que actualiza informacion de una persona")
	 public void givenUrl_whenSuccessOnUpdateResponse_thenCorrect() throws JSONException, JsonProcessingException {
		 JsonMapper jsonMapper = JsonMapper.builder()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
		 
		 Persona persona = personasSaveApiService.get(0);
		 
		 PersonaRequest updatedPersona = new PersonaRequest(
				 Persona.builder()
				 	.nombre("Benito")
				 	.apellidos("Siancas")
				 	.edad(53)
				 	.email("bsiancas@gmail.com")
				 	.build());		 
		 
		 String jsonBody = jsonMapper.writeValueAsString(updatedPersona);
			
		 given()
			.contentType(ContentType.JSON).body(jsonBody)
			.when()
			.put("/apipersonas/personas/" + persona.getId())
			.then()
			.log().all()
			.assertThat()
			.statusCode(201)
			.body("id", equalTo(persona.getId()))
			.body("nombre", equalTo(updatedPersona.getNombre()))
			.body("apellidos", equalTo(updatedPersona.getApellidos()))
			.body("edad", equalTo(updatedPersona.getEdad()))
			.body("email", equalTo(updatedPersona.getEmail()));
	 }
	 
	 @Test
	 @Disabled
	 @DisplayName("Verificar el servicio que borra la informacion de una persona")
	 public void givenUrl_whenSuccessOnDeleteResponse_thenCorrect() {
		 Persona persona = personasSaveApiService.get(0);
		 
		 // Eliminar los datos de la persona
		 given()
		 	.contentType(ContentType.JSON)
		 	.when()
		 	.delete("/apipersonas/personas/" + persona.getId())
		 	.then()
		 	.log().all()
		 	.assertThat().statusCode(204);
		 
		 // Verificar que la persona ya no exista
		 given()
			.contentType(ContentType.JSON)
			.when()
			.get("/apipersonas/personas/" + persona.getId())
			.then()
			.assertThat()
			.log().all()
			.statusCode(404);
	 }
}
