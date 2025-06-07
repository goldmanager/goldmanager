package com.my.goldmanager.rest.response;

import com.my.goldmanager.service.entity.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response containing the current job status and an optional message.
 */
@AllArgsConstructor
@NoArgsConstructor
public class JobStatusResponse {
    @Getter
    @Setter
    private JobStatus status;

    @Getter
    @Setter
    private String message;
}
