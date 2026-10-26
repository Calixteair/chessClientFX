package com.chessclientfx.controller;

import com.chessclientfx.model.GameInfo;
import com.chessclientfx.network.Protocol;
import com.chessclientfx.network.ServerConnection;
import com.chessclientfx.network.ServerMessage;
import com.chessclientfx.view.Screens;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Salon : liste des parties en attente (mise à jour par le serveur), création d'une partie.
 */
public class HomeController {

    /** Partie en attente : GAMES;id,nom,créateur|… */
    public record LobbyGame(String id, String name, String creator) {
    }

    private final ServerConnection connection;
    private final Screens screens;
    private final String pseudo;

    @FXML
    private Label usernameLabel;
    @FXML
    private TextField gameNameField;
    @FXML
    private Button createGameButton;
    @FXML
    private ListView<LobbyGame> gameListView;
    @FXML
    private Button joinButton;
    @FXML
    private Label emptyLabel;
    @FXML
    private VBox lobbyBox;
    @FXML
    private VBox waitingBox;
    @FXML
    private Label waitingLabel;
    @FXML
    private Label errorLabel;

    public HomeController(ServerConnection connection, Screens screens, String pseudo) {
        this.connection = connection;
        this.screens = screens;
        this.pseudo = pseudo;
    }

    @FXML
    public void initialize() {
        usernameLabel.setText("Bonjour " + pseudo);
        gameNameField.setText((pseudo.matches("(?i)[aeiouyh].*") ? "Partie d'" : "Partie de ") + pseudo);
        gameListView.setCellFactory(list -> new GameCell());
        joinButton.disableProperty().bind(gameListView.getSelectionModel().selectedItemProperty().isNull());
        gameListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleJoinButton();
            }
        });
        showWaiting(null);
        connection.setListener(this::onMessage);
        connection.send("LIST_GAMES");
    }

    private void onMessage(ServerMessage message) {
        switch (message.type()) {
            case Protocol.GAMES -> showGames(message.field(0));
            case Protocol.GAME_CREATED -> showWaiting(message.field(1));
            case Protocol.GAME_START -> screens.showGame(pseudo, GameInfo.from(message));
            case Protocol.ERROR -> errorLabel.setText(message.errorText());
            default -> { }
        }
    }

    private void showGames(String field) {
        gameListView.getItems().clear();
        if (!field.isBlank()) {
            for (String entry : field.split("\\|")) {
                String[] parts = entry.split(",");
                if (parts.length == 3) {
                    gameListView.getItems().add(new LobbyGame(parts[0], parts[1], parts[2]));
                }
            }
        }
        emptyLabel.setVisible(gameListView.getItems().isEmpty());
    }

    private void showWaiting(String gameName) {
        boolean waiting = gameName != null;
        waitingBox.setVisible(waiting);
        waitingBox.setManaged(waiting);
        lobbyBox.setDisable(waiting);
        if (waiting) {
            waitingLabel.setText("« " + gameName + " » attend un adversaire…");
        }
    }

    @FXML
    public void handleCreateGameButton() {
        errorLabel.setText("");
        connection.send(Protocol.createGame(gameNameField.getText().trim()));
    }

    @FXML
    public void handleJoinButton() {
        LobbyGame game = gameListView.getSelectionModel().getSelectedItem();
        if (game != null && !game.creator().equals(pseudo)) {
            errorLabel.setText("");
            connection.send(Protocol.joinGame(game.id()));
        }
    }

    @FXML
    public void handleCancelButton() {
        connection.send(Protocol.leaveGame());
        showWaiting(null);
    }

    private static final class GameCell extends ListCell<LobbyGame> {
        @Override
        protected void updateItem(LobbyGame game, boolean empty) {
            super.updateItem(game, empty);
            setText(empty || game == null ? null : game.name() + "   ·   proposée par " + game.creator());
        }
    }
}
