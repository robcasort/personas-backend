package pe.nombreempresa.nombreproducto.personas.security.controller.advice;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import pe.nombreempresa.nombreproducto.personas.security.exception.TokenLogoutException;
import pe.nombreempresa.nombreproducto.personas.security.exception.TokenRefreshException;
import pe.nombreempresa.nombreproducto.personas.security.exception.TokenSignUpException;

@RestControllerAdvice
public class TokenControllerAdvice {

  @ExceptionHandler(value = TokenRefreshException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorMessage handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.FORBIDDEN.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }
  
  @ExceptionHandler(value = TokenLogoutException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorMessage handleTokenLogoutException(TokenLogoutException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.BAD_REQUEST.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }
  
  @ExceptionHandler(value = TokenSignUpException.class)
  @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
  public ErrorMessage handleTokenLogoutException(TokenSignUpException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.FAILED_DEPENDENCY.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }
}
