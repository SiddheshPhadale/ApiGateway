package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginDto;
import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegistrationResponseDto;
import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import com.Siddhesh.ApiGateway.Auth.Entity.Role;
import com.Siddhesh.ApiGateway.Auth.Repository.BaseUserRepo;
import com.Siddhesh.ApiGateway.Auth.Service.AuthService;
import com.Siddhesh.ApiGateway.Auth.Service.CustomUserDetails;
import com.Siddhesh.ApiGateway.Auth.Service.JwtService;
import com.Siddhesh.ApiGateway.Exceptions.UserAlreadyExistsException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthAndSecurityTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private BaseUserRepo baseUserRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void testConsumerRegistrationAndLogin_Success() {
        RegisterationRequestDto dto = new RegisterationRequestDto();
        dto.setUserName("testConsumer1");
        dto.setUserEmail("consumer1@example.com");
        dto.setPassword("SecretPass123!");

        RegistrationResponseDto regResponse = authService.registerConsumer(dto);
        assertNotNull(regResponse);
        assertEquals("testConsumer1", regResponse.getUserName());
        assertEquals(Role.CONSUMER, regResponse.getRole());

        // Verify password hashing
        BaseUser savedUser = baseUserRepo.findByUserName("testConsumer1");
        assertNotNull(savedUser);
        assertNotEquals("SecretPass123!", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("SecretPass123!", savedUser.getPassword()));

        // Login
        LoginDto loginDto = new LoginDto();
        loginDto.setUserName("testConsumer1");
        loginDto.setPassword("SecretPass123!");

        LoginResponseDto loginResponse = authService.login(loginDto);
        assertNotNull(loginResponse);
        assertEquals("testConsumer1", loginResponse.getUserName());
        assertEquals(Role.CONSUMER, loginResponse.getRole());
        assertNotNull(loginResponse.getJwtToken());
        assertTrue(jwtService.isTokenValid(loginResponse.getJwtToken(), new CustomUserDetails(savedUser)));
    }

    @Test
    void testProviderRegistration_ReturnsProviderRole() {
        RegisterationRequestDto dto = new RegisterationRequestDto();
        dto.setUserName("testProvider1");
        dto.setUserEmail("provider1@example.com");
        dto.setPassword("SecretPass123!");

        RegistrationResponseDto regResponse = authService.registerProvider(dto);
        assertNotNull(regResponse);
        assertEquals("testProvider1", regResponse.getUserName());
        assertEquals(Role.PROVIDER, regResponse.getRole());

        BaseUser savedUser = baseUserRepo.findByUserName("testProvider1");
        assertNotNull(savedUser);
        assertEquals(Role.PROVIDER, savedUser.getRole());
    }

    @Test
    void testDuplicateUsername_ThrowsConflictException() {
        RegisterationRequestDto dto = new RegisterationRequestDto();
        dto.setUserName("dupUser");
        dto.setUserEmail("dup1@example.com");
        dto.setPassword("SecretPass123!");
        authService.registerConsumer(dto);

        RegisterationRequestDto dupDto = new RegisterationRequestDto();
        dupDto.setUserName("dupUser");
        dupDto.setUserEmail("dup2@example.com");
        dupDto.setPassword("SecretPass123!");

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerConsumer(dupDto));
    }

    @Test
    void testDuplicateEmail_ThrowsConflictException() {
        RegisterationRequestDto dto = new RegisterationRequestDto();
        dto.setUserName("userA");
        dto.setUserEmail("same@example.com");
        dto.setPassword("SecretPass123!");
        authService.registerConsumer(dto);

        RegisterationRequestDto dupDto = new RegisterationRequestDto();
        dupDto.setUserName("userB");
        dupDto.setUserEmail("same@example.com");
        dupDto.setPassword("SecretPass123!");

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerProvider(dupDto));
    }

    @Test
    void testInvalidAndExpiredJwtHandling() {
        BaseUser user = new BaseUser();
        user.setUserName("jwtUser");
        user.setUserEmail("jwt@example.com");
        user.setPassword("hashed");
        user.setRole(Role.CONSUMER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Malformed token
        assertFalse(jwtService.isTokenValid("invalid.malformed.token", userDetails));

        // Expired token (created with past expiration)
        byte[] keyBytes = Decoders.BASE64.decode("7fK29xLmPqR8vNz4YtH3sBc91QaWmXe5JuDkLp0RsTyU");
        String expiredToken = Jwts.builder()
                .subject("jwtUser")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }
}
