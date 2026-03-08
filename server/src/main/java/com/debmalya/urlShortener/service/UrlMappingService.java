package com.debmalya.urlShortener.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.web.util.InvalidUrlException;
import com.debmalya.urlShortener.CustomExceptions.ShortUrlGenerationException;

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
        String normalizedUrl = normalizeUrl(originalUrl);
        String shortUrl = generateShortUrl(normalizedUrl);
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(normalizedUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setUser(user);
        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);
        return convertToDTO(savedMapping);
    }

    private static final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    final int CODE_LENGTH = 7;
    final int MAX_ATTEMPTS = 5;
    public String generateShortUrl(String originalUrl) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            StringBuilder shortCode = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                int index = ThreadLocalRandom.current().nextInt(characters.length());
                shortCode.append(characters.charAt(index));
            }
            String code = shortCode.toString();
            if (!urlMappingRepository.existsByShortUrl(code)) return code;
        }

        throw new ShortUrlGenerationException("Failed to generate unique short URL");
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) throw new InvalidUrlException("URL cannot be empty");
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null ||
                (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidUrlException("Only HTTP/HTTPS URLs allowed");
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) throw new InvalidUrlException("Invalid URL host");
            
            host = host.toLowerCase();

            if (!host.contains(".")) {
                throw new InvalidUrlException("Invalid domain");
            }

            return uri.toString();
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format");
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

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping= urlMappingRepository.findByShortUrl(shortUrl);
        if(urlMapping != null){
            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);
            // Recording the click event
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setUrlMapping(urlMapping);
            clickEvent.setClickDate(LocalDateTime.now());
            clickEventRepository.save(clickEvent);
        }
        return urlMapping;
    }

}
