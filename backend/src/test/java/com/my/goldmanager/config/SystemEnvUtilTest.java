package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SystemEnvUtilTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("TEST_VAR");
    }

    @Test
    void readVariable_returnsSystemProperty_whenEnvMissing() {
        System.setProperty("TEST_VAR", "value123");
        assertEquals("value123", SystemEnvUtil.readVariable("TEST_VAR"));
    }
}

