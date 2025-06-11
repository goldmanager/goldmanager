package com.my.goldmanager.encoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PasswordEncoderImplTest {

    private final PasswordEncoderImpl encoder = new PasswordEncoderImpl();

    @Test
    void encodedPasswordMatches() {
        String raw = "secret";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
    }
}
