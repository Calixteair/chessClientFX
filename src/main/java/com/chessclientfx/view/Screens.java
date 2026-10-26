package com.chessclientfx.view;

import com.chessclientfx.controller.ConnectionController;
import com.chessclientfx.controller.GameController;
import com.chessclientfx.controller.HomeController;
import com.chessclientfx.model.GameInfo;
import com.chessclientfx.network.ServerConnection;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Navigation entre les écrans, tous affichés dans la même fenêtre.
 */
public final class Screens {

    private final Stage stage;
    private final ServerConnection connection;

    public Screens(Stage stage, ServerConnection connection) {
        this.stage = stage;
        this.connection = connection;
        connection.setOnDisconnect(this::connectionLost);
    }

    public void showConnection() {
        show("/fxml/connection_view.fxml", new ConnectionController(connection, this), "Échecs · connexion");
    }

    public void showHome(String pseudo) {
        show("/fxml/home_view.fxml", new HomeController(connection, this, pseudo), "Échecs · salon");
    }

    public void showGame(String pseudo, GameInfo info) {
        show("/fxml/game_view.fxml", new GameController(connection, this, pseudo, info), "Échecs · " + info.name());
    }

    private void show(String fxml, Object controller, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/chess.css")).toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            throw new UncheckedIOException("Vue introuvable : " + fxml, e);
        }
    }

    private void connectionLost() {
        fatal("La connexion au serveur a été perdue.");
    }

    /** Erreur bloquante : message puis fermeture de l'application. */
    public static void fatal(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Serveur injoignable");
        alert.showAndWait();
        Platform.exit();
    }
}
