package com.debmalya.urlShortener.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import java.util.Map;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.debmalya.urlShortener.dtos.UrlMappingDTO;
import com.debmalya.urlShortener.models.UrlMapping;
import com.debmalya.urlShortener.models.User;
import com.debmalya.urlShortener.repository.ClickEventRepository;
import com.debmalya.urlShortener.repository.UrlMappingRepository;
import com.debmalya.urlShortener.dtos.ClickEventDTO;
import com.debmalya.urlShortener.models.ClickEvent;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UrlMappingService {
    private UrlMappingRepository urlMappingRepository;
    private ClickEventRepository clickEventRepository;

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl(originalUrl);
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setUser(user);
        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);
        return convertToDTO(savedMapping);
    }

    public String generateShortUrl(String originalUrl) {
        final int CODE_LENGTH = 7;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        while (true) {
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
        shortCode.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        if (!urlMappingRepository.existsByShortUrl(shortCode.toString())) {
        return shortCode.toString();
        }
        }
    }

    private UrlMappingDTO convertToDTO(UrlMapping urlMapping) {
        UrlMappingDTO dto = new UrlMappingDTO();
        dto.setId(urlMapping.getId());
        dto.setOriginalUrl(urlMapping.getOriginalUrl());
        dto.setShortUrl(urlMapping.getShortUrl());
        dto.setClickCount(urlMapping.getClickCount());
        dto.setCreatedAt(urlMapping.getCreatedAt());
        dto.setUserName(urlMapping.getUser().getUsername());
        return dto;
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        return urlMappings.stream().map(this::convertToDTO).toList();
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime startDate, LocalDateTime endDate) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping==null) return null;
        List<ClickEventDTO> clickEvents = clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, startDate, endDate)
        .stream()
        .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()))
        .entrySet().stream()
        .map(event -> {
                    ClickEventDTO dto = new ClickEventDTO();
                    dto.setClickDate(event.getKey());
                    dto.setCount(event.getValue());
                    return dto;
                })
        .collect(Collectors.toList());
        return clickEvents;
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate startDate, LocalDate endDate) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        if(urlMappings.isEmpty()) return null;
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMappings, startDate.atStartOfDay(), endDate.plusDays((1)).atStartOfDay());
        Map<LocalDate, Long> clicksByDate = clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()));
        return clicksByDate;
    }

}
