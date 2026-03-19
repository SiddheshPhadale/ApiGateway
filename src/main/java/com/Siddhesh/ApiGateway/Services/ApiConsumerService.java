package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.RegisterApiConsumerDto;
import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import com.Siddhesh.ApiGateway.Entities.Role;
import com.Siddhesh.ApiGateway.Repositories.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Services.MapperService.ConsumerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiConsumerService {
    private final ConsumerMapper mapper;

    private final ApiConsumerRepo repo;

    public HttpStatus registerUser(RegisterApiConsumerDto consumerDto){
        ApiConsumer consumer = mapper.mapToApiConsumer(consumerDto);
        consumer.setRole(Role.CONSUMER);
        repo.save(consumer);
        return HttpStatus.CREATED;
    }
}
