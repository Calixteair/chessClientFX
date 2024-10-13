package com.chessclientfx.controller;

import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessclientfx.network.Protocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateGameController {

    @FXML
    public TextField gameNameField;
    @FXML
    public Button createGameButton;
    @FXML
    public Label errorLabel;

    private ClientSocket clientSocket;

    private PlayerFX playerFX;

    public CreateGameController(ClientSocket clientSocket, PlayerFX playerFX) {
        this.clientSocket = clientSocket;
        this.playerFX = playerFX;
    }

    public void handleCreateGameButton()  throws Exception {
        String gameName = gameNameField.getText();
        if (isValidGameName(gameName)) {
            clientSocket.sendMessage(Protocol.formatCreateGame(gameName));
            String response = clientSocket.receiveMessage();
            if (response.equals("OK")) {
                response = clientSocket.receiveMessage();
                System.out.println(response);
                String uuid = response;
                System.out.println("test1");
                response = clientSocket.receiveMessage();
                System.out.println(response);
                System.out.println("test2");
                switchToGameView(uuid);
            } else {
                System.out.println(response);
                errorLabel.setText("Nom de partie déjà utilisé. Veuillez réessayer.");
            }

        } else {
            errorLabel.setText("Nom de partie invalide. Veuillez réessayer.");
        }
    }

    private boolean isValidGameName(String gameName) {
        return gameName != null && gameName.matches("^[a-zA-Z0-9]{1,20}$");
    }

    public void switchToGameView(String uuid) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_view.fxml"));
        Stage stage = (Stage) createGameButton.getScene().getWindow();
        GameController controller = new GameController(clientSocket, playerFX ,uuid);
        loader.setControllerFactory(param -> controller);
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Chess Game - " + gameNameField.getText());

    }
}
