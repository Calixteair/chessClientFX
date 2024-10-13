package com.chessclientfx.utils;

public class JoinGameParser {

    public static String[] parseJoinGame(String message) {
        return message.split(";");
    }
}
