package com.chessclientfx.model;

/**
 * Placement des pièces lu depuis une FEN. Case (file, rank) avec file 0 = a et rank 0 = rangée 1.
 * Une pièce est sa lettre FEN (majuscule = blanc), ' ' pour une case vide.
 */
public final class Position {

    private final char[][] squares = new char[8][8];

    private Position() {
    }

    public static Position fromFen(String fen) {
        Position p = new Position();
        String[] rows = fen.split(" ")[0].split("/");
        for (int i = 0; i < 8 && i < rows.length; i++) {
            int rank = 7 - i;
            int file = 0;
            for (char c : rows[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    for (int n = 0; n < c - '0'; n++) {
                        p.squares[file++][rank] = ' ';
                    }
                } else if (file < 8) {
                    p.squares[file++][rank] = c;
                }
            }
        }
        return p;
    }

    public char at(int file, int rank) {
        return squares[file][rank];
    }

    public boolean isEmpty(int file, int rank) {
        return squares[file][rank] == ' ';
    }

    /** Case du roi de la couleur donnée, en notation algébrique, ou null. */
    public String kingSquare(boolean white) {
        char king = white ? 'K' : 'k';
        for (int f = 0; f < 8; f++) {
            for (int r = 0; r < 8; r++) {
                if (squares[f][r] == king) {
                    return squareName(f, r);
                }
            }
        }
        return null;
    }

    public static String squareName(int file, int rank) {
        return "" + (char) ('a' + file) + (rank + 1);
    }
}
