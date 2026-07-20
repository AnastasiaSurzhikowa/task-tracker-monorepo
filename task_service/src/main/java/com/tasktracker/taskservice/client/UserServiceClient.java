package com.tasktracker.taskservice.client;

import com.tasktracker.taskservice.exception.UserServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

@Component
public class UserServiceClient {
    private final RestClient restClient;

    public UserServiceClient(RestClient userServiceRestClient) {
        this.restClient = userServiceRestClient;
    }

    public boolean userExists(Long userId) {
        try {
            restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (ResourceAccessException e) {
            throw new UserServiceUnavailableException("User service is not reachable: " + e.getMessage());
        }
    }
}