package com.chessclientfx.model;

import java.util.Map;

/**
 * Texte de fin de partie (même formulation que le client web).
 */
public final class Outcome {

    private static final Map<String, String> DRAWS = Map.of(
            "STALEMATE", "Pat : partie nulle.",
            "DRAW_FIFTY_MOVES", "Partie nulle : cinquante coups sans prise ni mouvement de pion.",
            "DRAW_REPETITION", "Partie nulle : la même position est apparue trois fois.",
            "DRAW_INSUFFICIENT_MATERIAL", "Partie nulle : plus assez de matériel pour mater.");

    private Outcome() {
    }

    public static String describe(GameState state, GameInfo info) {
        if (!state.isOver()) {
            return "";
        }
        if (DRAWS.containsKey(state.status()) || state.winnerIsWhite() == null) {
            return "½-½ · " + DRAWS.getOrDefault(state.status(), "Partie nulle.");
        }
        boolean whiteWon = state.winnerIsWhite();
        String score = whiteWon ? "1-0" : "0-1";
        String loser = info.nameOf(!whiteWon);
        String verdict = whiteWon == info.iAmWhite() ? "Vous gagnez." : info.nameOf(whiteWon) + " gagne.";
        switch (state.status()) {
            case "CHECKMATE":
                return score + " · Échec et mat. " + verdict;
            case "RESIGNATION":
                return score + " · " + loser + " abandonne. " + verdict;
            default:
                return score + " · " + loser + " a quitté la table. " + verdict;
        }
    }

    /** SAN anglaise → française : Nf3 → Cf3, exd8=Q → exd8=D. */
    public static String frenchSan(String san) {
        StringBuilder sb = new StringBuilder(san.length());
        for (char c : san.toCharArray()) {
            sb.append(switch (c) {
                case 'K' -> 'R';
                case 'Q' -> 'D';
                case 'R' -> 'T';
                case 'B' -> 'F';
                case 'N' -> 'C';
                default -> c;
            });
        }
        return sb.toString();
    }
}
