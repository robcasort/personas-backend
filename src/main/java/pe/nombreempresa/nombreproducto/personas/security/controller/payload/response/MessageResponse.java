package pe.nombreempresa.nombreproducto.personas.security.controller.payload.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
	private Integer errorCode; // 0: no es error
	private String message;		
	
	public MessageResponse(String message) {
		this.errorCode = 0;
	    this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
}
