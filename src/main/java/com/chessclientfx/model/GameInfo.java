package com.chessclientfx.model;

import com.chessclientfx.network.ServerMessage;

/**
 * Partie commencée : GAME_START;id;nom;blancs;noirs;w|b
 */
public record GameInfo(String id, String name, String white, String black, boolean iAmWhite) {

    public static GameInfo from(ServerMessage m) {
        return new GameInfo(m.field(0), m.field(1), m.field(2), m.field(3), "w".equals(m.field(4)));
    }

    public String nameOf(boolean whiteSide) {
        return whiteSide ? white : black;
    }

    public String opponent() {
        return nameOf(!iAmWhite);
    }
}
