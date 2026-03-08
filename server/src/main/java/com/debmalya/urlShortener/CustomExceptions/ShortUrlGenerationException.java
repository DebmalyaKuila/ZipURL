package com.debmalya.urlShortener.CustomExceptions;

public class ShortUrlGenerationException extends RuntimeException {
    public ShortUrlGenerationException(String message) {
        super(message);
    }
}