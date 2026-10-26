package com.chessclientfx.network;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Connexion TCP au serveur. Un thread lit les lignes et les transmet, sur le thread JavaFX,
 * à l'écran actif (un seul écouteur à la fois : celui de la vue affichée).
 */
public class ServerConnection {

    private final Socket socket;
    private final PrintWriter out;
    private volatile Consumer<ServerMessage> listener = m -> { };
    private volatile Runnable onDisconnect = () -> { };

    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        Thread reader = new Thread(this::readLoop, "server-reader");
        reader.setDaemon(true);
        reader.start();
    }

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                ServerMessage message = ServerMessage.parse(line);
                Platform.runLater(() -> listener.accept(message));
            }
        } catch (IOException e) {
            // Fermeture du socket : traitée ci-dessous.
        }
        Platform.runLater(() -> onDisconnect.run());
    }

    public void setListener(Consumer<ServerMessage> listener) {
        this.listener = listener;
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void send(String line) {
        synchronized (out) {
            out.println(line);
        }
    }

    public void close() {
        try {
            send(Protocol.disconnect());
            socket.close();
        } catch (IOException e) {
            // Déjà fermé.
        }
    }
}
