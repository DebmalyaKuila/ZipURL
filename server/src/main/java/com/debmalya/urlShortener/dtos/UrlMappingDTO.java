package com.debmalya.urlShortener.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UrlMappingDTO {
    private Long id;
    private String originalUrl;
    private String shortUrl;
    private int clickCount;
    private LocalDateTime createdAt;
    private String userName;
}
