package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiProviderDto;
import com.Siddhesh.ApiGateway.Dto.RegisterApiConsumerDto;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import com.Siddhesh.ApiGateway.Entities.Role;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Services.MapperService.ApiProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiProviderService {

    private final ApiOwnerRepo userRepo;

    private final ApiProviderMapper mapper;

    public HttpStatus registerUser(ApiProviderDto userDto){
        ApiProvider apiProvider = mapper.mapToUser(userDto);
        apiProvider.setRole(Role.PROVIDER);
        userRepo.save(apiProvider);
        return HttpStatus.CREATED;
    }
}
