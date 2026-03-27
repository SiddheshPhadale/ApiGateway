package com.Siddhesh.ApiGateway.Auth.Service;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private BaseUser baseUser;

    public CustomUserDetails(BaseUser baseUser){
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(baseUser.getRole().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return baseUser.getPassword();
    }

    @Override
    public String getUsername() {
        return baseUser.getUserName();
    }
}
