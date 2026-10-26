package com.chessclientfx.view;

import com.chessclientfx.model.Position;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Échiquier dessiné sur un canvas, aux couleurs du client web (encre et papier).
 * Le canvas ne connaît pas les règles : il affiche une position et des surlignages.
 */
public class BoardCanvas extends Canvas {

    private static final Color LIGHT = Color.web("#efe6d5");
    private static final Color DARK = Color.web("#6684a6");
    private static final Color LAST_LIGHT = Color.web("#ead8a3");
    private static final Color LAST_DARK = Color.web("#b89a68");
    private static final Color SELECTED = Color.web("#2f4fc0", 0.32);
    private static final Color HINT = Color.web("#232a3a", 0.28);
    private static final Color CHECK = Color.web("#d93a2b");
    private static final Color FRAME = Color.web("#2a3242");

    private final Map<Character, Image> images = loadImages();
    private boolean whiteAtBottom = true;
    private Position position = Position.fromFen("8/8/8/8/8/8/8/8 w - - 0 1");
    private String lastMove;
    private String checkSquare;
    private String selected;
    private Set<String> targets = Set.of();
    private Consumer<String> onSquareClicked;

    public BoardCanvas(double size) {
        super(size, size);
        setOnMouseClicked(e -> {
            if (onSquareClicked != null) {
                onSquareClicked.accept(squareAt(e.getX(), e.getY()));
            }
        });
    }

    public void setOnSquareClicked(Consumer<String> handler) {
        this.onSquareClicked = handler;
    }

    public void setWhiteAtBottom(boolean whiteAtBottom) {
        this.whiteAtBottom = whiteAtBottom;
    }

    public void show(Position position, String lastMove, String checkSquare) {
        this.position = position;
        this.lastMove = lastMove;
        this.checkSquare = checkSquare;
        redraw();
    }

    public void select(String square, Set<String> targets) {
        this.selected = square;
        this.targets = targets;
        redraw();
    }

    private double cell() {
        return getWidth() / 8;
    }

    /** Case algébrique sous le point (x, y) du canvas. */
    private String squareAt(double x, double y) {
        int col = (int) Math.min(7, Math.max(0, x / cell()));
        int row = (int) Math.min(7, Math.max(0, y / cell()));
        int file = whiteAtBottom ? col : 7 - col;
        int rank = whiteAtBottom ? 7 - row : row;
        return Position.squareName(file, rank);
    }

    public void redraw() {
        GraphicsContext g = getGraphicsContext2D();
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                drawSquare(g, file, rank);
            }
        }
        g.setStroke(FRAME);
        g.setLineWidth(2);
        g.strokeRect(1, 1, getWidth() - 2, getHeight() - 2);
    }

    private void drawSquare(GraphicsContext g, int file, int rank) {
        String name = Position.squareName(file, rank);
        double c = cell();
        double x = (whiteAtBottom ? file : 7 - file) * c;
        double y = (whiteAtBottom ? 7 - rank : rank) * c;
        boolean dark = (file + rank) % 2 == 0;
        boolean last = lastMove != null && (lastMove.startsWith(name) || lastMove.substring(2, 4).equals(name));

        g.setFill(last ? (dark ? LAST_DARK : LAST_LIGHT) : (dark ? DARK : LIGHT));
        g.fillRect(x, y, c, c);
        if (name.equals(selected)) {
            g.setFill(SELECTED);
            g.fillRect(x, y, c, c);
        }
        if (name.equals(checkSquare)) {
            g.setFill(new RadialGradient(0, 0, x + c / 2, y + c / 2, c / 1.4, false, CycleMethod.NO_CYCLE,
                    new Stop(0, CHECK), new Stop(0.45, CHECK.deriveColor(0, 1, 1, 0.55)), new Stop(1, Color.TRANSPARENT)));
            g.fillRect(x, y, c, c);
        }
        drawCoordinates(g, file, rank, x, y, dark);
        Image piece = images.get(position.at(file, rank));
        if (piece != null) {
            g.drawImage(piece, x + c * 0.06, y + c * 0.06, c * 0.88, c * 0.88);
        }
        if (targets.contains(name)) {
            drawHint(g, x, y, !position.isEmpty(file, rank));
        }
    }

    private void drawHint(GraphicsContext g, double x, double y, boolean capture) {
        double c = cell();
        if (capture) {
            g.setStroke(HINT);
            g.setLineWidth(c * 0.07);
            g.strokeOval(x + c * 0.07, y + c * 0.07, c * 0.86, c * 0.86);
        } else {
            g.setFill(HINT);
            g.fillOval(x + c * 0.37, y + c * 0.37, c * 0.26, c * 0.26);
        }
    }

    private void drawCoordinates(GraphicsContext g, int file, int rank, double x, double y, boolean dark) {
        double c = cell();
        int col = whiteAtBottom ? file : 7 - file;
        int row = whiteAtBottom ? 7 - rank : rank;
        g.setFill(dark ? LIGHT : DARK);
        g.setFont(Font.font("System", FontWeight.BOLD, Math.max(9, c * 0.16)));
        if (col == 0) {
            g.fillText(String.valueOf(rank + 1), x + c * 0.06, y + c * 0.2);
        }
        if (row == 7) {
            g.fillText(String.valueOf((char) ('a' + file)), x + c * 0.82, y + c * 0.94);
        }
    }

    private static Map<Character, Image> loadImages() {
        Map<Character, Image> map = new HashMap<>();
        String[][] names = {
                {"K", "King"}, {"Q", "Queen"}, {"R", "Rook"}, {"B", "Bishop"}, {"N", "Knight"}, {"P", "Pawn"}};
        for (String[] n : names) {
            map.put(n[0].charAt(0), load("white/White" + n[1]));
            map.put(n[0].toLowerCase().charAt(0), load("black/Black" + n[1]));
        }
        return map;
    }

    private static Image load(String path) {
        return new Image(Objects.requireNonNull(
                BoardCanvas.class.getResourceAsStream("/img/pieces/" + path + ".png"), path));
    }
}
