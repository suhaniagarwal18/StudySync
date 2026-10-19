package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.Note;
import com.studysync.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for Note entities.
 * 
 * CONCEPT: JDBC & Parameterized Query Construction
 * WHY: Provides search, filtering, and CRUD operations for lecture notes with
 * safe wildcard escaping for SQL LIKE queries.
 */
public class NoteDAO {

    /**
     * Inserts a new note record into the database.
     */
    public Note insert(Note note) {
        String sql = "INSERT INTO notes (user_id, title, subject, module, original_filename, stored_filename, file_path, file_size) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, note.getUserId());
            ps.setString(2, note.getTitle());
            ps.setString(3, note.getSubject());
            ps.setString(4, note.getModule());
            ps.setString(5, note.getOriginalFilename());
            ps.setString(6, note.getStoredFilename());
            ps.setString(7, note.getFilePath());
            ps.setLong(8, note.getFileSize());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating note failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setNoteId(rs.getInt(1));
                }
            }
            return note;
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting note: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a single note by primary key ID.
     */
    public Note findById(int noteId) {
        String sql = "SELECT n.note_id, n.user_id, u.name AS uploader_name, n.title, n.subject, n.module, "
                   + "n.original_filename, n.stored_filename, n.file_path, n.file_size, n.uploaded_at "
                   + "FROM notes n "
                   + "JOIN users u ON n.user_id = u.user_id "
                   + "WHERE n.note_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNote(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding note by ID: " + noteId, e);
        }
    }

    /**
     * Searches notes with optional keyword across title/subject/module and optional subject filter.
     */
    public List<Note> search(String keyword, String subject) {
        StringBuilder sql = new StringBuilder(
            "SELECT n.note_id, n.user_id, u.name AS uploader_name, n.title, n.subject, n.module, "
          + "n.original_filename, n.stored_filename, n.file_path, n.file_size, n.uploaded_at "
          + "FROM notes n "
          + "JOIN users u ON n.user_id = u.user_id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (subject != null && !subject.isBlank()) {
            sql.append("AND LOWER(n.subject) = LOWER(?) ");
            params.add(subject.trim());
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(n.title) LIKE ? OR LOWER(n.subject) LIKE ? OR LOWER(n.module) LIKE ?) ");
            String escaped = "%" + escapeLikeWildcards(keyword.trim().toLowerCase()) + "%";
            params.add(escaped);
            params.add(escaped);
            params.add(escaped);
        }

        sql.append("ORDER BY n.uploaded_at DESC");

        List<Note> results = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToNote(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("Error searching notes", e);
        }
    }

    /**
     * Lists notes uploaded by a specific user.
     */
    public List<Note> listByUser(int userId) {
        String sql = "SELECT n.note_id, n.user_id, u.name AS uploader_name, n.title, n.subject, n.module, "
                   + "n.original_filename, n.stored_filename, n.file_path, n.file_size, n.uploaded_at "
                   + "FROM notes n "
                   + "JOIN users u ON n.user_id = u.user_id "
                   + "WHERE n.user_id = ? ORDER BY n.uploaded_at DESC";
        List<Note> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToNote(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing notes for user ID: " + userId, e);
        }
    }

    /**
     * Lists all notes across the platform (for admin oversight).
     */
    public List<Note> listAll() {
        return search(null, null);
    }

    /**
     * Returns all distinct subjects for note filtering dropdowns.
     */
    public List<String> distinctSubjects() {
        String sql = "SELECT DISTINCT subject FROM notes ORDER BY subject ASC";
        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("subject"));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing distinct note subjects", e);
        }
    }

    /**
     * Deletes a note by its primary key ID.
     */
    public void delete(int noteId) {
        String sql = "DELETE FROM notes WHERE note_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, noteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting note ID: " + noteId, e);
        }
    }

    private Note mapRowToNote(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setNoteId(rs.getInt("note_id"));
        note.setUserId(rs.getInt("user_id"));
        note.setUploaderName(rs.getString("uploader_name"));
        note.setTitle(rs.getString("title"));
        note.setSubject(rs.getString("subject"));
        note.setModule(rs.getString("module"));
        note.setOriginalFilename(rs.getString("original_filename"));
        note.setStoredFilename(rs.getString("stored_filename"));
        note.setFilePath(rs.getString("file_path"));
        note.setFileSize(rs.getLong("file_size"));

        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) {
            note.setUploadedAt(ts.toLocalDateTime());
        }
        return note;
    }

    private String escapeLikeWildcards(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");
    }
}
