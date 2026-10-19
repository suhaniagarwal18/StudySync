package com.studysync.exception;

/**
 * Thrown when file storage operations fail (invalid extension, file size exceeded,
 * spoofed mime/magic bytes, directory traversal attempt, or missing disk file).
 * 
 * CONCEPT: File I/O Safety & Exception Handling
 */
public class FileStorageException extends StudySyncException {

    private static final long serialVersionUID = 1L;

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
