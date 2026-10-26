package com.chessclientfx.controller;


import com.chessclientfx.model.PlayerFX;
import com.chessclientfx.network.ClientSocket;
import com.chessclientfx.network.Protocol;
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
import java.sql.SQLOutput;
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
    private int rowStart;
    private int colStart;

    public GameController( ClientSocket clientSocket, PlayerFX playerFX,String uuidGameSession, boolean gameIsStarted) {
        this.moveHistory = FXCollections.observableArrayList();
        this.clientSocket = clientSocket;
        this.playerFX = playerFX;
        this.uuidGameSession = uuidGameSession;
        this.gameIsStarted = gameIsStarted;

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
                System.out.println("noir");
                this.chessController = new ChessGameController( "Adversaire",playerFX.getPseudo());
                waitMyTurn();
            }
            startGameUI();
        }
    }

    private void waitForOpponent(){
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
                        waitMyTurn();
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

    private void startGameUI(){
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


    private void initializeChessBoard(){
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
            if ( piece instanceof Decoy){
                //mettre l'image du pions mais en rouge
                pieceImage = piece.isWhite() ? whitePawn : blackPawn;
            } else {
                 pieceImage = piece.isWhite() ? whitePawn : blackPawn;
            }
        }

        // Dessiner l'image de la pièce sur le canvas
        if (pieceImage != null) {
            gc.drawImage(pieceImage, x, y, 75, 75); // Adapter la taille si nécessaire
        }
    }

    private void chosePiece(MouseEvent event){
        int row = (int) (event.getY() / 75);
        int col = (int) (event.getX() / 75);
        if(playerFX.myTurn){
            pieceSelectedP = chessController.getBoard().getPiece(col,row);
            System.out.println(pieceSelectedP.getType());
            System.out.println(pieceSelectedP.isWhite());
            System.out.println(playerFX.isWhite);
            if (pieceSelectedP.getType() != TypePiece.EMPTY && (pieceSelectedP.isWhite() == playerFX.isWhite)){
                this.pieceSelected = true;
                pieceSelectedP.findValidMove(chessController.getBoard());
                List<Move> possibleMoves = this.chessController.selectPiece(col, row);
                System.out.println(possibleMoves.size());
                if (!possibleMoves.isEmpty()) {
                    rowStart = row;
                    colStart = col;
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
                    this.errorLabel.setText("");
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
        if( this.chessController.movePiece(new Move(pieceSelectedP.getX(), pieceSelectedP.getY(), col, row))){
            drawPieces(gc);
            this.updateMoveHistory();
            playerFX.myTurn = chessController.getCurrentPlayer().getName().equals(playerFX.getPseudo());
            this.updateTurnLabel();
            return true;
        } else {
            this.errorLabel.setText("Mouvement invalide");
            drawPieces(gc);
            this.pieceSelectedP = null;
            this.pieceSelected = false;
            rowStart = -1;
            colStart = -1;
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
                if(this.choseMove(row , col)){
                    this.errorLabel.setText("");
                    try {
                        if (pieceSelectedP.getType() == TypePiece.PAWN && Math.abs(rowStart - row) == 2){
                            System.out.println("i do pawndash");
                            System.out.println(Protocol.formatMovePiece(colStart,rowStart,col,row,true));
                            clientSocket.sendMessage(Protocol.formatMovePiece(colStart,rowStart,col,row,true));
                        } else {
                            System.out.println("i dont pawndash");
                            clientSocket.sendMessage(Protocol.formatMovePiece(colStart,rowStart,col,row));
                        }
                        this.pieceSelectedP = null;
                        this.pieceSelected = false;
                        rowStart = -1;
                        colStart = -1;
                        String response = clientSocket.receiveMessage(); // Attendre la réponse du serveur
                        System.out.println("Réponse du serveur: " + response);
                        if(response.equals("OK")){
                            System.out.println("Move OK");
                            System.out.println(playerFX.myTurn);
                            if (!playerFX.myTurn){
                                this.waitMyTurn();
                            }
                        }
                        else{
                            //TODO : bon deplacement client mais pas serveur donc undo move client et redemander le deplacement
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }




                }
            }

        }
        else {
            this.errorLabel.setText("Ce n'est pas votre tour");

        }
    }

    private void updateTurnLabel() {
        Platform.runLater(() -> {
            currentTurnLabel.setText(chessController.chessGame.getCurrentPlayer().getName());
        });
    }

    private void updateMoveHistory() {
        Platform.runLater(() -> {
            moveHistory.clear();
            for (Move move : this.chessController.getBoard().getMoveHistory()) {
                moveHistory.add(move.toString());
            }
            moveHistoryListView.setItems(moveHistory);
        });
    }

    private void refreshGame(){
        GraphicsContext gc = chessBoardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 600, 600);
        drawChessBoard(gc);
        drawPieces(gc);
    }

    @FXML
    public void handleLeaveGameButton(ActionEvent event) {
        System.out.println("Quitter la partie");
        // Logique pour quitter la partie et retourner à une autre vue
    }

    private void waitMyTurn(){
        System.out.println("waitMyTurn");
         new Thread(() -> {
            try {
                    //wait 0.5 seconde

                String response = clientSocket.receiveMessage(); // Attendre la réponse du serveur
                if(response.equals("START")){response = clientSocket.receiveMessage();}
                System.out.println("wait");
                System.out.println("Réponse du serveur: " + response);
                if(response.equals("MOVE_ADDV")) {
                    response = clientSocket.receiveMessage();
                    String[] moveS = response.split(";");
                    String pawnDash = (moveS.length == 5) ? moveS[4] : "false";
                    System.out.println("received move");
                    System.out.println(pawnDash);
                    //print moveS
                    for (String s : moveS) {
                        System.out.println(s);
                    }
                    Move move = new Move(Integer.parseInt(moveS[0]), Integer.parseInt(moveS[1]), Integer.parseInt(moveS[2]), Integer.parseInt(moveS[3]), Boolean.parseBoolean(pawnDash));
                    Piece piece = chessController.getBoard().getPiece(move.getStartX(), move.getStartY());
                    piece.findValidMove(chessController.getBoard());
                    this.chessController.movePiece(move);
                    System.out.println(chessController.chessGame.getCurrentPlayer().getName());
                    this.updateMoveHistory();
                    this.refreshGame();
                    playerFX.myTurn = chessController.getCurrentPlayer().getName().equals(playerFX.getPseudo());
                    this.updateTurnLabel();
                    initTurn();
                }
         } catch (IOException e) {
                throw new RuntimeException(e);
            }
         }).start();
    }
}

