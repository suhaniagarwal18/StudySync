package com.studysync.desktop;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public final class WebViewController {

    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private TextField addressField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label errorMessage;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox errorPane;
    @FXML
    private WebView webView;

    private WebEngine webEngine;
    private String startUrl;
    private URI allowedOrigin;
    private String allowedPath;
    private boolean restoringAllowedLocation;

    @FXML
    private void initialize() {
        startUrl = DesktopApplication.loadStartUrl();
        URI configuredUri = parseUri(startUrl);
        validateConfiguredUrl(configuredUri);
        allowedOrigin = configuredUri;
        allowedPath = configuredUri.getPath().endsWith("/")
                ? configuredUri.getPath()
                : configuredUri.getPath().substring(0, configuredUri.getPath().lastIndexOf('/') + 1);

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setCreatePopupHandler(config -> webEngine);
        webEngine.locationProperty().addListener(this::handleLocationChanged);
        webEngine.getLoadWorker().stateProperty().addListener(this::handleLoadStateChanged);
        webEngine.getLoadWorker().exceptionProperty().addListener((observable, oldValue, exception) -> {
            if (exception != null) {
                showError(exception.getMessage());
            }
        });

        updateNavigationButtons();
        loadStartPage();
    }

    @FXML
    private void goBack() {
        if (webEngine.getHistory().getCurrentIndex() > 0) {
            webEngine.getHistory().go(-1);
        }
    }

    @FXML
    private void goForward() {
        int index = webEngine.getHistory().getCurrentIndex();
        if (index + 1 < webEngine.getHistory().getEntries().size()) {
            webEngine.getHistory().go(1);
        }
    }

    @FXML
    private void reload() {
        webEngine.reload();
    }

    @FXML
    private void goHome() {
        loadStartPage();
    }

    @FXML
    private void retry() {
        loadStartPage();
    }

    private void loadStartPage() {
        hideError();
        if (isAllowedUrl(startUrl)) {
            webEngine.load(startUrl);
        } else {
            showError("The configured StudySync URL is outside the allowed origin.");
        }
    }

    private void handleLocationChanged(ObservableValue<? extends String> observable, String oldLocation,
            String newLocation) {
        if (newLocation == null || newLocation.isBlank() || restoringAllowedLocation) {
            return;
        }
        if (!isAllowedUrl(newLocation)) {
            statusLabel.setText("External or unsafe navigation blocked.");
            restoringAllowedLocation = true;
            webEngine.getLoadWorker().cancel();
            if (oldLocation != null && isAllowedUrl(oldLocation)) {
                webEngine.load(oldLocation);
            } else {
                webEngine.load(startUrl);
            }
            restoringAllowedLocation = false;
        } else {
            addressField.setText(newLocation);
            updateNavigationButtons();
        }
    }

    private void handleLoadStateChanged(ObservableValue<? extends Worker.State> observable,
            Worker.State oldState, Worker.State newState) {
        boolean active = newState == Worker.State.SCHEDULED || newState == Worker.State.RUNNING;
        progressIndicator.setVisible(active);
        if (active) {
            hideError();
            statusLabel.setText("Loading StudySync...");
        } else if (newState == Worker.State.SUCCEEDED) {
            statusLabel.setText("Ready");
            addressField.setText(webEngine.getLocation());
            updateNavigationButtons();
        } else if (newState == Worker.State.FAILED) {
            showError(webEngine.getLoadWorker().getException() == null
                    ? "Tomcat could not load the StudySync page."
                    : webEngine.getLoadWorker().getException().getMessage());
        } else if (newState == Worker.State.CANCELLED) {
            statusLabel.setText("Navigation cancelled.");
        }
    }

    private void updateNavigationButtons() {
        if (webEngine == null) {
            return;
        }
        int index = webEngine.getHistory().getCurrentIndex();
        int size = webEngine.getHistory().getEntries().size();
        backButton.setDisable(index <= 0);
        forwardButton.setDisable(index < 0 || index >= size - 1);
    }

    private void showError(String detail) {
        progressIndicator.setVisible(false);
        webView.setVisible(false);
        errorPane.setVisible(true);
        errorMessage.setText("URL: " + startUrl + "\n" + (detail == null || detail.isBlank()
                ? "Tomcat did not return a page."
                : detail));
        statusLabel.setText("StudySync could not be loaded.");
    }

    private void hideError() {
        errorPane.setVisible(false);
        webView.setVisible(true);
    }

    private boolean isAllowedUrl(String value) {
        URI uri = parseUri(value);
        if (uri == null || (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme()))) {
            return false;
        }
        return Objects.equals(uri.getScheme(), allowedOrigin.getScheme())
                && Objects.equals(uri.getHost(), allowedOrigin.getHost())
                && effectivePort(uri) == effectivePort(allowedOrigin)
                && uri.getPath() != null
                && (uri.getPath().equals(allowedPath) || uri.getPath().startsWith(allowedPath));
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static URI parseUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private static void validateConfiguredUrl(URI uri) {
        if (uri == null || uri.getHost() == null || uri.getPath() == null
                || (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalStateException("studysync.start-url must be a valid HTTP or HTTPS URL.");
        }
    }
}
