package pe.nombreempresa.nombreproducto.personas.security.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.request.LoginRequest;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.request.SignupRequest;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.request.TokenRefreshRequest;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.response.JwtResponse;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.response.MessageResponse;
import pe.nombreempresa.nombreproducto.personas.security.controller.payload.response.TokenRefreshResponse;
import pe.nombreempresa.nombreproducto.personas.security.exception.TokenLogoutException;
import pe.nombreempresa.nombreproducto.personas.security.exception.TokenRefreshException;
import pe.nombreempresa.nombreproducto.personas.security.exception.TokenSignUpException;
import pe.nombreempresa.nombreproducto.personas.security.jwt.JwtUtils;
import pe.nombreempresa.nombreproducto.personas.security.model.ERole;
import pe.nombreempresa.nombreproducto.personas.security.model.RefreshToken;
import pe.nombreempresa.nombreproducto.personas.security.model.Role;
import pe.nombreempresa.nombreproducto.personas.security.model.User;
import pe.nombreempresa.nombreproducto.personas.security.repository.RoleRepository;
import pe.nombreempresa.nombreproducto.personas.security.repository.UserRepository;
import pe.nombreempresa.nombreproducto.personas.security.service.RefreshTokenService;
import pe.nombreempresa.nombreproducto.personas.security.service.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value="/apipersonas/auth", produces = MediaType.APPLICATION_JSON_VALUE)
//@Slf4j
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	RefreshTokenService refreshTokenService;

	@Operation(summary = "Autenticarse al servicio",
			description = "Autenticarse al servicio",
			tags = {"auth"})
    	@ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            	description = "Datos de acceso para uso del servicio",
            	content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            	schema = @Schema(implementation = JwtResponse.class))}),
            @ApiResponse(responseCode = "400",
            description = "Datos de entrada incorrectos",
            content = @Content),
            @ApiResponse(responseCode = "401",
            description = "Credenciales no validas",
            content = @Content)
    })
	@PostMapping("/signin")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		
		String jwt = jwtUtils.generateJwtToken(userDetails);

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());
		
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());		
				
		return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
				userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@Operation(summary = "Agregar un usuario",
			description = "Agregar un usuario",
			tags = {"auth"})
    	@ApiResponses(value = {
            @ApiResponse(responseCode = "201",
            	description = "Mensaje de respuesta de la accion realizada",
            	content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            	schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400",
            description = "Datos de entrada incorrectos",
            content = @Content),
            @ApiResponse(responseCode = "424",
            description = "Algunos datos internos no estan completos",
            content = @Content)
    })
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse(1, "Error: El nombre de usuario ya esta en uso!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse(1, "Error: El correo electronico ya esta en uso!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				passwordEncoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new TokenSignUpException(ERole.ROLE_USER.toString(), 
							"No se encuentra el rol en la base de datos!"));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				String srole = role.toUpperCase();
				switch (srole) {
				case "ADMIN":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new TokenSignUpException(ERole.ROLE_ADMIN.toString(), 
									"No se encuentra el rol en la base de datos!"));
					roles.add(adminRole);

					break;
				case "MODERATOR":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new TokenSignUpException(ERole.ROLE_MODERATOR.toString(), 
									"No se encuentra el rol en la base de datos!"));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new TokenSignUpException(ERole.ROLE_USER.toString(), 
									"No se encuentra el rol en la base de datos!"));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);
		
		return new ResponseEntity<>(new MessageResponse("Usuario registrado con exito!"), HttpStatus.CREATED);
	}

	@Operation(summary = "Solicitar nuevo token con el refresh token",
			description = "Solicitar nuevo token con el refresh token",
			tags = {"auth"})
    	@ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            	description = "Datos de acceso para uso del servicio",
            	content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            	schema = @Schema(implementation = TokenRefreshResponse.class))}),
            @ApiResponse(responseCode = "400",
            description = "Datos de entrada incorrectos",
            content = @Content),
            @ApiResponse(responseCode = "401",
            description = "Credenciales no validas",
            content = @Content),
            @ApiResponse(responseCode = "403",
            description = "Acceso restringido",
            content = @Content)
    })
	@PostMapping("/refreshtoken")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
		String requestRefreshToken = request.getRefreshToken();
			
		return refreshTokenService.findByToken(requestRefreshToken)
				.map(refreshTokenService::verifyExpiration)
				.map(RefreshToken::getUser)
				.map(user -> {
					String token = jwtUtils.generateTokenFromUsername(user.getUsername());
					return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
				})
				.orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Token de actualizacion no se encuentra en la base de datos!"));			
	}

	@Operation(summary = "Solicitar la finalizacion del acceso al api",
			description = "Solicitar la finalizacion del acceso al api",
			tags = {"auth"})
    	@ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            	description = "Mensaje de respuesta de la accion realizada",
            	content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            	schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400",
            description = "Datos de entrada incorrectos",
            content = @Content),
            @ApiResponse(responseCode = "401",
            description = "Credenciales no validas",
            content = @Content)            
    })
	@PostMapping("/signout")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> logoutUser() {
		UserDetailsImpl userDetails;
		try {
			userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		} catch (Exception e) {
			throw new TokenLogoutException(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(), 
					"El token no es valido o ya expiro!");
		}
		String userId = userDetails.getId();
		refreshTokenService.deleteByUserId(userId);
		return ResponseEntity.ok(new MessageResponse("Cierre de sesion exitoso!"));
	}

}
