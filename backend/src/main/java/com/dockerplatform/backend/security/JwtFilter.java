package com.dockerplatform.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

//        if (request.getRequestURL().toString().contains("**")){

//            System.out.println("---- Method:" +request.getMethod()+"  URL: "+request.getRequestURL());
//            System.out.println("---- Authrization: " + request.getHeader("Authorization"));
            String header = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;

            if (header != null && header.contains("Bearer")){
                jwtToken = header.substring(header.indexOf("Bearer")+7);
//                System.out.println(">>JWT TOKEN: " + jwtToken);

                try{
                    username = jwtService.getUsernameFormToken(jwtToken);
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if(jwtService.validateToken(jwtToken,userDetails)){
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                        System.out.println("Username: " + userDetails.getUsername() + ", role: " + userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }catch (IllegalAccessError e){
                    System.out.println("Unable to get JWT Token.");
                } catch (ExpiredJwtException e) {
                    System.out.println("JWT Token has expired.");
                } catch (io.jsonwebtoken.MalformedJwtException e) {
                    System.out.println("Bad JWT Token.");
                } catch (UsernameNotFoundException e){
                    System.out.println("User not found.");
                }
            } else {
                logger.warn("JWT Token does not exist.");
            }
//        }
        filterChain.doFilter(request, response);
    }
}
