package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Gateway.Service.UrlSecurityValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProxyServiceTests {

    @Autowired
    private UrlSecurityValidator urlValidator;

    @Test
    void testUrlSecurityValidator_BlocksInvalidSchemes() {
        assertThrows(IllegalArgumentException.class, () -> urlValidator.validateUrl("ftp://api.example.com/data"));
        assertThrows(IllegalArgumentException.class, () -> urlValidator.validateUrl("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> urlValidator.validateUrl("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class, () -> urlValidator.validateUrl(""));
        assertThrows(IllegalArgumentException.class, () -> urlValidator.validateUrl(null));
    }

    @Test
    void testUrlSecurityValidator_BlocksSsrfAndPrivateAddresses() {
        UrlSecurityValidator strictValidator = new UrlSecurityValidator();
        strictValidator.setAllowPrivateIps(false);

        // Localhost and metadata
        assertThrows(IllegalArgumentException.class, () -> strictValidator.validateUrl("http://localhost:8080/admin"));
        assertThrows(IllegalArgumentException.class, () -> strictValidator.validateUrl("http://test.localhost/api"));
        assertThrows(IllegalArgumentException.class, () -> strictValidator.validateUrl("http://metadata.google.internal/computeMetadata/v1/"));

        // Loopback IP
        assertThrows(IllegalArgumentException.class, () -> strictValidator.validateUrl("http://127.0.0.1:8080"));

        // Valid external HTTPS URL
        assertDoesNotThrow(() -> strictValidator.validateUrl("https://httpbin.org/get"));
    }

    @Test
    void testBlockedAddressesDetection() throws Exception {
        UrlSecurityValidator strictValidator = new UrlSecurityValidator();
        strictValidator.setAllowPrivateIps(false);

        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        assertTrue(strictValidator.isBlockedAddress(loopback));

        InetAddress privateIp = InetAddress.getByName("10.0.0.1");
        assertTrue(strictValidator.isBlockedAddress(privateIp));

        InetAddress linkLocal = InetAddress.getByName("169.254.169.254");
        assertTrue(strictValidator.isBlockedAddress(linkLocal));
    }
}
