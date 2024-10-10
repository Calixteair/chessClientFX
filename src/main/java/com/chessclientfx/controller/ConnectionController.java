package com.chessclientfx.controller;

import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessclientfx.network.Protocol;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectionController {

    private ClientSocket clientSocket;

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button connectButton;

    public ConnectionController(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @FXML
    public void handleConnectButton(ActionEvent event) throws Exception {
        String username = usernameField.getText();
        if (isValidUsername(username)) {
            //connection au serveur multi thread
            String message = Protocol.formatConnect(username);
            clientSocket.sendMessage(Protocol.formatConnect(username));
            System.out.println(clientSocket.receiveMessage());
            String response = clientSocket.receiveMessage();
            if (response.equals("OK")) {
                System.out.println("Connexion réussie.");
                PlayerFX playerFX = new PlayerFX(username);
                switchToGameView(playerFX);

            } else {
                errorLabel.setText("Pseudo déjà utilisé. Veuillez réessayer.");
            }

        } else {
            errorLabel.setText("Pseudo invalide. Veuillez réessayer.");
        }
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9]{1,20}$");
    }

    private void switchToGameView(PlayerFX playerFX) {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home_view.fxml"));
            loader.setControllerFactory(param -> new HomeController(clientSocket, playerFX));
            Parent homeRoot = loader.load();


            // Obtenir la scène actuelle à partir du bouton
            Stage stage = (Stage) connectButton.getScene().getWindow();

            // Remplacer la scène par la nouvelle vue
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("Chess Game");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
