package com.my.goldmanager.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.my.goldmanager.repository.UserLoginRepository;
import com.my.goldmanager.service.AuthKeyInfoService;
import com.my.goldmanager.service.CustomUserDetailsService;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = DefaultSecurityAuthorizationIT.TestConfig.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class DefaultSecurityAuthorizationIT {

    @Configuration
    @Import(DefaultSecurityConfiguration.class)
    static class TestConfig {
        @Bean
        UserLoginRepository userLoginRepository() { return mock(UserLoginRepository.class); }

        @Bean
        CustomUserDetailsService customUserDetailsService() { return new CustomUserDetailsService(); }

        @Bean
        AuthKeyInfoService authKeyInfoService() { return mock(AuthKeyInfoService.class); }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() { return new JwtAuthenticationFilter(); }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Bean(name = "mvcHandlerMappingIntrospector")
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() { return new HandlerMappingIntrospector(); }
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginEndpoint_isPermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isNotFound());
    }

    @Test
    void dataImportStatus_isPermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/dataimport/status")).andExpect(status().isNotFound());
    }

    @Test
    void protectedApi_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isForbidden());
    }
}
