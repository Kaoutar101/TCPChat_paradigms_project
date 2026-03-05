package com.chat.client.view;

import com.chat.client.listener.MessageListener;
import com.chat.client.model.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ChatController implements MessageListener {

    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;
    @FXML private Circle statusCircle;

    private ClientModel clientModel;

    public void setClientModel(ClientModel model) {
        this.clientModel = model;

        if (model.isReadOnly()) {
            sendButton.setDisable(true);
            messageField.setDisable(true);
            messageField.setPromptText("Read-only mode — you can only read messages");
        }

        model.startListening();
    }

    @FXML
    public void handleSend() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        clientModel.sendMessage(text);
        messageField.clear();
    }

    @FXML
    public void handleEnterKey(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleSend();
        }
    }

    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message + "\n");
        });
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            statusLabel.setText("Offline");
            statusLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
            statusCircle.setFill(Color.web("#f38ba8"));
            sendButton.setDisable(true);
            chatArea.appendText("--- Disconnected from server ---\n");
        });
    }
}