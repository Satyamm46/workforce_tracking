package com.institute.workforce_tracking.security;

import com.institute.workforce_tracking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security's authentication to this application's user store.
 *
 * <p>Spring Security calls {@link #loadUserByUsername(String)} whenever it needs
 * to look up an account during authentication. This implementation loads the
 * {@link com.institute.workforce_tracking.entity.User} by email via
 * {@link UserRepository} and wraps it in a {@link SecurityUser} adapter — the
 * only piece of "how to find our users" that the framework needs.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @param userRepository the repository used to look up users by email
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their login identifier (email) for Spring Security.
     *
     * <p>The parameter is named {@code username} by the interface, but in this
     * application the login identifier is the email address.</p>
     *
     * @param username the email address to authenticate
     * @return a {@link SecurityUser} adapting the found domain user
     * @throws UsernameNotFoundException if no user has that email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(SecurityUser::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found with email: " + username));
    }
}