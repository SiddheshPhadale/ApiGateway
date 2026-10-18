package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T19:03:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoginMapperImpl implements LoginMapper {

    @Override
    public LoginResponseDto mapToLoginResponseDto(BaseUser baseUser) {
        if ( baseUser == null ) {
            return null;
        }

        LoginResponseDto loginResponseDto = new LoginResponseDto();

        loginResponseDto.setUserName( baseUser.getUserName() );
        loginResponseDto.setRole( baseUser.getRole() );

        return loginResponseDto;
    }
}
