package com.chat.client;

import com.chat.client.view.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TCPClient extends Application {

    private static String serverIp = "127.0.0.1";
    private static int serverPort = 5000;

    public static void main(String[] args) {
        if (args.length > 0) serverIp = args[0];
        if (args.length > 1) serverPort = Integer.parseInt(args[1]);
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/chat/client/view/login-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 400, 350);
        stage.setTitle("TCPChat");

        LoginController controller = loader.getController();
        controller.setConnectionParams(serverIp, serverPort);

        stage.setScene(scene);
        stage.show();
    }
}