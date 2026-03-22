package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Entities.ApiBody;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import com.Siddhesh.ApiGateway.Entities.Status;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateApiNameException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Repositories.ApiRepo;
import com.Siddhesh.ApiGateway.Services.MapperService.ApiMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiServices {

    private final ApiRepo apiRepo;

    private final ApiOwnerRepo userRepo;

    private final ApiMapper ApiMapper;

    @Transactional
    public ApiResponseDto registerApi(ApiRegistrationBodyDto apiDto, String userName){
            ApiProvider apiProvider = userRepo.findByUserName(userName);
            if (apiProvider != null){
                boolean isDuplicate = apiProvider.getApiList()
                        .stream()
                        .anyMatch(
                                api -> api.getApiName().equalsIgnoreCase(apiDto.getApiName())
                        );

                if (isDuplicate){
                    throw new DuplicateApiNameException("Api with the name : " + apiDto.getApiName() +" already exists.");
                }
                ApiBody body = ApiMapper.mapToApiBody(apiDto);
                body.setStatus(Status.ACTIVE);
                ApiBody apiBody = apiRepo.save(body);
                apiProvider.getApiList().add(apiBody);
                userRepo.save(apiProvider);
                return ApiMapper.mapToApiRegistrationResponse(apiBody);
            }
            else throw new UserNotFoundException("User with username : " + userName + " not found.");
    }

    @Transactional
    public List<ApiResponseDto> getAllApiByUsername(String name){
        System.out.println("in service");
        ApiProvider apiProvider = userRepo.findByUserName(name);
        if(apiProvider != null){
            List<ApiBody> originalList = apiProvider.getApiList();
            return originalList.stream().map(ApiMapper::mapToResponceApiDto).toList();
        }else throw new UserNotFoundException("User is not present!!");
    }
}
