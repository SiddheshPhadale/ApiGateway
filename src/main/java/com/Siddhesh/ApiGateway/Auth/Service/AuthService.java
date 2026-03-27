package com.Siddhesh.ApiGateway.Auth.Service;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Dto.LoginDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegistrationResponseDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import com.Siddhesh.ApiGateway.Auth.Entity.Role;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Auth.Repository.BaseUserRepo;
import com.Siddhesh.ApiGateway.Auth.Mapper.ApiProviderMapper;
import com.Siddhesh.ApiGateway.Auth.Mapper.ConsumerMapper;
import com.Siddhesh.ApiGateway.Auth.Mapper.LoginMapper;
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
