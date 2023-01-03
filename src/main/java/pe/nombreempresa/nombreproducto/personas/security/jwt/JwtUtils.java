package pe.nombreempresa.nombreproducto.personas.security.jwt;

import java.io.File;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import pe.nombreempresa.nombreproducto.personas.security.service.UserDetailsImpl;

@Component
@Slf4j
public class JwtUtils {
	//private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Autowired
	ResourceLoader resourceLoader;
	
	@Value("${jwt.app.jwtExpirationMs}")
	private Long jwtExpirationMs;
	
	@Value("${jwt.rsa.public-key}")
	private String publicKeyLocation;
	
	@Value("${jwt.rsa.private-key}")
	private String privateKeyLocation;
	
	private RSAPublicKey publicKey;
	private RSAPrivateKey privateKey;
		
	@Bean
	void loadKeys() throws Exception {			
		publicKey = readPublicKey(resourceLoader.getResource(publicKeyLocation).getFile());
		privateKey = readPrivateKey(resourceLoader.getResource(privateKeyLocation).getFile());									
	}
	
	private RSAPublicKey readPublicKey(File file) throws Exception {
		KeyFactory factory = KeyFactory.getInstance("RSA");

		try (FileReader keyReader = new FileReader(file); 
				PemReader pemReader = new PemReader(keyReader)) {

			PemObject pemObject = pemReader.readPemObject();
			byte[] content = pemObject.getContent();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
			return (RSAPublicKey) factory.generatePublic(pubKeySpec);
		}
	}
	
	private RSAPrivateKey readPrivateKey(File file) throws Exception {
	    KeyFactory factory = KeyFactory.getInstance("RSA");

	    try (FileReader keyReader = new FileReader(file);
	      PemReader pemReader = new PemReader(keyReader)) {

	        PemObject pemObject = pemReader.readPemObject();
	        byte[] content = pemObject.getContent();
	        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
	        return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
	    }
	}
	
	public String generateJwtToken(UserDetailsImpl userPrincipal) {
	    return generateTokenFromUsername(userPrincipal.getUsername());
	}

	public String generateTokenFromUsername(String username) {
	    return Jwts.builder()
	    		.setSubject(username).setIssuedAt(new Date())
	    		.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
	    		.signWith(privateKey, SignatureAlgorithm.RS512)
	    		.compact();
	}
	
	public String generateJwtToken(Authentication authentication) {
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		return Jwts.builder()
				.setSubject((userPrincipal.getUsername()))
				.setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(privateKey, SignatureAlgorithm.RS512)
				//.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		//return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
		return Jwts.parserBuilder()
				.setSigningKey(publicKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			//Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(authToken);
			return true;		
		}  catch (SignatureException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());			
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}
}
