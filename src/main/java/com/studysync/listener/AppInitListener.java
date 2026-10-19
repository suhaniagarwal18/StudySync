package com.studysync.listener;

import com.studysync.util.AppConfig;
import com.studysync.util.FileStorageUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application Lifecycle Listener initializing upload directories and sample demonstration files.
 * 
 * CONCEPT: Servlet Listeners & Application Lifecycle Events
 * WHY: Executes pre-flight bootstrap tasks upon ServletContext initialization, ensuring
 * upload directories exist outside the webapp root and deploying bundled seed note files.
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("StudySync Web Application Initializing...");
        Path notesDir = FileStorageUtil.initDirectories();

        // Copy bundled sample files to storage directory if not already present
        copySampleResource("sample-uploads/sample-java-notes.pdf", notesDir.resolve("sample-java-notes.pdf"));
        copySampleResource("sample-uploads/sample-dbms-notes.pdf", notesDir.resolve("sample-dbms-notes.pdf"));
        copySampleResource("sample-uploads/sample-dsa-notes.txt", notesDir.resolve("sample-dsa-notes.txt"));

        LOGGER.info("StudySync Initialized successfully. Notes directory: " + notesDir.toAbsolutePath());
    }

    private void copySampleResource(String resourcePath, Path targetPath) {
        if (!Files.exists(targetPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Copied sample demonstration file to: " + targetPath.getFileName());
                } else {
                    LOGGER.warning("Could not find bundled resource: " + resourcePath);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to copy sample resource: " + resourcePath, e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("StudySync Web Application Stopped.");
    }
}
