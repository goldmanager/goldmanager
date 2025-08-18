package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.my.goldmanager.config.filter.UIWebFilter;

class UiPathWebConfigTest {

    @Test
    void spaWebFilter_registersFilter() {
        UiPathWebConfig config = new UiPathWebConfig();
        FilterRegistrationBean<UIWebFilter> bean = config.spaWebFilter();
        assertNotNull(bean);
        assertNotNull(bean.getFilter());
        assertTrue(bean.getUrlPatterns().contains("/*"));
    }
}

