package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.my.goldmanager.service.CustomUserDetailsService;
import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.AuthKeyInfoService;

@SpringJUnitConfig(classes = DefaultSecurityConfigurationIntegrationTest.TestConfig.class)
class DefaultSecurityConfigurationIntegrationTest {

    @Configuration
    @Import(DefaultSecurityConfiguration.class)
    static class TestConfig {
        @Bean
        UserLoginRepository userLoginRepository() {
            return mock(UserLoginRepository.class);
        }

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return new CustomUserDetailsService();
        }

        @Bean
        AuthKeyInfoService authKeyInfoService() {
            return mock(AuthKeyInfoService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Bean(name = "mvcHandlerMappingIntrospector")
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChainBean_isBuilt() {
        assertNotNull(securityFilterChain);
    }
}
