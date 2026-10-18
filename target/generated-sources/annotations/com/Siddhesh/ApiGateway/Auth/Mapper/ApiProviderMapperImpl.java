package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T19:03:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ApiProviderMapperImpl implements ApiProviderMapper {

    @Override
    public ApiProvider mapToApiProvider(RegisterationRequestDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        ApiProvider apiProvider = new ApiProvider();

        apiProvider.setUserName( userDto.getUserName() );
        apiProvider.setUserEmail( userDto.getUserEmail() );
        apiProvider.setPassword( userDto.getPassword() );

        return apiProvider;
    }
}
