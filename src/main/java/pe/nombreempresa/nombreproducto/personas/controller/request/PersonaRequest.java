package pe.nombreempresa.nombreproducto.personas.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.nombreempresa.nombreproducto.personas.model.Persona;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonaRequest {	
	private String nombre;
	private String apellidos;
	private Integer edad;
	private String email;
	
	public PersonaRequest (Persona persona) {	
		super();
		this.nombre = persona.getNombre();
		this.apellidos = persona.getApellidos();
		this.edad = persona.getEdad();
		this.email = persona.getEmail();
	}
}
