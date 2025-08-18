package com.my.goldmanager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.UnitService;
import com.my.goldmanager.service.UserService;

import org.springframework.test.util.ReflectionTestUtils;

class ConfigInitializationRunnerTest {

    private ConfigInitializationRunner runner;
    private UserService userService;
    private MaterialService materialService;
    private UnitService unitService;

    @BeforeEach
    void setUp() {
        runner = new ConfigInitializationRunner();
        userService = mock(UserService.class);
        materialService = mock(MaterialService.class);
        unitService = mock(UnitService.class);

        ReflectionTestUtils.setField(runner, "userService", userService);
        ReflectionTestUtils.setField(runner, "materialService", materialService);
        ReflectionTestUtils.setField(runner, "unitService", unitService);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ConfigInitializationRunner.ENV_DEFAULTUSER);
        System.clearProperty(ConfigInitializationRunner.ENV_DEFAULTPASSWORD);
    }

    @Test
    void createsDefaultUser_whenNoUsersExist_andCredsProvided() throws Exception {
        System.setProperty(ConfigInitializationRunner.ENV_DEFAULTUSER, "admin");
        System.setProperty(ConfigInitializationRunner.ENV_DEFAULTPASSWORD, "admin!pass");

        when(userService.countUsers()).thenReturn(0L);
        when(materialService.list()).thenReturn(Collections.emptyList());
        when(unitService.listAll()).thenReturn(Collections.emptyList());

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userService).create("admin", "admin!pass");

        // verify a material was created
        ArgumentCaptor<Material> mat = ArgumentCaptor.forClass(Material.class);
        verify(materialService).store(mat.capture());
        assertEquals("Gold", mat.getValue().getName());
        assertTrue(mat.getValue().getPrice() > 0);
        assertTrue(mat.getValue().getEntryDate() instanceof Date);

        // verify a unit was created
        ArgumentCaptor<Unit> unit = ArgumentCaptor.forClass(Unit.class);
        verify(unitService).save(unit.capture());
        assertEquals("Oz", unit.getValue().getName());
        assertEquals(1, unit.getValue().getFactor());
    }

    @Test
    void skipsUserCreation_whenUsersExist() throws Exception {
        System.setProperty(ConfigInitializationRunner.ENV_DEFAULTUSER, "admin");
        System.setProperty(ConfigInitializationRunner.ENV_DEFAULTPASSWORD, "admin!pass");

        when(userService.countUsers()).thenReturn(1L);
        when(materialService.list()).thenReturn(Collections.emptyList());
        when(unitService.listAll()).thenReturn(Collections.emptyList());

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userService, never()).create(anyString(), anyString());
    }
}
