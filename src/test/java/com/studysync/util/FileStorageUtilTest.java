package com.studysync.util;

import com.studysync.exception.FileStorageException;
import com.studysync.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying FileStorageUtil defenses: magic-byte validation,
 * extension whitelist, path-traversal prevention, and size bounds.
 */
class FileStorageUtilTest {

    @Test
    @DisplayName("Whitelisted extensions should pass; disallowed extensions (.exe, .sh, .php) rejected")
    void testExtensionWhitelist() throws Exception {
        assertEquals("pdf", FileStorageUtil.getValidatedExtension("lecture_notes.pdf"));
        assertEquals("docx", FileStorageUtil.getValidatedExtension("syllabus.DOCX"));
        assertEquals("png", FileStorageUtil.getValidatedExtension("diagram.png"));

        assertThrows(ValidationException.class, () -> FileStorageUtil.getValidatedExtension("script.exe"));
        assertThrows(ValidationException.class, () -> FileStorageUtil.getValidatedExtension("exploit.php"));
        assertThrows(ValidationException.class, () -> FileStorageUtil.getValidatedExtension("malware.sh"));
        assertThrows(ValidationException.class, () -> FileStorageUtil.getValidatedExtension("noextension"));
    }

    @Test
    @DisplayName("Filename sanitization must eliminate path separators and directory traversal tokens")
    void testSanitizeFilename() {
        String unsafe = "../../sensitive/passwords.txt";
        String clean = FileStorageUtil.sanitizeOriginalFilename(unsafe);

        assertFalse(clean.contains("/"));
        assertFalse(clean.contains("\\"));
        assertFalse(clean.contains(".."));
        assertEquals("passwords.txt", clean);
    }

    @Test
    @DisplayName("Magic bytes validation: valid PDF (%PDF) accepted, spoofed PDF rejected")
    void testMagicBytesPdf() {
        // Valid PDF signature: %PDF (0x25, 0x50, 0x44, 0x46)
        byte[] validPdfHeader = "%PDF-1.4 sample content".getBytes(StandardCharsets.US_ASCII);
        assertDoesNotThrow(() -> FileStorageUtil.validateFileSignature(validPdfHeader, "pdf"));

        // Spoofed PDF (e.g. plain text or executable renamed to .pdf)
        byte[] fakePdfHeader = "MZThisIsAnExecutable".getBytes(StandardCharsets.US_ASCII);
        assertThrows(FileStorageException.class, () -> FileStorageUtil.validateFileSignature(fakePdfHeader, "pdf"));
    }

    @Test
    @DisplayName("Magic bytes validation: valid PNG and JPEG accepted, corrupt headers rejected")
    void testMagicBytesImages() {
        // Valid PNG signature: 0x89 0x50 0x4E 0x47
        byte[] validPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertDoesNotThrow(() -> FileStorageUtil.validateFileSignature(validPng, "png"));

        // Valid JPEG signature: 0xFF 0xD8 0xFF
        byte[] validJpg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        assertDoesNotThrow(() -> FileStorageUtil.validateFileSignature(validJpg, "jpg"));

        // Invalid signature
        byte[] garbage = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertThrows(FileStorageException.class, () -> FileStorageUtil.validateFileSignature(garbage, "png"));
    }

    @Test
    @DisplayName("Files exceeding the 10 MB maximum upload limit must be rejected")
    void testOversizeLimit() {
        InputStream stream = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        long oversize = 15 * 1024 * 1024L; // 15 MB

        assertThrows(ValidationException.class, () -> FileStorageUtil.storeFile(stream, "notes.pdf", oversize));
    }

    @Test
    @DisplayName("Valid file storage generates unique UUID filename and stores successfully")
    void testStoreAndResolveValidFile() throws Exception {
        byte[] content = "%PDF-1.4 Valid PDF for JUnit 5 Test".getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream in = new ByteArrayInputStream(content);

        Path storedPath = FileStorageUtil.storeFile(in, "test-document.pdf", content.length);
        assertNotNull(storedPath);
        assertTrue(storedPath.getFileName().toString().endsWith(".pdf"));

        // Verify resolution
        Path resolved = FileStorageUtil.resolveStoredFile(storedPath.getFileName().toString());
        assertEquals(storedPath.toAbsolutePath(), resolved.toAbsolutePath());

        // Cleanup
        FileStorageUtil.deleteFile(storedPath.toString());
    }

    @Test
    @DisplayName("Directory traversal attempt during file resolution must throw FileStorageException")
    void testResolveTraversalFails() {
        assertThrows(FileStorageException.class, () -> FileStorageUtil.resolveStoredFile("../../etc/passwd"));
        assertThrows(FileStorageException.class, () -> FileStorageUtil.resolveStoredFile("..\\..\\windows\\system32\\cmd.exe"));
    }
}
