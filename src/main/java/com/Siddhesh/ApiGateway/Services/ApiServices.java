package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ResponceApiDto;
import com.Siddhesh.ApiGateway.Entities.ApiBody;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
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

    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public ResponceApiDto registerApi(ApiRegistrationBodyDto apiDto, String userName){
            ApiProvider apiProvider = userRepo.findByUserName(userName);
            if (apiProvider != null){
                ApiBody body = ApiMapper.mapToApiBody(apiDto);
                body.setApiKey("gw_" + generateApiKey());
                ApiBody apiBody = apiRepo.save(body);
                apiProvider.getApiList().add(apiBody);
                userRepo.save(apiProvider);
                return ApiMapper.mapToApiRegistrationResponse(apiBody);
            }
            else throw new UserNotFoundException("User with username : " + userName + " not found.");
    }

    public List<ResponceApiDto> getAllApiByUsername(String name){
        ApiProvider apiProvider = userRepo.findByUserName(name);
        if(apiProvider != null){
            List<ApiBody> originalList = apiProvider.getApiList();
            return originalList.stream().map(ApiMapper::mapToResponceApiDto).toList();
        }else throw new UserNotFoundException("User is not present!!");
    }

    private String generateApiKey(){
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
