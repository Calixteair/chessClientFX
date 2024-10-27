package com.chessclientfx.controller;


import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessgame.controller.ChessGameController;
import com.chessgame.model.ChessGame;
import com.chessgame.model.Player;
import com.chessgame.model.pieces.*;
import com.chessgame.utils.Move;
import com.chessgame.utils.TypePiece;
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
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class GameController {

    @FXML
    public Canvas columnsCanvas;
    public Label errorLabel;

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

    private boolean pieceSelected = false;
    private Piece pieceSelectedP;

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
                playerFX.myTurn = true;
                initTurn();
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
        columnsCanvas.setVisible(false);
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
                        playerFX.myTurn = true;
                        initTurn();
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

    private void initTurn() {
        List<Piece> pieces = chessController.chessGame.getCurrentPlayer().getPieces();
        pieces.forEach(
                piece -> piece.findValidMove(chessController.getBoard())
        );
    }

    private void startGameUI() {
        waitingLabel.setVisible(false);
        columnsCanvas.setVisible(true);
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
        GraphicsContext cgc = columnsCanvas.getGraphicsContext2D();
        drawColumnsName(cgc);
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        drawChessBoard(gc);

        // Dessiner les pièces initiales du jeu
        drawPieces(gc);

        // Ajouter un écouteur pour les clics sur le plateau
        chessBoardCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleChessBoardClick);
    }

    private void drawColumnsName(GraphicsContext cgc) {
        String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int tileSize = 75;
        //draw the columns name
        cgc.setFill(Color.BLACK);
        cgc.setFont(new Font(20)); // Taille de police ajustée
        for (int i = 0; i < 8; i++) {
            int xPosition = i * tileSize + tileSize / 2 - 10; // Centrer le texte
            cgc.fillText(columns[i], xPosition, 8 * tileSize + 20); // En bas
            cgc.fillText(columns[i], xPosition, 20); // En haut
        }
    }


    private void drawChessBoard(GraphicsContext gc) {
        int tileSize = 75;
        boolean white = true;


        // Dessiner le plateau d'échecs
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


        // Dessiner les chiffres (1-8) à gauche et à droite du plateau
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(20)); // Taille de police ajustée
        for (int i = 0; i < 8; i++) {
            int yPosition = i * tileSize + tileSize / 2;
            gc.fillText(String.valueOf(i+1), 5, yPosition); // À gauche
            gc.fillText(String.valueOf(i+1), 8 * tileSize + 5, yPosition); // À droite
        }
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

    private void chosePiece(MouseEvent event){
        int row = (int) (event.getY() / 75);
        int col = (int) (event.getX() / 75);
        System.out.println("Clique sur la case: " + row + ", " + col);

        if(playerFX.myTurn){
            System.out.println("my turn");

            pieceSelectedP = chessController.getBoard().getPiece(col,row);
            if (pieceSelectedP.getType() != TypePiece.EMPTY && pieceSelectedP.isWhite() && playerFX.isWhite){
                this.pieceSelected = true;
                pieceSelectedP.findValidMove(chessController.getBoard());
                List<Move> possibleMoves = this.chessController.selectPiece(col, row);
                System.out.println(possibleMoves.size());
                if (!possibleMoves.isEmpty()) {
                    GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
                    // mettre en vert les case possible
                    gc.setFill(Color.GREEN);
                    gc.setGlobalAlpha(0.3);
                    possibleMoves.forEach(
                            move -> {
                                gc.fillRect(move.getEndX() * 75 + 7.5, move.getEndY() * 75 + 7.5, 60, 60);
                                System.out.println(move.getEndY() + " " + move.getEndX());
                            }
                    );
                    gc.setGlobalAlpha(1);
                } else {
                    this.errorLabel.setText("Aucun mouvement possible pour cette pièce");
                }
            }

        } else {
            this.errorLabel.setText("Ce n'est pas votre tour");
        }

    }

    private boolean choseMove(int row, int col){
        // mettre a jour le plateau
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 600, 600);
        drawChessBoard(gc);
        this.pieceSelected = false;
        if( this.chessController.movePiece(new Move(pieceSelectedP.getX(), pieceSelectedP.getY(), col, row))){
            this.pieceSelectedP = null;
            drawPieces(gc);
            this.updateMoveHistory();
            playerFX.myTurn = chessController.getCurrentPlayer().getName().equals(playerFX.getPseudo());
            if (!playerFX.myTurn){
                this.waitMyTurn();
            }
            this.updateTurnLabel();
            return true;
        } else {
            this.errorLabel.setText("Mouvement invalide");
            drawPieces(gc);
            return false;
        }


    }

    private void handleChessBoardClick(MouseEvent event) {
        int row = (int) (event.getY() / 75);
        int col = (int) (event.getX() / 75);
        System.out.println("Clique sur la case: " + row + ", " + col);

        if(playerFX.myTurn) {
            System.out.println("my turn");
            if (!pieceSelected){
                this.chosePiece(event);
            }
            else{
                this.choseMove(row , col);
            }

        }
        else {
            this.errorLabel.setText("Ce n'est pas votre tour");

        }
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

    private void waitMyTurn(){
         new Thread(() -> {
            try {
                String response = clientSocket.receiveMessage(); // Attendre la réponse du serveur
                System.out.println("Réponse du serveur: " + response);
                if(response.equals("YOUR_TURN")){
                    playerFX.myTurn = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
         });
    }
}

