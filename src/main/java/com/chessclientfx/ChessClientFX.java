package com.chessclientfx;

import com.chessclientfx.network.ServerConnection;
import com.chessclientfx.view.Screens;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Client JavaFX. Serveur : variables CHESS_HOST (localhost), CHESS_TCP_PORT (8090) et
 * CHESS_TLS (false), ou arguments --host=… --port=… --tls=true.
 * Serveur public : --host=chessfx.calixteair.fr --port=443 --tls=true
 */
public class ChessClientFX extends Application {

    private ServerConnection connection;

    @Override
    public void start(Stage stage) {
        String host = option("host", "CHESS_HOST", "localhost");
        String port = option("port", "CHESS_TCP_PORT", "8090");
        boolean tls = Boolean.parseBoolean(option("tls", "CHESS_TLS", "false"));
        try {
            connection = new ServerConnection(validHost(host), validPort(port), tls);
        } catch (IOException | IllegalArgumentException e) {
            Screens.fatal("Impossible de joindre le serveur " + host + ":" + port + ".\nLancez d'abord chessServer.");
            return;
        }
        stage.setMinWidth(760);
        stage.setMinHeight(640);
        new Screens(stage, connection).showConnection();
        stage.show();
    }

    /** Nom d'hôte ou adresse IP, rien d'autre (pas d'URL, pas d'espace). */
    private static String validHost(String host) {
        if (!host.matches("[A-Za-z0-9.:\\[\\]-]{1,253}")) {
            throw new IllegalArgumentException("Hôte invalide : " + host);
        }
        return host;
    }

    private static int validPort(String port) {
        int value = Integer.parseInt(port);
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException("Port invalide : " + port);
        }
        return value;
    }

    private String option(String name, String envVariable, String fallback) {
        String fromArgs = getParameters().getNamed().get(name);
        if (fromArgs != null) {
            return fromArgs;
        }
        String fromEnv = System.getenv(envVariable);
        return fromEnv == null || fromEnv.isBlank() ? fallback : fromEnv;
    }

    @Override
    public void stop() {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
