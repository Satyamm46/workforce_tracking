package com.institute.workforce_tracking.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.institute.workforce_tracking.entity.User;

/**
 * Adapter that presents a domain {@link User} to Spring Security as a
 * {@link UserDetails}.
 *
 * <p>Spring Security only understands the {@code UserDetails} interface. Rather
 * than forcing our JPA entity to implement that framework interface (mixing
 * persistence and security concerns), this adapter WRAPS a {@code User} and
 * exposes exactly what Spring Security needs. The entity stays a clean domain
 * object; this class owns the security translation.</p>
 *
 * <p>This is the Adapter design pattern: converting one interface (our domain
 * {@code User}) into another that a client (Spring Security) expects.</p>
 */
public class SecurityUser implements UserDetails {

    /** The wrapped domain user. */
    private final User user;

    /**
     * @param user the authenticated domain user to adapt
     */
    public SecurityUser(User user) {
        this.user = user;
    }

    /**
     * Exposes the wrapped user so authenticated code can reach domain data
     * (id, full name, role) after authentication.
     *
     * @return the underlying domain user
     */
    public User getDomainUser() {
        return user;
    }

    /**
     * Translates the user's single {@link com.institute.workforce_tracking.enums.Role}
     * into Spring Security authorities, applying the conventional "ROLE_"
     * prefix that {@code hasRole(...)} checks expect.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Spring Security's "username" is our login identifier — the email.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * A disabled account cannot authenticate — delegates to the entity's flag.
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}