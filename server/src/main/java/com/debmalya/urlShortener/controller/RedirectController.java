package com.debmalya.urlShortener.controller;

import org.springframework.web.bind.annotation.RestController;

import com.debmalya.urlShortener.models.UrlMapping;
import com.debmalya.urlShortener.service.UrlMappingService;

import lombok.AllArgsConstructor;

import java.net.URI;

import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@AllArgsConstructor
public class RedirectController {

    private UrlMappingService urlMappingService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortUrl) {
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);
        if (urlMapping == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Location", urlMapping.getOriginalUrl());
        return ResponseEntity
            .status(302)
            .location(URI.create(urlMapping.getOriginalUrl()))
            .build();
    }

}
