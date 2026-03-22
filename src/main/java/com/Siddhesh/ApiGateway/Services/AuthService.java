package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Dto.LoginDto;
import com.Siddhesh.ApiGateway.Dto.RegistrationResponseDto;
import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import com.Siddhesh.ApiGateway.Entities.BaseUser;
import com.Siddhesh.ApiGateway.Entities.Role;
import com.Siddhesh.ApiGateway.Repositories.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Repositories.BaseUserRepo;
import com.Siddhesh.ApiGateway.Services.MapperService.ApiProviderMapper;
import com.Siddhesh.ApiGateway.Services.MapperService.ConsumerMapper;
import com.Siddhesh.ApiGateway.Services.MapperService.LoginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final BaseUserRepo baseUserRepo;

    private final ApiOwnerRepo userRepo;

    private final LoginMapper loginMapper;

    private final ApiProviderMapper providerMapper;

    private final ConsumerMapper consumerMapper;

    private final ApiConsumerRepo consumerRepo;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public LoginResponseDto login(LoginDto loginDto){
        BaseUser baseUser = baseUserRepo.findByUserName(loginDto.getUserName());
        LoginResponseDto responseDto = loginMapper.mapToLoginResponseDto(baseUser);
        responseDto.setJwtToken(jwtService.generateToken(responseDto.getUserName(), responseDto.getRole()));
        return responseDto;
    }

    public RegistrationResponseDto registerConsumer(RegisterationRequestDto consumerDto){
        ApiConsumer consumer = consumerMapper.mapToApiConsumer(consumerDto);
        consumer.setRole(Role.CONSUMER);
        consumer.setPassword(passwordEncoder.encode(consumerDto.getPassword()));
        ApiConsumer savedConsumer = consumerRepo.save(consumer);
        RegistrationResponseDto responseDto = new RegistrationResponseDto();
        responseDto.setUserName(savedConsumer.getUserName());
        responseDto.setRole(Role.CONSUMER);
        responseDto.setMsg("Account created with USERNAME : " + responseDto.getUserName());
        return responseDto;
    }

    public RegistrationResponseDto registerProvider(RegisterationRequestDto userDto){
        ApiProvider apiProvider = providerMapper.mapToApiProvider(userDto);
        apiProvider.setRole(Role.PROVIDER);
        apiProvider.setPassword(passwordEncoder.encode(userDto.getPassword()));
        ApiProvider savedProvider = userRepo.save(apiProvider);
        RegistrationResponseDto responseDto = new RegistrationResponseDto();
        responseDto.setUserName(savedProvider.getUserName());
        responseDto.setRole(Role.CONSUMER);
        responseDto.setMsg("Account created with USERNAME : " + responseDto.getUserName());
        return responseDto;
    }
}
