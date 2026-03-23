package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.BaseUser;
import com.Siddhesh.ApiGateway.Repositories.BaseUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
