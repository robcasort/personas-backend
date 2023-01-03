package pe.nombreempresa.nombreproducto.personas.config.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import pe.nombreempresa.nombreproducto.personas.controller.PersonaController;
import pe.nombreempresa.nombreproducto.personas.controller.hateoas.PersonaModelAssembler;
import pe.nombreempresa.nombreproducto.personas.security.config.WebSecurityConfig;
import pe.nombreempresa.nombreproducto.personas.security.controller.AuthController;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.request.LoginRequest;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.response.JwtResponse;
import pe.nombreempresa.nombreproducto.personas.security.jwt.AuthEntryPointJwt;
import pe.nombreempresa.nombreproducto.personas.security.jwt.JwtUtils;
import pe.nombreempresa.nombreproducto.personas.security.model.ERole;
import pe.nombreempresa.nombreproducto.personas.security.model.RefreshToken;
import pe.nombreempresa.nombreproducto.personas.security.model.Role;
import pe.nombreempresa.nombreproducto.personas.security.model.User;
import pe.nombreempresa.nombreproducto.personas.security.repository.RoleRepository;
import pe.nombreempresa.nombreproducto.personas.security.repository.UserRepository;
import pe.nombreempresa.nombreproducto.personas.security.service.RefreshTokenService;
import pe.nombreempresa.nombreproducto.personas.security.service.UserDetailsImpl;
import pe.nombreempresa.nombreproducto.personas.security.service.UserDetailsServiceImpl;
import pe.nombreempresa.nombreproducto.personas.service.PersonaServiceImpl;

@WebMvcTest(controllers = { PersonaController.class, AuthController.class })
@Import(value = { WebSecurityConfig.class, AuthEntryPointJwt.class, JwtUtils.class, PersonaModelAssembler.class })
class PersonaControllerMVCTest {
	
	@Autowired
	MockMvc mvc;

	@MockBean
	private UserDetailsServiceImpl userDetailsService;

	@MockBean
	private PersonaServiceImpl personaService;
	
	@MockBean
	private UserRepository userRepository;
		
	@MockBean
	private RoleRepository roleRepository;	

	@MockBean
	RefreshTokenService refreshTokenService;	

	@Test
	void tokenWhenUserSignInWithValidCredentialsThenStatusIsOk() throws Exception {
		User userSign = new User("jperez", "jperez@gmail.com", new BCryptPasswordEncoder().encode("admin1234")); 
		Set<Role> roles = new HashSet<>();
		roles.add(new Role(ERole.ROLE_ADMIN)); 
		userSign.setRoles(roles);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userSign);
		refreshToken.setExpiryDate(Instant.now().plusMillis(60000L));  // 60 segundos
		refreshToken.setToken(UUID.randomUUID().toString());
		 
		when(userDetailsService.loadUserByUsername(anyString())).thenReturn(UserDetailsImpl.build(userSign));		
		when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);		
		 
		this.mvc.perform(post("/apipersonas/auth/signin")
				.content(asJsonString(new LoginRequest("jperez", "admin1234")))
				.contentType(MediaType.APPLICATION_JSON)				
				.accept(MediaType.APPLICATION_JSON))
			//.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk());
	}

	@Test
	void tokenWhenAnonymousUserSignInThenStatusIsUnauthorized() throws Exception {
		User userSign = new User("jperez", "jperez@gmail.com", new BCryptPasswordEncoder().encode("admin1234")); 
		Set<Role> roles = new HashSet<>();
		roles.add(new Role(ERole.ROLE_ADMIN)); 
		userSign.setRoles(roles);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userSign);
		refreshToken.setExpiryDate(Instant.now().plusMillis(60000L));  // 60 segundos
		refreshToken.setToken(UUID.randomUUID().toString());
		 
		when(userDetailsService.loadUserByUsername(anyString())).thenReturn(UserDetailsImpl.build(userSign));		
		when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);		
		
		this.mvc.perform(post("/apipersonas/auth/signin")
				.content(asJsonString(new LoginRequest("jperez", "secret")))
				.contentType(MediaType.APPLICATION_JSON)				
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void tokenWhenAnonymousUserSignInThenStatusIsBadRequest() throws Exception {
		User userSign = new User("jperez", "jperez@gmail.com", new BCryptPasswordEncoder().encode("admin1234")); 
		Set<Role> roles = new HashSet<>();
		roles.add(new Role(ERole.ROLE_ADMIN)); 
		userSign.setRoles(roles);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userSign);
		refreshToken.setExpiryDate(Instant.now().plusMillis(60000L));  // 60 segundos
		refreshToken.setToken(UUID.randomUUID().toString());
		 
		when(userDetailsService.loadUserByUsername(anyString())).thenReturn(UserDetailsImpl.build(userSign));		
		when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);		
		
		this.mvc.perform(post("/apipersonas/auth/signin"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	void tokenWithValidCredentialsThenGetToken() throws Exception {
		User userSign = new User("jperez", "jperez@gmail.com", new BCryptPasswordEncoder().encode("admin1234")); 
		Set<Role> roles = new HashSet<>();
		roles.add(new Role(ERole.ROLE_ADMIN)); 
		userSign.setRoles(roles);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userSign);
		refreshToken.setExpiryDate(Instant.now().plusMillis(60000L));  // 60 segundos
		refreshToken.setToken(UUID.randomUUID().toString());
		 
		when(userDetailsService.loadUserByUsername(anyString())).thenReturn(UserDetailsImpl.build(userSign));		
		when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);		
		 
		MvcResult result = this.mvc.perform(post("/apipersonas/auth/signin")
				.content(asJsonString(new LoginRequest("jperez", "admin1234")))
				.contentType(MediaType.APPLICATION_JSON)				
				.accept(MediaType.APPLICATION_JSON))
			//.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk()).andReturn();
		
		assertThat(result.getResponse().getContentAsString()).isNotEmpty();
	}

	@Test
	public void getAllPeoplesWithValidCredentialsThenStatusIsOK() throws Exception {
		// Obtener el token
		User userSign = new User("jperez", "jperez@gmail.com", new BCryptPasswordEncoder().encode("admin1234")); 
		Set<Role> roles = new HashSet<>();
		roles.add(new Role(ERole.ROLE_ADMIN)); 
		userSign.setRoles(roles);
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userSign);
		refreshToken.setExpiryDate(Instant.now().plusMillis(60000L));  // 60 segundos
		refreshToken.setToken(UUID.randomUUID().toString());
		 
		when(userDetailsService.loadUserByUsername(anyString())).thenReturn(UserDetailsImpl.build(userSign));		
		when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);		
		 
		MvcResult result = this.mvc.perform(post("/apipersonas/auth/signin")
				.content(asJsonString(new LoginRequest("jperez", "admin1234")))
				.contentType(MediaType.APPLICATION_JSON)				
				.accept(MediaType.APPLICATION_JSON))
			//.andDo(MockMvcResultHandlers.print())
			.andExpect(status().isOk()).andReturn();				
				
		assertThat(result.getResponse().getContentAsString()).isNotEmpty();

		// Parsear la respuesta para obtener el token de acceso
		String jsonString = result.getResponse().getContentAsString();
		JsonMapper jsonMapper = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();

		JwtResponse jwtResponse = jsonMapper.readValue(jsonString, new TypeReference<JwtResponse>(){});
		
		// Invocar el servicio para obtener todas las personas
		this.mvc.perform(get("/apipersonas/personas")
					.contentType(MediaType.APPLICATION_JSON)				
					.accept(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer " + jwtResponse.getAccessToken())
				)
				.andExpect(status().isOk());
	}

	public static String asJsonString(final Object obj) {
	    try {
	        return new ObjectMapper().writeValueAsString(obj);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}