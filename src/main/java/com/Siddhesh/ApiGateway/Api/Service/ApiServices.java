package com.Siddhesh.ApiGateway.Api.Service;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateApiNameException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Api.Mapper.ApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        ApiProvider apiProvider = userRepo.findByUserName(name);
        if(apiProvider != null){
            List<ApiBody> originalList = apiProvider.getApiList();
            return originalList.stream().map(ApiMapper::mapToResponceApiDto).toList();
        }else throw new UserNotFoundException("User is not present!!");
    }
}
