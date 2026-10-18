package com.Siddhesh.ApiGateway.Api.Mapper;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T19:03:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ApiMapperImpl implements ApiMapper {

    @Override
    public ApiResponseDto mapToResponceApiDto(ApiBody apiBody) {
        if ( apiBody == null ) {
            return null;
        }

        ApiResponseDto apiResponseDto = new ApiResponseDto();

        apiResponseDto.setApiLink( apiBody.getLink() );
        if ( apiBody.getApiId() != null ) {
            apiResponseDto.setApiId( apiBody.getApiId() );
        }
        apiResponseDto.setApiName( apiBody.getApiName() );
        apiResponseDto.setApiDescription( apiBody.getApiDescription() );

        return apiResponseDto;
    }

    @Override
    public ApiResponseDto mapToApiRegistrationResponse(ApiBody apiBody) {
        if ( apiBody == null ) {
            return null;
        }

        ApiResponseDto apiResponseDto = new ApiResponseDto();

        apiResponseDto.setApiLink( apiBody.getLink() );
        if ( apiBody.getApiId() != null ) {
            apiResponseDto.setApiId( apiBody.getApiId() );
        }
        apiResponseDto.setApiName( apiBody.getApiName() );
        apiResponseDto.setApiDescription( apiBody.getApiDescription() );

        return apiResponseDto;
    }

    @Override
    public ApiBody mapToApiBody(ApiRegistrationBodyDto apiDto) {
        if ( apiDto == null ) {
            return null;
        }

        ApiBody apiBody = new ApiBody();

        apiBody.setApiName( apiDto.getApiName() );
        apiBody.setLink( apiDto.getLink() );
        apiBody.setApiDescription( apiDto.getApiDescription() );

        return apiBody;
    }
}
