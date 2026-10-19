package com.studysync.util;

import com.studysync.exception.FileStorageException;
import com.studysync.exception.ValidationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust file storage utility utilizing java.nio.file and buffered I/O streams.
 * 
 * CONCEPT: Java File Handling & Storage Security
 * WHY:
 * 1. Stores files outside web root to prevent unauthorized direct URL access or code execution.
 * 2. Directory traversal prevention: resolves files strictly under the configured root directory.
 * 3. File extension whitelist and "magic bytes" signature inspection to thwart extension spoofing.
 * 4. UUID-based stored filenames to eliminate collisions and malicious overwrites.
 * 5. Cleans up orphaned disk files if subsequent database operations fail.
 */
public final class FileStorageUtil {

    private static final Logger LOGGER = Logger.getLogger(FileStorageUtil.class.getName());

    // Whitelisted extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "txt", "png", "jpg", "jpeg"))
    );

    // MIME type mapping for downloads
    private static final Map<String, String> MIME_MAP = new HashMap<>();
    static {
        MIME_MAP.put("pdf", "application/pdf");
        MIME_MAP.put("doc", "application/msword");
        MIME_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_MAP.put("ppt", "application/vnd.ms-powerpoint");
        MIME_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_MAP.put("txt", "text/plain; charset=UTF-8");
        MIME_MAP.put("png", "image/png");
        MIME_MAP.put("jpg", "image/jpeg");
        MIME_MAP.put("jpeg", "image/jpeg");
    }

    private FileStorageUtil() {
        // Utility class
    }

    /**
     * Initializes the storage directory and subdirectories on disk.
     */
    public static Path initDirectories() {
        Path baseDir = AppConfig.getUploadDirectory();
        Path notesDir = baseDir.resolve("notes");
        Path summariesDir = baseDir.resolve("summaries");
        try {
            Files.createDirectories(notesDir);
            Files.createDirectories(summariesDir);
            LOGGER.info("Upload storage directories initialized at: " + baseDir.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create upload directories: " + baseDir, e);
        }
        return notesDir;
    }

    /**
     * Extracts and validates file extension.
     */
    public static String getValidatedExtension(String filename) throws ValidationException {
        if (filename == null || !filename.contains(".")) {
            throw new ValidationException("Uploaded file must have a valid extension (e.g. .pdf, .docx, .png).");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ValidationException(
                "File type '." + ext + "' is not allowed. Supported formats: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
        return ext;
    }

    /**
     * Sanitizes original filename for metadata display and download headers.
     * Strips path separators, directory traversal sequences, and control characters.
     */
    public static String sanitizeOriginalFilename(String clientFilename) {
        if (clientFilename == null || clientFilename.isBlank()) {
            return "document.bin";
        }
        // Extract just the basename if full path was sent
        String name = new File(clientFilename).getName();
        // Remove null characters and illegal characters
        name = name.replaceAll("[\\r\\n\\x00\\\\/:*?\"<>|]", "_");
        return name.isBlank() ? "document.bin" : name;
    }

    /**
     * Generates a safe, unguessable filename based on UUID and validated extension.
     */
    public static String generateStoredFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * Inspects initial file bytes ("magic bytes") to verify file signature against extension.
     */
    public static void validateFileSignature(byte[] headerBytes, String extension) throws FileStorageException {
        if (headerBytes == null || headerBytes.length == 0) {
            throw new FileStorageException("Uploaded file appears to be empty.");
        }

        switch (extension.toLowerCase(Locale.ROOT)) {
            case "pdf":
                // PDF magic bytes: %PDF (0x25, 0x50, 0x44, 0x46)
                if (headerBytes.length < 4 ||
                    headerBytes[0] != 0x25 || headerBytes[1] != 0x50 ||
                    headerBytes[2] != 0x44 || headerBytes[3] != 0x46) {
                    throw new FileStorageException("File content does not match standard PDF signature.");
                }
                break;
            case "docx":
            case "pptx":
                // ZIP/OpenXML magic bytes: PK (0x50, 0x4B)
                if (headerBytes.length < 2 || headerBytes[0] != 0x50 || headerBytes[1] != 0x4B) {
                    throw new FileStorageException("File content does not match Office OpenXML document signature.");
                }
                break;
            case "png":
                // PNG magic bytes: 0x89 0x50 0x4E 0x47
                if (headerBytes.length < 4 ||
                    (headerBytes[0] & 0xFF) != 0x89 || headerBytes[1] != 0x50 ||
                    headerBytes[2] != 0x4E || headerBytes[3] != 0x47) {
                    throw new FileStorageException("File content does not match PNG image signature.");
                }
                break;
            case "jpg":
            case "jpeg":
                // JPEG magic bytes: 0xFF 0xD8 0xFF
                if (headerBytes.length < 3 ||
                    (headerBytes[0] & 0xFF) != 0xFF || (headerBytes[1] & 0xFF) != 0xD8 ||
                    (headerBytes[2] & 0xFF) != 0xFF) {
                    throw new FileStorageException("File content does not match JPEG image signature.");
                }
                break;
            case "txt":
                // Text files should not contain binary NUL bytes
                for (byte b : headerBytes) {
                    if (b == 0) {
                        throw new FileStorageException("Text file contains prohibited null bytes.");
                    }
                }
                break;
            case "doc":
            case "ppt":
                // Standard OLE Compound Document signature: 0xD0 0xCF 0x11 0xE0
                if (headerBytes.length >= 4) {
                    if ((headerBytes[0] & 0xFF) != 0xD0 || (headerBytes[1] & 0xFF) != 0xCF ||
                        (headerBytes[2] & 0xFF) != 0x11 || (headerBytes[3] & 0xFF) != 0xE0) {
                        throw new FileStorageException("File content does not match Microsoft Compound Document signature.");
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Stores incoming file stream into the upload directory after validating size and signature.
     */
    public static Path storeFile(InputStream inputStream, String originalFilename, long declaredSize)
            throws ValidationException, FileStorageException {

        if (inputStream == null) {
            throw new FileStorageException("Input stream is null.");
        }

        long maxBytes = AppConfig.getMaxUploadBytes();
        if (declaredSize > maxBytes) {
            throw new ValidationException("File size exceeds maximum permitted limit (" + (maxBytes / (1024 * 1024)) + " MB).");
        }

        String extension = getValidatedExtension(originalFilename);
        Path notesDir = initDirectories();

        // Read first 256 bytes for magic-bytes verification without consuming the full stream
        byte[] header = new byte[256];
        int headerRead;
        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
        bufferedStream.mark(512);
        try {
            headerRead = bufferedStream.read(header);
            if (headerRead <= 0) {
                throw new FileStorageException("Uploaded file is empty.");
            }
            byte[] actualHeader = Arrays.copyOf(header, headerRead);
            validateFileSignature(actualHeader, extension);
            bufferedStream.reset();
        } catch (IOException e) {
            throw new FileStorageException("Error reading uploaded file headers: " + e.getMessage(), e);
        }

        String storedName = generateStoredFilename(extension);
        Path targetPath = notesDir.resolve(storedName).normalize();

        // Directory traversal defense
        if (!targetPath.startsWith(notesDir)) {
            throw new FileStorageException("Security violation: Path traversal detected.");
        }

        try {
            long totalWritten = Files.copy(bufferedStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (totalWritten > maxBytes) {
                Files.deleteIfExists(targetPath);
                throw new ValidationException("File size exceeds maximum permitted limit (" + (maxBytes / (1024 * 1024)) + " MB).");
            }
            return targetPath;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException ignored) {}
            throw new FileStorageException("Failed to persist file to disk: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves a stored filename safely, checking directory traversal and existence.
     */
    public static Path resolveStoredFile(String storedFilename) throws FileStorageException {
        if (storedFilename == null || storedFilename.isBlank()) {
            throw new FileStorageException("Invalid filename reference.");
        }

        Path notesDir = initDirectories();
        Path targetPath = notesDir.resolve(storedFilename).normalize();

        // Security: Prevent ../ traversal attacks
        if (!targetPath.startsWith(notesDir)) {
            throw new FileStorageException("Security error: Illegal file traversal attempt.");
        }

        if (!Files.exists(targetPath) || !Files.isReadable(targetPath)) {
            throw new FileStorageException("The requested file could not be found on the server.");
        }

        return targetPath;
    }

    /**
     * Deletes a file from disk safely.
     */
    public static void deleteFile(String filePathOrName) {
        if (filePathOrName == null || filePathOrName.isBlank()) {
            return;
        }
        try {
            Path path = Path.of(filePathOrName);
            if (!path.isAbsolute()) {
                path = initDirectories().resolve(filePathOrName).normalize();
            }
            Files.deleteIfExists(path);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not delete disk file: " + filePathOrName, e);
        }
    }

    /**
     * Returns appropriate MIME type for an extension.
     */
    public static String getMimeType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "application/octet-stream";
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return MIME_MAP.getOrDefault(ext, "application/octet-stream");
    }
}
