package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiProviderDto;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiProviderService {

    private final ApiOwnerRepo userRepo;

    private final ApiProviderMapper ApiMapper;

    public HttpStatus registerUser(ApiProviderDto userDto){
        ApiProvider apiProvider = ApiMapper.mapToUser(userDto);
        userRepo.save(apiProvider);
        return HttpStatus.CREATED;
    }
}
