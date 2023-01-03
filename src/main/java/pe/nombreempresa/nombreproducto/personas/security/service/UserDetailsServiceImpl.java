package pe.nombreempresa.nombreproducto.personas.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.nombreempresa.nombreproducto.personas.security.model.User;
import pe.nombreempresa.nombreproducto.personas.security.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	//@Autowired
	private final UserRepository userRepository;
	
	private UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

		return UserDetailsImpl.build(user);
	}

}
