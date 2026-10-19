package com.studysync.service;

import com.studysync.dao.NoteDAO;
import com.studysync.dao.UserDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.FileStorageException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.exception.ValidationException;
import com.studysync.model.Note;
import com.studysync.model.User;
import com.studysync.util.FileStorageUtil;
import com.studysync.util.ValidationUtil;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Service managing lecture notes, secure uploads, metadata indexing, and access-controlled deletion.
 * 
 * CONCEPT: File Handling & Atomicity
 * WHY: Synchronizes disk file persistence with database record insertion. If database metadata
 * insertion fails, the physical file on disk is immediately deleted to prevent orphaned assets.
 */
public class NoteService {

    private final NoteDAO noteDAO;
    private final UserDAO userDAO;

    public NoteService() {
        this.noteDAO = new NoteDAO();
        this.userDAO = new UserDAO();
    }

    public NoteService(NoteDAO noteDAO, UserDAO userDAO) {
        this.noteDAO = noteDAO;
        this.userDAO = userDAO;
    }

    /**
     * Uploads and indexes a new study resource note.
     */
    public Note uploadNote(int userId, String title, String subject, String module,
                           String clientFilename, InputStream fileStream, long declaredSize)
            throws ValidationException, FileStorageException {

        if (title == null || title.trim().length() < 2 || title.trim().length() > 150) {
            throw new ValidationException("Title must be between 2 and 150 characters.");
        }
        ValidationUtil.validateSubject(subject);

        String sanitizedOriginalName = FileStorageUtil.sanitizeOriginalFilename(clientFilename);
        Path storedPath = null;
        try {
            // Persist file physically to disk with signature and size validation
            storedPath = FileStorageUtil.storeFile(fileStream, sanitizedOriginalName, declaredSize);
            String storedFilename = storedPath.getFileName().toString();
            long actualSize = java.nio.file.Files.size(storedPath);

            Note note = new Note();
            note.setUserId(userId);
            note.setTitle(title.trim());
            note.setSubject(ValidationUtil.normalizeSkillName(subject));
            note.setModule(module != null ? module.trim() : "");
            note.setOriginalFilename(sanitizedOriginalName);
            note.setStoredFilename(storedFilename);
            note.setFilePath(storedPath.toAbsolutePath().toString());
            note.setFileSize(actualSize);

            // Persist metadata to database
            return noteDAO.insert(note);
        } catch (Exception e) {
            // Rollback: eliminate orphaned disk file if metadata save fails
            if (storedPath != null) {
                FileStorageUtil.deleteFile(storedPath.toString());
            }
            if (e instanceof ValidationException ve) {
                throw ve;
            }
            if (e instanceof FileStorageException fse) {
                throw fse;
            }
            throw new FileStorageException("Failed to upload note: " + e.getMessage(), e);
        }
    }

    /**
     * Searches notes with optional keyword and subject filter.
     */
    public List<Note> searchNotes(String keyword, String subject) {
        return noteDAO.search(keyword, subject);
    }

    public List<Note> listNotesByUser(int userId) {
        return noteDAO.listByUser(userId);
    }

    public List<Note> listAllNotes() {
        return noteDAO.listAll();
    }

    public Note getNoteById(int noteId) throws ResourceNotFoundException {
        Note note = noteDAO.findById(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Note not found with ID: " + noteId);
        }
        return note;
    }

    /**
     * Deletes a note from both the database and physical disk storage.
     * Only the original uploader or an Admin may delete.
     */
    public void deleteNote(int noteId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException {

        Note note = noteDAO.findById(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Note not found with ID: " + noteId);
        }

        User user = userDAO.findById(currentUserId);
        boolean isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        boolean isUploader = note.getUserId() == currentUserId;

        if (!isAdmin && !isUploader) {
            throw new AuthorizationException("You are not authorized to delete this note.");
        }

        // Delete from database first, then delete disk file
        noteDAO.delete(noteId);
        FileStorageUtil.deleteFile(note.getFilePath());
    }

    public List<String> getDistinctSubjects() {
        return noteDAO.distinctSubjects();
    }
}
