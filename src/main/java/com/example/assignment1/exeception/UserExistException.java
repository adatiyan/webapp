package com.example.assignment1.exeception;

public class UserExistException extends Exception{

    public UserExistException(String message) {
        super(message);
    }
}
