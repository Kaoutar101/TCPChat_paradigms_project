package com.chat.client.view;

import com.chat.client.model.ClientModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private Button connectButton;

    private String ip;
    private int port;

    public void setConnectionParams(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @FXML
    public void handleConnect() {
        String username = usernameField.getText().trim();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/chat/client/view/chat-view.fxml")
            );
            Scene chatScene = new Scene(loader.load(), 600, 500);

            ChatController chatController = loader.getController();

            // create the model and connect
            ClientModel clientModel = new ClientModel(ip, port, username, chatController);
            clientModel.connect(ip, port);

            // pass model to chat controller
            chatController.setClientModel(clientModel);

            Stage stage = (Stage) connectButton.getScene().getWindow();
            stage.setScene(chatScene);
            stage.setTitle("TCPChat — " + (username.isEmpty() ? "Read-Only" : username));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
