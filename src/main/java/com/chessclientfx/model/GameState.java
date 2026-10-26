package com.chessclientfx.model;

import com.chessclientfx.network.ServerMessage;

import java.util.Arrays;
import java.util.List;

/**
 * État complet poussé par le serveur après chaque coup :
 * STATE;fen;statut;vainqueur;échec;dernierCoup;historiqueSAN;coupsLégaux
 */
public record GameState(
        String fen,
        boolean whiteToMove,
        String status,
        Boolean winnerIsWhite,
        boolean check,
        String lastMove,
        List<String> history,
        List<String> legalMoves) {

    public static GameState from(ServerMessage m) {
        String fen = m.field(0);
        String winner = m.field(2);
        return new GameState(
                fen,
                !fen.contains(" b "),
                m.field(1),
                "w".equals(winner) ? Boolean.TRUE : "b".equals(winner) ? Boolean.FALSE : null,
                "1".equals(m.field(3)),
                "-".equals(m.field(4)) ? null : m.field(4),
                words(m.field(5)),
                words(m.field(6)));
    }

    private static List<String> words(String field) {
        return field.isBlank() ? List.of() : Arrays.asList(field.trim().split(" "));
    }

    public boolean isOver() {
        return !"ONGOING".equals(status);
    }
}
