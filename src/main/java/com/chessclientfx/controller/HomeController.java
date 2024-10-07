package com.chessclientfx.controller;

import com.chessclientfx.model.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import com.chessclientfx.network.ClientSocket;

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

    public Player player;

    public HomeController(ClientSocket clientSocket, Player player) {
        this.clientSocket = clientSocket;
        this.player = player;
    }

    @FXML
    public void initialize() {
        usernameLabel.setText("pseudo : " + player.getPseudo());
        // Remplir la liste des parties avec des données mock pour le moment
        ObservableList<String> games = FXCollections.observableArrayList(
            "Partie 1 - 1 joueur",
            "Partie 2 - 2 joueurs",
            "Partie 3 - 1 joueur"
        );
        gameListView.setItems(games);

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
        // Redirection vers la vue de création de partie (create_game_view)
    }

    @FXML
    private void handleLogoutButton() {
        System.out.println("Déconnexion");
        // Logique pour déconnecter le client
    }

    public void handleJoinGameButton(String game) {
        System.out.println("Connexion à la partie : " + game);
        // Logique pour se connecter à la partie sélectionnée
        // Vérifier si la partie est pleine ou non, puis se connecter si possible
    }

    public void handleViewGamesButton(ActionEvent actionEvent) {
        System.out.println("Affichage des parties disponibles");
        // Logique pour demander la liste des parties disponibles au serveur
    }

}
