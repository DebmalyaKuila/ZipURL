package com.debmalya.urlShortener.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

import com.debmalya.urlShortener.models.User;
import com.debmalya.urlShortener.repository.UserRepository;
import com.debmalya.urlShortener.security.jwt.JwtAuthResponponse;
import com.debmalya.urlShortener.security.jwt.JwtUtils;
import com.debmalya.urlShortener.dtos.LoginRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    public User registerUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    public JwtAuthResponponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateJwtToken(userDetails);
        return new JwtAuthResponponse(token);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
