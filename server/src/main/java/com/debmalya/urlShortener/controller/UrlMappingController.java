package com.debmalya.urlShortener.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debmalya.urlShortener.models.User;
import com.debmalya.urlShortener.service.UrlMappingService;
import com.debmalya.urlShortener.service.UserService;
import com.debmalya.urlShortener.dtos.UrlMappingDTO;

import lombok.AllArgsConstructor;

import java.security.Principal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl( @RequestBody Map<String, String> request, Principal principal) {
        String originalUrl = request.get("originalUrl");
        String username = principal.getName();
        User user = userService.findByUsername(username);
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUrls (Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }
}
