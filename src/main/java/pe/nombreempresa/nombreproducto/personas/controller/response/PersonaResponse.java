package pe.nombreempresa.nombreproducto.personas.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.nombreempresa.nombreproducto.personas.model.Persona;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonaResponse {
	private String id;
	private String nombre;
	private String apellidos;
	private Integer edad;
	private String email;
	
	public PersonaResponse (Persona persona) {
		super();
		this.id = persona.getId();
		this.nombre = persona.getNombre();
		this.apellidos = persona.getApellidos();
		this.edad = persona.getEdad();
		this.email = persona.getEmail();
	}
}
