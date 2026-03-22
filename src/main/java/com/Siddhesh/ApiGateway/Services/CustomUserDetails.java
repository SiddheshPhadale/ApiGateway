package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.BaseUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
