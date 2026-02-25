package com.debmalya.urlShortener.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
