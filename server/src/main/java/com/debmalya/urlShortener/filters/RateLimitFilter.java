package com.debmalya.urlShortener.filters;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debmalya.urlShortener.service.RateLimitService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIP=getClientIP(request);
        Bucket tokenBucket = rateLimitService.resolveBucket(clientIP);
        var probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        if(probe.isConsumed()){
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        }else{
            var waitToRefill= probe.getNanosToWaitForRefill()/1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds",String.valueOf(waitToRefill));
            response.setContentType("application/json");
            
            String jsonResponse="""
                    {
                        "status":%s,
                        "error":"Too many requests",
                        "message":"Try again later !",
                        "retryAfterSeconds":%s
                    }
                    """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(), waitToRefill);
                    
            response.getWriter().write(jsonResponse);

            
        }
    }

    private String getClientIP(HttpServletRequest req){
        String xfHeader=req.getHeader("X-Forwarded-For");
        if(xfHeader==null || xfHeader.isEmpty()) return req.getRemoteAddr();
        return xfHeader.split(",")[0].trim();
    }


}
