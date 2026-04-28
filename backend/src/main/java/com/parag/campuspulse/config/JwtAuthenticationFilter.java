package com.parag.campuspulse.config;

import com.parag.campuspulse.model.User;
import com.parag.campuspulse.repository.UserRepository;
import com.parag.campuspulse.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ Allow preflight (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ✅ Skip auth endpoints
        if (path != null && path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // ✅ Extract token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
            } catch (Exception e) {
                logger.warn("Invalid JWT: " + e.getMessage());
            }
        }

        // ✅ Validate token
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && jwtUtil.validateToken(jwt, email)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                                )
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ✅ SET AUTH (ONLY ONCE)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}