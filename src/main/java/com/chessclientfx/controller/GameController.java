package com.chessclientfx.controller;


import com.chessclientfx.model.Game;
import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessgame.utils.Move;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GameController {

    @FXML
    private Canvas chessBoardCanvas;

    @FXML
    private Label gameIdLabel;

    @FXML
    private Label currentTurnLabel;

    @FXML
    private ListView<String> moveHistoryListView;

    @FXML
    private Button leaveGameButton;

    private Boolean gameIsStarted = false;

    private Game currentGame;
    private PlayerFX playerFX;
    private ClientSocket clientSocket;

    private ObservableList<String> moveHistory;

    public GameController( Game game, ClientSocket clientSocket, PlayerFX playerFX) {
        this.currentGame = game;
        this.moveHistory = FXCollections.observableArrayList();
        this.clientSocket = clientSocket;
        this.playerFX = playerFX;
    }

    @FXML
    public void initialize() {
        gameIdLabel.setText(String.valueOf(currentGame.getId()));
        updateTurnLabel();
        initializeChessBoard();
        updateMoveHistory();
    }

    private void initializeChessBoard() {
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        drawChessBoard(gc);

        // Dessiner les pièces initiales du jeu
        drawPieces(gc);

        // Ajouter un écouteur pour les clics sur le plateau
        chessBoardCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleChessBoardClick);
    }


    private void drawChessBoard(GraphicsContext gc) {
        int tileSize = 75;
        boolean white = true;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (white) {
                    gc.setFill(Color.BEIGE);
                } else {
                    gc.setFill(Color.BROWN);
                }
                gc.fillRect(i * tileSize, j * tileSize, tileSize, tileSize);
                white = !white;
            }
            white = !white;
        }
    }

    private void drawPieces(GraphicsContext gc) {
        // Logique pour dessiner les pièces sur le plateau à partir du modèle `currentGame`
        // Tu devrais récupérer l'état des pièces depuis `currentGame` et dessiner les pièces correspondantes
    }

    private void handleChessBoardClick(MouseEvent event) {
        int row = (int) (event.getY() / 75);
        int col = (int) (event.getX() / 75);
        System.out.println("Clique sur la case: " + row + ", " + col);

        // Logique pour gérer les clics sur le plateau d'échecs (par exemple, sélectionner une pièce ou faire un mouvement)
        // Utiliser `chessController` pour effectuer les mouvements et mettre à jour l'interface utilisateur
    }

    private void updateTurnLabel() {
        currentTurnLabel.setText(currentGame.isWhiteTurn() ? "Blancs" : "Noirs");
    }

    private void updateMoveHistory() {
        moveHistory.clear();
        for (Move move : currentGame.getMoveHistory()) {
            moveHistory.add(move.toString());
        }
        moveHistoryListView.setItems(moveHistory);
    }

    @FXML
    public void handleLeaveGameButton(ActionEvent event) {
        System.out.println("Quitter la partie");
        // Logique pour quitter la partie et retourner à une autre vue
    }
}

