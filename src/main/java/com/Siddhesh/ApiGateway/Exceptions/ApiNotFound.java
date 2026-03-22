package com.Siddhesh.ApiGateway.Exceptions;

public class ApiNotFound extends RuntimeException {
    public ApiNotFound(String message) {
        super(message);
    }
}
