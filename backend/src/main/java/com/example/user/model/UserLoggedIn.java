package com.example.user.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * <b>UserLoggedIn</b><br>
 * 
 * Class that stores information about the authenticated user that Spring Security needs.
 * It implements UserDetails interface.
 */
public class UserLoggedIn implements UserDetails {

    private final Long idUser;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserLoggedIn(
            Long idUser,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities) {

        this.idUser = idUser;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public Long getIdUser() {
        return idUser;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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

    @Override
    public boolean isEnabled() {
        return true;
    }
}

