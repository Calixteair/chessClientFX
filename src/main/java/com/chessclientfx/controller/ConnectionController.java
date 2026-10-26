package com.chessclientfx.controller;

import com.chessclientfx.network.Protocol;
import com.chessclientfx.network.ServerConnection;
import com.chessclientfx.network.ServerMessage;
import com.chessclientfx.view.Screens;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Écran d'accueil : choix du pseudo.
 */
public class ConnectionController {

    private static final String PSEUDO_PATTERN = "[A-Za-z0-9_-]{1,20}";

    private final ServerConnection connection;
    private final Screens screens;
    private boolean requested;

    @FXML
    private TextField usernameField;
    @FXML
    private Button connectButton;
    @FXML
    private Label errorLabel;

    public ConnectionController(ServerConnection connection, Screens screens) {
        this.connection = connection;
        this.screens = screens;
    }

    @FXML
    public void initialize() {
        connection.setListener(this::onMessage);
    }

    @FXML
    public void handleConnectButton() {
        String pseudo = usernameField.getText().trim();
        if (!pseudo.matches(PSEUDO_PATTERN)) {
            errorLabel.setText("Pseudo : 1 à 20 caractères parmi lettres, chiffres, _ et -");
            return;
        }
        requested = true;
        connectButton.setDisable(true);
        connection.send(Protocol.connect(pseudo));
    }

    private void onMessage(ServerMessage message) {
        if (message.is(Protocol.WELCOME)) {
            screens.showHome(message.field(0));
        } else if (message.is(Protocol.ERROR) && requested) {
            errorLabel.setText(message.errorText());
            connectButton.setDisable(false);
        }
    }
}
