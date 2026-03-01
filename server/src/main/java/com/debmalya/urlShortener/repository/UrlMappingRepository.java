package com.debmalya.urlShortener.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debmalya.urlShortener.models.UrlMapping;
import com.debmalya.urlShortener.models.User;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    UrlMapping findByShortUrl(String shortUrl);
    List<UrlMapping> findByUser(User user);
    boolean existsByShortUrl(String shortUrl);
    
}
