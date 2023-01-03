package pe.nombreempresa.nombreproducto.personas.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class TokenSignUpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TokenSignUpException(String token, String message) {
		super(String.format("Failed for [%s]: %s", token, message));
	}
}
