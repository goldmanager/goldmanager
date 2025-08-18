package com.my.goldmanager.scheduled;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.my.goldmanager.service.AuthKeyInfoService;

class CleanUpExpiredKeyIdsTest {

    @Test
    void doCleanUp_invokesService() {
        CleanUpExpiredKeyIds cleaner = new CleanUpExpiredKeyIds();
        AuthKeyInfoService svc = mock(AuthKeyInfoService.class);
        ReflectionTestUtils.setField(cleaner, "authKeyInfoService", svc);

        cleaner.doCleaUp();

        verify(svc).cleanUpExpiredKeyInfos();
    }
}

