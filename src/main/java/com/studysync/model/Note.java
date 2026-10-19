package com.studysync.model;

import com.studysync.exception.FileStorageException;
import com.studysync.util.HtmlUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Domain model representing a peer-shared lecture note or study resource.
 * 
 * CONCEPT: Encapsulation & Interface Implementation
 * WHY: Implements Downloadable interface to provide polymorphic streaming of the stored file
 * directly into the servlet response stream.
 */
public class Note implements Downloadable {

    private int noteId;
    private int userId;
    private String uploaderName;
    private String title;
    private String subject;
    private String module;
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private long fileSize;
    private LocalDateTime uploadedAt;

    public Note() {
    }

    public Note(int noteId, int userId, String title, String subject, String module,
                String originalFilename, String storedFilename, String filePath, long fileSize) {
        this.noteId = noteId;
        this.userId = userId;
        this.title = title;
        this.subject = subject;
        this.module = module;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // --------------------------------------------------------------------------
    // Downloadable Interface Implementation
    // --------------------------------------------------------------------------

    /**
     * Streams the note's binary payload from the configured safe storage path.
     * 
     * CONCEPT: File Handling (Buffered Streaming & Try-With-Resources)
     */
    @Override
    public void download(OutputStream out) throws FileStorageException {
        if (filePath == null || filePath.isBlank()) {
            throw new FileStorageException("File path metadata is missing for note ID: " + noteId);
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path) && storedFilename != null) {
            path = com.studysync.util.AppConfig.getUploadDirectory().resolve("notes").resolve(storedFilename);
        }
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new FileStorageException("The requested file does not exist on disk or cannot be read.");
        }
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e) {
            throw new FileStorageException("Failed to stream note file to client: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadName() {
        return originalFilename != null && !originalFilename.isBlank() ? originalFilename : "download";
    }

    // --------------------------------------------------------------------------
    // Formatted View Getters
    // --------------------------------------------------------------------------

    public String getFileSizeDisplay() {
        return HtmlUtil.formatFileSize(fileSize);
    }

    public String getUploadedAtDisplay() {
        return HtmlUtil.formatDateTime(uploadedAt);
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
