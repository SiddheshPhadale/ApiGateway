package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T19:03:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ConsumerMapperImpl implements ConsumerMapper {

    @Override
    public ApiConsumer mapToApiConsumer(RegisterationRequestDto consumerDto) {
        if ( consumerDto == null ) {
            return null;
        }

        ApiConsumer apiConsumer = new ApiConsumer();

        apiConsumer.setUserName( consumerDto.getUserName() );
        apiConsumer.setUserEmail( consumerDto.getUserEmail() );
        apiConsumer.setPassword( consumerDto.getPassword() );

        return apiConsumer;
    }
}
