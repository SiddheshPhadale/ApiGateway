package com.Siddhesh.ApiGateway.Auth.Service;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import com.Siddhesh.ApiGateway.Auth.Repository.BaseUserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private BaseUserRepo repo;

    public CustomUserDetailService(BaseUserRepo repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BaseUser baseUser = repo.findByUserName(username);
        if (baseUser != null) return new CustomUserDetails(baseUser);
        throw UsernameNotFoundException.fromUsername(username);
    }
}
