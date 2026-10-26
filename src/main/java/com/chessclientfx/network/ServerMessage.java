package com.chessclientfx.network;

import java.util.Arrays;

/**
 * Message reçu du serveur : un type et ses champs bruts.
 */
public record ServerMessage(String type, String[] fields) {

    public static ServerMessage parse(String line) {
        String[] parts = line.split(";", -1);
        return new ServerMessage(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
    }

    /** Champ n° {@code index}, ou chaîne vide s'il est absent. */
    public String field(int index) {
        return index < fields.length ? fields[index] : "";
    }

    /** Texte d'une erreur : tout ce qui suit le code (le message peut contenir des ';'). */
    public String errorText() {
        return fields.length < 2 ? "" : String.join(";", Arrays.copyOfRange(fields, 1, fields.length));
    }

    public boolean is(String expectedType) {
        return type.equals(expectedType);
    }
}
