package com.mingleroom.security.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mingleroom.domain.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;


@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    @JsonIgnore
    private final User user;

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }


    public Long getId() {
        return user.getId();
    }

    public String getRole() {
        return user.getRoleGlobal().name();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public OffsetDateTime getCreatedAt() {
        return user.getCreatedAt();
    }

    public OffsetDateTime getUpdatedAt() {
        return user.getUpdatedAt();
    }

    public boolean getBanned() {
        return user.isBanned();
    }

    public String getProfileImg() {
        return user.getProfileImg();
    }

    public OffsetDateTime getLastLoginAt() {
        return user.getLastLoginAt();
    }

    public OffsetDateTime getPasswordUpdatedAt() {
        return user.getPasswordUpdatedAt();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.getRole()));
    }

    @Override
    public boolean isAccountNonExpired() {
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

    @Override
    public boolean isAccountNonLocked() {
        return !this.getBanned();
    }
}
