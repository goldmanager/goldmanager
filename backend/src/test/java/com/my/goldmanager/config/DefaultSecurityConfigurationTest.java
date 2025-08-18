package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import com.my.goldmanager.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class DefaultSecurityConfigurationTest {

    @org.mockito.Mock
    private CustomUserDetailsService userDetailsService;

    private final DefaultSecurityConfiguration configuration = new DefaultSecurityConfiguration();

    @Test
    void passwordEncoder_and_userDetailsService_and_authenticationManager_work() {
        // userDetailsService bean
        // inject mock service
        org.springframework.test.util.ReflectionTestUtils.setField(configuration, "userDetailsService", userDetailsService);

        UserDetailsService uds = configuration.userDetailsService();
        assertSame(userDetailsService, uds);

        // password encoder
        PasswordEncoder encoder = configuration.passwordEncoder();
        String raw = "secret";
        String enc = encoder.encode(raw);
        assertTrue(encoder.matches(raw, enc));

        // authentication manager success
        UserDetails user = User.withUsername("alice")
                .password(enc)
                .authorities(Collections.emptyList())
                .disabled(false)
                .build();
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);

        AuthenticationManager authMgr = configuration.authenticationManager();
        assertNotNull(authMgr);
        var result = authMgr.authenticate(new UsernamePasswordAuthenticationToken("alice", raw));
        assertNotNull(result);
        assertEquals("alice", result.getName());

        // authentication manager failure
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(user);
        assertThrows(BadCredentialsException.class, () ->
                configuration.authenticationManager()
                        .authenticate(new UsernamePasswordAuthenticationToken("bob", "wrong")));
    }
}
