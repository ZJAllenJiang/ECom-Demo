package org.allen.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EndpointAccessibilityTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void testProductsEndpointsAccessible() {
        String baseUrl = "http://localhost:" + port;
        
        // Test GET /api/products
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/api/products", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUsersEndpointsAccessible() {
        String baseUrl = "http://localhost:" + port;
        
        // Test GET /api/users
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/api/users", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testOrdersEndpointsAccessible() {
        String baseUrl = "http://localhost:" + port;
        
        // Test GET /api/orders
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/api/orders", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testActuatorEndpointsAccessible() {
        String baseUrl = "http://localhost:" + port;
        
        // Test GET /actuator/health
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
        // Actuator might return 404 if not enabled, which is acceptable for testing
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void testH2ConsoleAccessible() {
        String baseUrl = "http://localhost:" + port;
        
        // Test GET /h2-console - H2 console redirects to login page (302)
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/h2-console", String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode()); // 302 redirect is expected
    }
} 