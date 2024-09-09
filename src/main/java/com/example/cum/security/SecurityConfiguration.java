package com.example.cum.security;


import com.example.cum.resolver.UserArgumentResolver;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration implements WebMvcConfigurer {
    private final AuthTokenFilter authTokenFilter;
    private final UserArgumentResolver userArgumentResolver;
    private final AccessDeniedHandlerImpl accessDeniedHandlerImpl;
    private final EntryPointImpl authenticationEntryPoint;

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/avatars/public/*",
            "/api/v1/orphanages"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
//    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(userArgumentResolver);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("*"); // Ganti dengan asal yang diizinkan
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    source.registerCorsConfiguration("/**", config);
                    cors.configurationSource(source);
                })
                .exceptionHandling(e -> {
                    e.accessDeniedHandler(accessDeniedHandlerImpl);
                    e.authenticationEntryPoint(authenticationEntryPoint);
                })
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req ->
                        req.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                                .requestMatchers(WHITE_LIST_URL).permitAll()
//                                .requestMatchers(ApiUrl.CUSTOMER_API +"/**").hasAnyAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/api/v1/feedback/*").hasAnyAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/api/v1/orphanages/*/status").hasAnyAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/api/v1/orphanages/needs/*/status").hasAnyAuthority("ROLE_ADMIN")
                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}