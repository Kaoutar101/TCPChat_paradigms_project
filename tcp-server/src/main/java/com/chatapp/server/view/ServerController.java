package com.chatapp.server.view;

import com.chatapp.server.TCPServer;
import com.chatapp.server.listener.ServerEventListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListCell;
import java.util.Random;

public class ServerController implements ServerEventListener {

    @FXML private ListView<String> usersListView;
    @FXML private TextArea logArea;

    private TCPServer server;
    private final Random random = new Random();

    // random pastel colors for users
    private final String[] COLORS = {
            "#f38ba8", "#fab387", "#f9e2af",
            "#a6e3a1", "#89dceb", "#89b4fa", "#cba6f7"
    };

    public void initialize() {
        server = new TCPServer();
        server.setListener(this);

        // style the ListView cells with random colors
        usersListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = COLORS[random.nextInt(COLORS.length)];
                    setStyle("-fx-background-color: " + color + ";" +
                            "-fx-text-fill: #1e1e2e;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8;" +
                            "-fx-background-radius: 5;");
                }
            }
        });

        // start server in background thread
        Thread serverThread = new Thread(() -> {
            try {
                int port = TCPServer.loadPortFromConfig();
                server.run(port);
            } catch (Exception e) {
                onLogMessage("Error starting server: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void onClientConnected(String username) {
        Platform.runLater(() -> {
            usersListView.getItems().add(username);
            log("✔ " + username + " joined.");
        });
    }

    @Override
    public void onClientDisconnected(String username) {
        Platform.runLater(() -> {
            usersListView.getItems().remove(username);
            log("✖ " + username + " left.");
        });
    }

    @Override
    public void onLogMessage(String message) {
        Platform.runLater(() -> log(message));
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }
}
