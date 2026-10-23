package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Api.Service.ApiServices;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Service.AuthService;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateApiNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ApiRegistrationTests {

    @Autowired
    private ApiServices apiServices;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApiRepo apiRepo;

    @BeforeEach
    void setup() {
        RegisterationRequestDto provider1 = new RegisterationRequestDto();
        provider1.setUserName("providerAlpha");
        provider1.setUserEmail("alpha@example.com");
        provider1.setPassword("Password123!");
        authService.registerProvider(provider1);

        RegisterationRequestDto provider2 = new RegisterationRequestDto();
        provider2.setUserName("providerBeta");
        provider2.setUserEmail("beta@example.com");
        provider2.setPassword("Password123!");
        authService.registerProvider(provider2);
    }

    @Test
    void testProviderCanRegisterApi() {
        ApiRegistrationBodyDto dto = new ApiRegistrationBodyDto();
        dto.setApiName("WeatherService");
        dto.setLink("https://api.weather.com/v1");
        dto.setApiDescription("Real-time weather reports");
        dto.setRateLimitPerMinute(60);

        ApiResponseDto response = apiServices.registerApi(dto, "providerAlpha");
        assertNotNull(response);
        assertEquals("WeatherService", response.getApiName());
        assertTrue(response.getApiId() > 0);

        List<ApiResponseDto> providerApis = apiServices.getAllApiByUsername("providerAlpha");
        assertEquals(1, providerApis.size());
        assertEquals("WeatherService", providerApis.get(0).getApiName());
    }

    @Test
    void testDuplicateApiRegistration_ThrowsException() {
        ApiRegistrationBodyDto dto = new ApiRegistrationBodyDto();
        dto.setApiName("StockQuotes");
        dto.setLink("https://api.stocks.com");
        dto.setApiDescription("Stocks API");
        dto.setRateLimitPerMinute(100);

        apiServices.registerApi(dto, "providerAlpha");

        ApiRegistrationBodyDto dupDto = new ApiRegistrationBodyDto();
        dupDto.setApiName("StockQuotes");
        dupDto.setLink("https://api.other.com");
        dupDto.setRateLimitPerMinute(50);

        assertThrows(DuplicateApiNameException.class, () -> apiServices.registerApi(dupDto, "providerBeta"));
    }

    @Test
    void testProviderCannotModifyAnotherProviderApi() {
        ApiRegistrationBodyDto dto = new ApiRegistrationBodyDto();
        dto.setApiName("CryptoFeed");
        dto.setLink("https://api.crypto.com");
        dto.setRateLimitPerMinute(120);

        ApiResponseDto created = apiServices.registerApi(dto, "providerAlpha");

        // providerBeta attempts to suspend providerAlpha's API -> Must be rejected with SecurityException
        assertThrows(SecurityException.class, () ->
                apiServices.updateApiStatus(created.getApiId(), Status.SUSPENDED, "providerBeta"));

        // providerAlpha can suspend their own API
        assertDoesNotThrow(() ->
                apiServices.updateApiStatus(created.getApiId(), Status.SUSPENDED, "providerAlpha"));

        ApiBody updated = apiRepo.findById(created.getApiId()).orElseThrow();
        assertEquals(Status.SUSPENDED, updated.getStatus());
    }
}
