package com.chatapp.server.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/chatapp/server/view/server-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 800, 500);
        stage.setTitle("TCPChat Server");
        stage.setScene(scene);
        stage.show();
    }
}
