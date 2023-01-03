package pe.nombreempresa.nombreproducto.personas.security.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import pe.nombreempresa.nombreproducto.personas.security.model.ERole;
import pe.nombreempresa.nombreproducto.personas.security.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
	Optional<Role> findByName(ERole name);
}
