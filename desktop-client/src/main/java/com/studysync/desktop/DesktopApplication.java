package com.studysync.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class DesktopApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                DesktopApplication.class.getResource("/com/studysync/desktop/desktop.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        stage.setTitle("StudySync");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static String loadStartUrl() {
        Properties properties = new Properties();
        try (InputStream input = DesktopApplication.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Desktop application.properties is missing.");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load desktop application.properties.", exception);
        }

        String url = properties.getProperty("studysync.start-url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("studysync.start-url is missing or blank.");
        }
        return url.trim();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
