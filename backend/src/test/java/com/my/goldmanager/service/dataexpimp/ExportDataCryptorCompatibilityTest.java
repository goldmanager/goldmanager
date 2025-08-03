package com.my.goldmanager.service.dataexpimp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.service.PasswordPolicyValidationService;
import com.my.goldmanager.service.entity.ExportData;
import com.my.goldmanager.service.exception.PasswordValidationException;
import com.my.goldmanager.service.exception.ValidationException;

@SpringBootTest
@ActiveProfiles("test")
class ExportDataCryptorCompatibilityTest {

    @Autowired
    private ExportDataCryptor exportDataCryptor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordPolicyValidationService passwordPolicyValidationService;

    @Test
    void decryptsLegacyEncryptedData() throws Exception {
        ExportData exportData = new ExportData();
        exportData.setVersion("1.0.0");
        exportData.setExportEntityData(new byte[] {1,2,3});
        exportData.setHash(new byte[] {4,5,6});
        exportData.setHashAlgorithm("SHA-256");

        String password = "validPassword";
        byte[] legacyEncrypted = legacyEncrypt(exportData, password);

        ExportData decrypted = exportDataCryptor.decrypt(legacyEncrypted, password);
        assertEquals(objectMapper.writeValueAsString(exportData), objectMapper.writeValueAsString(decrypted));
    }

    private byte[] legacyEncrypt(ExportData exportData, String encryptionPassword) throws Exception {
        if (exportData == null) {
            throw new ValidationException("ExportData cannot be null");
        }
        try {
            passwordPolicyValidationService.validate(encryptionPassword);
        } catch (ValidationException ve) {
            throw new PasswordValidationException(ve.getMessage(), ve);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] salt = DataExportImportCryptoUtil.generateSalt();
        byte[] iv = DataExportImportCryptoUtil.generateIV();

        SecretKey key = DataExportImportCryptoUtil.generateKeyFromPassword(encryptionPassword, salt);
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        Cipher cipher = DataExportImportCryptoUtil.getCipher(key, iv, Cipher.ENCRYPT_MODE);

        ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
        try (CipherOutputStream cout = new CipherOutputStream(encryptedData, cipher)) {
            // Adding magic bytes to ensure correct encryption:
            cout.write(ExportDataCryptor.body_start);
            byte[] payload = objectMapper.writeValueAsBytes(exportData);
            cout.write(DataExportImportUtil.longToByteArray(payload.length));
            cout.write(payload);
            cout.flush();
        }
        try (DeflaterOutputStream deflaterOutPutStream = new DeflaterOutputStream(bos, deflater)) {
            deflaterOutPutStream.write(ExportDataCryptor.header_start);
            byte[] encryptedDataPayload = encryptedData.toByteArray();
            deflaterOutPutStream.write(DataExportImportUtil.longToByteArray(encryptedDataPayload.length));
            deflaterOutPutStream.write(salt);
            deflaterOutPutStream.write(iv);
            deflaterOutPutStream.write(encryptedDataPayload);
            deflaterOutPutStream.flush();
        }

        return bos.toByteArray();
    }
}
