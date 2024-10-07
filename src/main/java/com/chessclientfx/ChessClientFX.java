package com.chessclientfx;

import com.chessclientfx.controller.ConnectionController;
import com.chessclientfx.network.ClientSocket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class ChessClientFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Chess Client FX");
        System.out.println();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/connection_view.fxml"));
        primaryStage.setTitle("Chess Game - Connection");
        ClientSocket clientSocket = new ClientSocket("localhost", 8090);
        ConnectionController controller = new ConnectionController(clientSocket);
        loader.setControllerFactory(param -> controller);
        Parent root = loader.load();
        //connection au serveur multi thread

        System.out.println("Client connected to server");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
