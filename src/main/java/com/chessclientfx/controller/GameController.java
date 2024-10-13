package com.chessclientfx.controller;


import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessgame.controller.ChessGameController;
import com.chessgame.model.ChessGame;
import com.chessgame.model.Player;
import com.chessgame.model.pieces.*;
import com.chessgame.utils.Move;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Objects;

public class GameController {

    @FXML
    private Canvas chessBoardCanvas;

    @FXML
    private Label gameIdLabel;

      @FXML
    private Label waitingLabel;

    @FXML
    private Label currentTurnLabel;

    @FXML
    private HBox turnInfoBox;

    @FXML
    private ListView<String> moveHistoryListView;

    @FXML
    private Button leaveGameButton;

    private Boolean gameIsStarted = false;

    private Image whiteKing, blackKing, whiteQueen, blackQueen, whiteRook, blackRook;
    private Image whiteBishop, blackBishop, whiteKnight, blackKnight, whitePawn, blackPawn;

    private PlayerFX playerFX;
    private ChessGameController chessController;
    private String uuidGameSession;
    private ClientSocket clientSocket;

    private ObservableList<String> moveHistory;

    public GameController( ClientSocket clientSocket, PlayerFX playerFX,String uuidGameSession) {
        this.moveHistory = FXCollections.observableArrayList();
        this.clientSocket = clientSocket;
        this.playerFX = playerFX;
        this.uuidGameSession = uuidGameSession;

        loadPieceImages();
    }


    @FXML
    public void initialize() throws IOException {
        if (!gameIsStarted) {
            waitForOpponent();
        } else {
            System.out.println("La partie a déjà commencé");
            if (playerFX.isWhite) {
                this.chessController = new ChessGameController(playerFX.getPseudo(), "Adversaire");
            } else {
                this.chessController = new ChessGameController( "Adversaire",playerFX.getPseudo());
            }
            startGameUI();
        }
    }

    private void waitForOpponent() throws IOException {
        // Logique pour attendre que l'adversaire rejoigne la partie
        // Tu devrais probablement utiliser `clientSocket` pour recevoir des messages du serveur
        waitingLabel.setVisible(true);
        chessBoardCanvas.setVisible(false);
        turnInfoBox.setVisible(false);
        moveHistoryListView.setVisible(false);
        System.out.println("waitForOpponent: ");
        new Thread(() -> {
            try {
                String response = clientSocket.receiveMessage(); // Attendre la réponse du serveur
                System.out.println("Réponse du serveur: " + response);
                if (response.equals("START")) {
                    gameIsStarted = true;

                    if (playerFX.isWhite) {
                        this.chessController = new ChessGameController(playerFX.getPseudo(), "Adversaire");
                    } else {
                        this.chessController = new ChessGameController("Adversaire", playerFX.getPseudo());
                    }
                    System.out.println("La partie a commencé");
                    // Mettre à jour l'interface utilisateur sur le thread de l'application
                    Platform.runLater(this::startGameUI);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Gérer les erreurs de manière appropriée (afficher un message d'erreur, etc.)
            }
        }).start();

    }

    private void startGameUI() {
        waitingLabel.setVisible(false);
        chessBoardCanvas.setVisible(true);
        turnInfoBox.setVisible(true);
        moveHistoryListView.setVisible(true);
        gameIdLabel.setText(this.chessController.chessGame.getBlackPlayer().getName()+  " vs " + this.chessController.chessGame.getWhitePlayer().getName()); // Remplacer par l'ID de la partie réelle
        updateTurnLabel();
        initializeChessBoard();
        updateMoveHistory();
    }

    private void loadPieceImages() {
        whiteKing = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhiteKing.png")));
        blackKing = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackKing.png")));
        whiteQueen = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhiteQueen.png")));
        blackQueen = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackQueen.png")));
        whiteRook = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhiteRook.png")));
        blackRook = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackRook.png")));
        whiteBishop = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhiteBishop.png")));
        blackBishop = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackBishop.png")));
        whiteKnight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhiteKnight.png")));
        blackKnight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackKnight.png")));
        whitePawn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/white/WhitePawn.png")));
        blackPawn = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pieces/black/BlackPawn.png")));

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
        String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H"};

        // Dessiner le plateau d'échecs
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (white) {
                    gc.setFill(Color.BEIGE);
                } else {
                    gc.setFill(Color.BROWN);
                }
                gc.fillRect(i * tileSize + tileSize, j * tileSize + tileSize, tileSize, tileSize);  // Décaler le plateau pour laisser de l'espace pour les annotations
                white = !white;
            }
            white = !white;
        }

        // Dessiner les chiffres (1-8) à gauche et à droite du plateau
/*        gc.setFill(Color.BLACK);
        gc.setFont(new Font(20)); // Taille de police ajustée
        for (int i = 0; i < 8; i++) {
            int yPosition = i * tileSize + tileSize + tileSize / 2;
            gc.fillText(String.valueOf(8 - i), 5, yPosition); // À gauche du plateau
            gc.fillText(String.valueOf(8 - i), 8 * tileSize + tileSize + 10, yPosition); // À droite du plateau
        }

        // Dessiner les lettres (A-H) en haut et en bas du plateau
        for (int i = 0; i < 8; i++) {
            int xPosition = i * tileSize + tileSize + tileSize / 2 - 10; // Centrer le texte par rapport à chaque case
            gc.fillText(columns[i], xPosition, 20); // En haut du plateau
            gc.fillText(columns[i], xPosition, 8 * tileSize + tileSize + 30); // En bas du plateau
        }*/
    }



    private void drawPieces(GraphicsContext gc) {

        this.chessController.chessGame.getWhitePlayer().getPieces().forEach(piece -> {
            int x = piece.getX() * 75;
            int y = piece.getY() * 75;

            // Dessiner la pièce sur le plateau
            drawPiece(gc, piece, x, y);
        });
        this.chessController.chessGame.getBlackPlayer().getPieces().forEach(piece -> {
            int x = piece.getX() * 75;
            int y = piece.getY() * 75;

            // Dessiner la pièce sur le plateau
            drawPiece(gc, piece, x, y);
        });

    }

    private void drawPiece(GraphicsContext gc, Piece piece, int x, int y) {
        Image pieceImage = null;

        if (piece instanceof King) {
            pieceImage = piece.isWhite() ? whiteKing : blackKing;
        } else if (piece instanceof Queen) {
            pieceImage = piece.isWhite() ? whiteQueen : blackQueen;
        } else if (piece instanceof Rook) {
            pieceImage = piece.isWhite() ? whiteRook : blackRook;
        } else if (piece instanceof Bishop) {
            pieceImage = piece.isWhite() ? whiteBishop : blackBishop;
        } else if (piece instanceof Knight) {
            pieceImage = piece.isWhite() ? whiteKnight : blackKnight;
        } else if (piece instanceof Pawn) {
            pieceImage = piece.isWhite() ? whitePawn : blackPawn;
        }

        // Dessiner l'image de la pièce sur le canvas
        if (pieceImage != null) {
            gc.drawImage(pieceImage, x, y, 75, 75); // Adapter la taille si nécessaire
        }
    }

    private void handleChessBoardClick(MouseEvent event) {
        int row = (int) (event.getY() / 75);
        int col = (int) (event.getX() / 75);
        System.out.println("Clique sur la case: " + row + ", " + col);

        // Logique pour gérer les clics sur le plateau d'échecs (par exemple, sélectionner une pièce ou faire un mouvement)
        // Utiliser `chessController` pour effectuer les mouvements et mettre à jour l'interface utilisateur
    }

    private void updateTurnLabel() {
        currentTurnLabel.setText(chessController.chessGame.getCurrentPlayer().getName()); // Remplacer par le nom du joueur actuel
    }

    private void updateMoveHistory() {
        moveHistory.clear();
        for (Move move : this.chessController.moveHistory) {
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

