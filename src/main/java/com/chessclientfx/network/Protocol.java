package com.chessclientfx.network;

/**
 * Commandes du protocole texte du serveur (voir chessServer : network/Protocol.java).
 * Un message = une ligne, champs séparés par ';'.
 */
public final class Protocol {

    public static final String WELCOME = "WELCOME";
    public static final String GAMES = "GAMES";
    public static final String GAME_CREATED = "GAME_CREATED";
    public static final String GAME_START = "GAME_START";
    public static final String STATE = "STATE";
    public static final String OPPONENT_LEFT = "OPPONENT_LEFT";
    public static final String ERROR = "ERROR";

    private Protocol() {
    }

    public static String connect(String pseudo) {
        return "CONNECT;" + pseudo;
    }

    public static String createGame(String name) {
        return "CREATE_GAME;" + name;
    }

    public static String joinGame(String id) {
        return "JOIN_GAME;" + id;
    }

    public static String move(String uci) {
        return "MOVE;" + uci;
    }

    public static String resign() {
        return "RESIGN";
    }

    public static String leaveGame() {
        return "LEAVE_GAME";
    }

    public static String disconnect() {
        return "DISCONNECT";
    }
}
