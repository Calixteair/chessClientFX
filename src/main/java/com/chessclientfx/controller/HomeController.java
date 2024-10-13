package com.chessclientfx.controller;

import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.Protocol;

import com.chessserver.server.GameSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import com.chessclientfx.network.ClientSocket;
import javafx.stage.Stage;

import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;

public class HomeController {

    @FXML
    private ListView<String> gameListView;
    @FXML
    private Button createGameButton;

    @FXML
    private Button viewGamesButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label usernameLabel;

    private ClientSocket clientSocket;


    List<GameSession> gameslist;


    ObservableList<String> games = FXCollections.observableArrayList();

    public PlayerFX playerFX;

    public HomeController(ClientSocket clientSocket, PlayerFX playerFX) {
        this.clientSocket = clientSocket;
        this.playerFX = playerFX;
    }

    @FXML
    public void initialize() throws Exception {
        usernameLabel.setText("pseudo : " + playerFX.getPseudo());
        handleViewGamesButton();
        // Ajouter un écouteur pour les clics sur les parties
        gameListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleJoinGameButton(newValue);
            }
        });
    }


    @FXML
    private void handleCreateGameButton() {
        System.out.println("Création d'une nouvelle partie");
        switchToCreateGameView();
    }

    @FXML
    private void handleLogoutButton() {
        clientSocket.sendMessage(Protocol.DISCONNECT);
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
    }

    public void handleJoinGameButton(String game) {

        try {
            // Envoi de la demande de connexion à la partie
            clientSocket.sendMessage(Protocol.formatJoinGame(game));
            String response = clientSocket.receiveMessage();
            if (response.equals("OK")) {
                response = clientSocket.receiveMessage();
                System.out.println("rep before siwtch" + response);
                switchToGameView(game);
            } else {
                errorLabel.setText(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handleViewGamesButton() throws Exception {
        System.out.println("Liste des parties demandée");
        // Envoyer la requête pour lister les parties disponibles
        clientSocket.sendMessage(Protocol.LIST_GAMES);
        // Réutilisez le ObjectInputStream si possible
        ObjectInputStream ois;
        if (clientSocket.getInputStream() instanceof ObjectInputStream) {
            ois = (ObjectInputStream) clientSocket.getInputStream();
        } else {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        }
        gameslist = (List<GameSession>) ois.readObject();

        for (GameSession game : gameslist) {
            games.add(game.getId());
        }

        gameListView.setItems(games);
    }

    private void switchToCreateGameView() {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_game_view.fxml"));
            playerFX.isWhite = true;
            loader.setControllerFactory(param -> new CreateGameController(clientSocket, playerFX));
            Parent homeRoot = loader.load();


            // Obtenir la scène actuelle à partir du bouton
            Stage stage = (Stage) createGameButton.getScene().getWindow();

            // Remplacer la scène par la nouvelle vue
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("Create Game");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToGameView(String gameSessionId) throws IOException {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_view.fxml"));
            playerFX.isWhite = false;
            loader.setControllerFactory(param -> new GameController(clientSocket, playerFX, gameSessionId));
            Parent homeRoot = loader.load();

            // Obtenir la scène actuelle à partir du bouton
            Stage stage = (Stage) createGameButton.getScene().getWindow();

            // Remplacer la scène par la nouvelle vue
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("Chess Game - ");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
