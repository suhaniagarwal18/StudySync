package com.studysync.service;

import com.studysync.dao.StudySessionDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.FileStorageException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.StudySession;
import com.studysync.model.StudySessionSummary;
import com.studysync.model.User;
import com.studysync.util.AppConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service demonstrating native Java Object Serialization and Deserialization.
 * 
 * CONCEPT: Java Object Serialization (ObjectOutputStream / ObjectInputStream)
 * WHY:
 * Demonstrates binary object serialization for session export and audit summaries.
 * 
 * ARCHITECTURE NOTE:
 * MySQL remains the primary, authoritative relational database for all application data.
 * Serialization is demonstrated as an educational showcase for file-based snapshot export.
 * 
 * SECURITY NOTE:
 * To protect against insecure deserialization vulnerabilities, ObjectInputStream is only
 * invoked on files stored within the application's own restricted summaries directory.
 */
public class SessionSummaryService {

    private final StudySessionDAO studySessionDAO;

    public SessionSummaryService() {
        this.studySessionDAO = new StudySessionDAO();
    }

    public SessionSummaryService(StudySessionDAO studySessionDAO) {
        this.studySessionDAO = studySessionDAO;
    }

    /**
     * Resolves the safe path for serialized session summary snapshots.
     */
    public Path getSummaryPath(int sessionId) {
        Path summariesDir = AppConfig.getUploadDirectory().resolve("summaries");
        try {
            Files.createDirectories(summariesDir);
        } catch (IOException ignored) {}
        return summariesDir.resolve("session-" + sessionId + ".ser").normalize();
    }

    /**
     * Serializes a StudySessionSummary object to disk using ObjectOutputStream.
     */
    public Path exportSummary(int sessionId, User currentUser)
            throws ResourceNotFoundException, AuthorizationException, FileStorageException {

        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Study session not found with ID: " + sessionId);
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin && !session.hasParticipant(currentUser.getUserId())) {
            throw new AuthorizationException("You are not authorized to export this session summary.");
        }

        StudySessionSummary summary = new StudySessionSummary(session, currentUser.getName());
        Path targetFile = getSummaryPath(sessionId);

        // CONCEPT: Serialization (ObjectOutputStream writing to BufferedOutputStream)
        try (OutputStream fileOut = Files.newOutputStream(targetFile);
             BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOut = new ObjectOutputStream(buffOut)) {

            objectOut.writeObject(summary);
            objectOut.flush();
            return targetFile;
        } catch (IOException e) {
            throw new FileStorageException("Failed to serialize session summary: " + e.getMessage(), e);
        }
    }

    /**
     * Deserializes a StudySessionSummary object from disk using ObjectInputStream.
     */
    public StudySessionSummary loadSummary(int sessionId, User currentUser)
            throws ResourceNotFoundException, AuthorizationException, FileStorageException {

        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Study session not found with ID: " + sessionId);
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin && !session.hasParticipant(currentUser.getUserId())) {
            throw new AuthorizationException("You are not authorized to view this session summary.");
        }

        Path targetFile = getSummaryPath(sessionId);
        if (!Files.exists(targetFile)) {
            throw new ResourceNotFoundException("No serialized summary found for session #" + sessionId + ". Please export one first.");
        }

        // CONCEPT: Deserialization (ObjectInputStream reading from BufferedInputStream)
        try (InputStream fileIn = Files.newInputStream(targetFile);
             BufferedInputStream buffIn = new BufferedInputStream(fileIn);
             ObjectInputStream objectIn = new ObjectInputStream(buffIn)) {

            Object obj = objectIn.readObject();
            if (obj instanceof StudySessionSummary summary) {
                return summary;
            } else {
                throw new FileStorageException("Corrupted serialized data: unexpected object type.");
            }
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException("Summary file not found: " + targetFile.getFileName());
        } catch (ClassNotFoundException e) {
            throw new FileStorageException("Serialization error: Class not found during deserialization.", e);
        } catch (IOException e) {
            throw new FileStorageException("Error reading serialized session summary: " + e.getMessage(), e);
        }
    }
}
