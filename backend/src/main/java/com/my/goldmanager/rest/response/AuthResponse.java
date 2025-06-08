package com.my.goldmanager.rest.response;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Response for authentication endpoints containing token metadata only.
 */
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    @Getter
    @Setter
    private Date refreshAfter;

    @Getter
    @Setter
    private Date epiresOn;
}
