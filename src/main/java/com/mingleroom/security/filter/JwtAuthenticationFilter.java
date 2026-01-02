package com.mingleroom.security.filter;

import com.mingleroom.security.CustomUserDetailsService;
import com.mingleroom.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String jwt = req.getHeader("Authorization");
        log.info(req.getRequestURI());
        if (jwt != null) {
            jwt = jwt.replace("Bearer ", "").trim();
            try {
                Claims claims = jwtProvider.parse(jwt).getPayload();
                String email = claims.getSubject();

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch (ExpiredJwtException e) {
                request.setAttribute("AUTH_ERROR_CODE", "TOKEN_EXPIRED");
                request.setAttribute("AUTH_ERROR_MESSAGE", "액세스 토큰이 만료되었습니다.");
                SecurityContextHolder.clearContext();
            }
            catch (JwtException e) {
                request.setAttribute("AUTH_ERROR_CODE", "TOKEN_INVALID");
                request.setAttribute("AUTH_ERROR_MESSAGE", "유효하지 않은 토큰입니다.");
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
