package pe.nombreempresa.nombreproducto.personas.security.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import pe.nombreempresa.nombreproducto.personas.security.model.RefreshToken;
import pe.nombreempresa.nombreproducto.personas.security.model.User;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	int deleteByUser(User user);
}
