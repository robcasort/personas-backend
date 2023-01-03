package pe.nombreempresa.nombreproducto.personas.service;

import java.util.List;

import pe.nombreempresa.nombreproducto.personas.model.Persona;

public interface IPersonaService {
	public List<Persona> findAll();
	
	public Persona findById(String id);
	
	public Persona save(Persona producto);
	
	public void deleteById(String id);
}
