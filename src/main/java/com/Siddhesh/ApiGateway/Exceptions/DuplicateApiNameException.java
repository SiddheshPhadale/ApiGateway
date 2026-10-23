package com.Siddhesh.ApiGateway.Exceptions;

public class DuplicateApiNameException extends RuntimeException{
    public DuplicateApiNameException(String msg){
        super(msg);
    }
}
