package com.example.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.example.user.dto.UserResponse;
import com.example.user.model.UserEntity;
import com.example.user.model.UserLoggedIn;
import com.example.user.service.CustomUserDetailsService;
import com.example.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * <b>SecurityConfig</b><br>
 * 
 * Spring Security configuration class that handles user authentication and access permissions. 
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoderConfig passwordEncoder;
    private final Environment environment;

    public SecurityConfig(
        CustomUserDetailsService userDetailsService,
        UserService userService,
        ObjectMapper objectMapper,
        PasswordEncoderConfig passwordEncoder,
        Environment environment
    ) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.DELETE, "/api/auth/delete-account").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder.passwordEncoder()))
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .successHandler((request, response, authentication) -> {
                    issueCsrfCookie(request);
                    UserEntity user = userService.getUserById(((UserLoggedIn) authentication.getPrincipal()).getIdUser());
                    UserResponse body = new UserResponse(user.getIdUser(), user.getEmail(), user.getUsername());

                    response.setStatus(200);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getWriter(), body);
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    try {
                        response.getWriter().write("{\"code\":\"INVALID_CREDENTIALS\",\"message\":\"Incorrect email or password.\"}");
                    } catch (IOException ignored) {
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies(environment.getRequiredProperty("server.servlet.session.cookie.name"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    issueCsrfCookie(request);
                    response.setStatus(200);
                })
                .permitAll()
            );
        return http.build();
    }

    // Login and logout clear the CSRF cookie; reading the new deferred token writes it again
    private void issueCsrfCookie(HttpServletRequest request) {
        Object token = request.getAttribute(CsrfToken.class.getName());
        if (token instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
