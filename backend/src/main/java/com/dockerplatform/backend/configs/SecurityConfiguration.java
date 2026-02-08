package com.dockerplatform.backend.configs;

import com.dockerplatform.backend.security.CustomUserDetailsService;
import com.dockerplatform.backend.security.JwtFilter;
import com.dockerplatform.backend.security.SystemLockdownFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private SystemLockdownFilter lockdownFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
         return http
                 .cors(Customizer.withDefaults())
                 .csrf(customizer -> customizer.disable())
                 .httpBasic(basic -> {})
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/user/register","/auth", "/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registry/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/token").authenticated()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                 .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .addFilterBefore(lockdownFilter, UsernamePasswordAuthenticationFilter.class)
                 .addFilterBefore(jwtFilter, SystemLockdownFilter.class)
                 .build();

    }

    @Autowired
    public CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }
}
