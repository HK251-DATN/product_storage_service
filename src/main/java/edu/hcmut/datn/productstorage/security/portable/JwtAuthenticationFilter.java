package edu.hcmut.datn.productstorage.security.portable;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
//        log.info(authHeader);

        if (authHeader != null) {
            String token = authHeader;

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            try {
                Claims claims = jwtTokenValidator.validateToken(token).getBody();

                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) claims.get("permissions");
                Long requesterId = claims.get("userId", Long.class);
                String requesterEmail = claims.get("userEmail", String.class);

                List<SimpleGrantedAuthority> authorities = List.of();

                if (permissions != null) {
                    authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();
                }

                AuthenticatedUser requester = new AuthenticatedUser(requesterId, requesterEmail);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        requester, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (Exception e) {
                log.info(e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String json = "{\"message\":\"Unauthorized: token invalid or expired\"}";
                response.getWriter().write(json);
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
