package com.my.goldmanager.config;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class DevWebConfigTest {

    @Test
    void addCorsMappings_configuresRegistry() {
        DevWebConfig cfg = new DevWebConfig();
        CorsRegistry registry = mock(CorsRegistry.class, RETURNS_DEEP_STUBS);

        cfg.addCorsMappings(registry);

        // Verify the fluent chain was invoked at least once.
        verify(registry).addMapping("/**");
    }
}

