package com.unble.budget.config;

import com.unble.budget.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtRequestFilter(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                // 토큰이 비어있거나 형식이 잘못된 경우 체크
                if (jwtToken == null || jwtToken.trim().isEmpty()) {
                    logger.warn("JWT Token is empty or null");
                } else {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                }
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                logger.warn("JWT Token is malformed: " + e.getMessage());
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                logger.warn("JWT Token is expired: " + e.getMessage());
            } catch (io.jsonwebtoken.UnsupportedJwtException e) {
                logger.warn("JWT Token is unsupported: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.warn("JWT claims string is empty: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Unable to get JWT Token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userService.loadUserByUsername(username);

            if (jwtTokenUtil.validateToken(jwtToken, userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                        
                usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}