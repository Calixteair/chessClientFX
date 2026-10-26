package com.chessclientfx.controller;

import com.chessclientfx.model.GameInfo;
import com.chessclientfx.model.GameState;
import com.chessclientfx.model.Outcome;
import com.chessclientfx.model.Position;
import com.chessclientfx.network.Protocol;
import com.chessclientfx.network.ServerConnection;
import com.chessclientfx.network.ServerMessage;
import com.chessclientfx.view.BoardCanvas;
import com.chessclientfx.view.Screens;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Partie en cours. Le serveur pousse l'état complet (position, coups légaux, statut) ;
 * ce contrôleur l'affiche et transforme les clics en coups UCI.
 */
public class GameController {

    private static final double BOARD_SIZE = 560;
    private static final Map<String, String> PROMOTIONS = Map.of(
            "Dame", "q", "Tour", "r", "Fou", "b", "Cavalier", "n");

    private final ServerConnection connection;
    private final Screens screens;
    private final String pseudo;
    private final GameInfo info;
    private final BoardCanvas board = new BoardCanvas(BOARD_SIZE);
    private GameState state;
    private String selected;

    @FXML
    private Label titleLabel;
    @FXML
    private Label opponentLabel;
    @FXML
    private Label meLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label detailLabel;
    @FXML
    private StackPane boardHolder;
    @FXML
    private ListView<String> historyList;
    @FXML
    private Button resignButton;
    @FXML
    private Button backButton;

    public GameController(ServerConnection connection, Screens screens, String pseudo, GameInfo info) {
        this.connection = connection;
        this.screens = screens;
        this.pseudo = pseudo;
        this.info = info;
    }

    @FXML
    public void initialize() {
        titleLabel.setText(info.name());
        opponentLabel.setText(info.opponent() + (info.iAmWhite() ? "  ·  noirs" : "  ·  blancs"));
        meLabel.setText(pseudo + (info.iAmWhite() ? "  ·  blancs (vous)" : "  ·  noirs (vous)"));
        board.setWhiteAtBottom(info.iAmWhite());
        board.setOnSquareClicked(this::onSquareClicked);
        boardHolder.getChildren().add(board);
        backButton.setVisible(false);
        backButton.setManaged(false);
        connection.setListener(this::onMessage);
    }

    private void onMessage(ServerMessage message) {
        switch (message.type()) {
            case Protocol.STATE -> update(GameState.from(message));
            case Protocol.OPPONENT_LEFT -> onOpponentLeft(message.field(0));
            case Protocol.ERROR -> detailLabel.setText(message.errorText());
            default -> { }
        }
    }

    /** Le départ de l'adversaire complète le résultat sans l'effacer (déjà dit en cas d'abandon). */
    private void onOpponentLeft(String name) {
        if (state != null && !"ABANDONED".equals(state.status())) {
            detailLabel.setText(detailLabel.getText() + "\n" + name + " a quitté la table.");
        }
    }

    private void update(GameState next) {
        state = next;
        selected = null;
        Position position = Position.fromFen(next.fen());
        String check = next.check() ? position.kingSquare(next.whiteToMove()) : null;
        board.select(null, Set.of());
        board.show(position, next.lastMove(), check);
        historyList.getItems().setAll(historyRows(next.history()));
        historyList.scrollTo(historyList.getItems().size() - 1);
        updateStatus();
    }

    private void updateStatus() {
        boolean over = state.isOver();
        resignButton.setVisible(!over);
        resignButton.setManaged(!over);
        backButton.setVisible(over);
        backButton.setManaged(over);
        statusLabel.getStyleClass().remove("check");
        if (over) {
            statusLabel.setText("Partie terminée");
            detailLabel.setText(Outcome.describe(state, info));
        } else if (isMyTurn()) {
            statusLabel.setText(state.check() ? "Échec !" : "À vous de jouer");
            detailLabel.setText(state.check() ? "Votre roi est attaqué : parez la menace." : "Cliquez une pièce puis sa destination.");
            if (state.check()) {
                statusLabel.getStyleClass().add("check");
            }
        } else {
            statusLabel.setText(info.opponent() + " réfléchit…");
            detailLabel.setText("");
        }
    }

    private boolean isMyTurn() {
        return state != null && !state.isOver() && state.whiteToMove() == info.iAmWhite();
    }

    private static List<String> historyRows(List<String> history) {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < history.size(); i += 2) {
            String white = Outcome.frenchSan(history.get(i));
            String black = i + 1 < history.size() ? Outcome.frenchSan(history.get(i + 1)) : "";
            rows.add(String.format("%3d.   %-10s %s", i / 2 + 1, white, black));
        }
        return rows;
    }

    private void onSquareClicked(String square) {
        if (!isMyTurn()) {
            return;
        }
        if (selected != null && tryMove(selected, square)) {
            return;
        }
        Set<String> targets = targetsFrom(square);
        selected = targets.isEmpty() || square.equals(selected) ? null : square;
        board.select(selected, selected == null ? Set.of() : targets);
    }

    private Set<String> targetsFrom(String from) {
        return state.legalMoves().stream()
                .filter(m -> m.startsWith(from))
                .map(m -> m.substring(2, 4))
                .collect(Collectors.toSet());
    }

    private boolean tryMove(String from, String to) {
        List<String> candidates = state.legalMoves().stream()
                .filter(m -> m.startsWith(from + to))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return false;
        }
        String move = candidates.size() == 1 ? candidates.get(0) : askPromotion(from + to).orElse(null);
        selected = null;
        board.select(null, Set.of());
        if (move != null) {
            connection.send(Protocol.move(move));
        }
        return true;
    }

    private Optional<String> askPromotion(String fromTo) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Dame", List.of("Dame", "Cavalier", "Tour", "Fou"));
        dialog.setTitle("Promotion");
        dialog.setHeaderText("Votre pion arrive sur la dernière rangée.");
        dialog.setContentText("Promouvoir en :");
        return dialog.showAndWait().map(choice -> fromTo + PROMOTIONS.get(choice));
    }

    @FXML
    public void handleResignButton() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Votre adversaire gagnera la partie.");
        confirm.setHeaderText("Abandonner la partie ?");
        confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(ok -> connection.send(Protocol.resign()));
    }

    @FXML
    public void handleBackButton() {
        connection.send(Protocol.leaveGame());
        screens.showHome(pseudo);
    }
}
