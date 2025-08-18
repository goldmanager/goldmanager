package com.my.goldmanager.service.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExceptionsCoverageTest {

    @Test
    void constructAllExceptions() {
        assertNotNull(new DuplicateItemException("dup"));
        assertNotNull(new DuplicateItemException("dup2", new Exception("e")));

        assertNotNull(new BadRequestException());
        assertNotNull(new BadRequestException("bad"));
        assertNotNull(new BadRequestException("bad2", new Exception("e")));

        assertNotNull(new ImportInProgressException("imp"));

        assertNotNull(new InvalidAlgorithmException("alg"));
        assertNotNull(new InvalidAlgorithmException("alg2", new Exception("e")));

        assertNotNull(new ImportDataException("ide"));
        assertNotNull(new ImportDataException("ide2", new Exception("e")));

        assertNotNull(new ExportInProgressException("expI"));

        assertNotNull(new ExportDataException("ede"));
        assertNotNull(new ExportDataException("ede2", new Exception("e")));

        assertNotNull(new DuplicateItemTypeException("dit", new Exception("e")));

        assertNotNull(new PasswordValidationException("pwd"));
        assertNotNull(new PasswordValidationException("pwd2", new Exception("e")));

        assertNotNull(new VersionLoadingException("ver"));
        assertNotNull(new VersionLoadingException("ver2", new Exception("e")));

        assertNotNull(new ValidationException("val"));
        assertNotNull(new ValidationException("val2", new Exception("e")));

        assertNotNull(new InvalidHashException("hash"));
        assertNotNull(new InvalidHashException("hash2", new Exception("e")));
    }
}
