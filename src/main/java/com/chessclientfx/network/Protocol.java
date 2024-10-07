package com.chessclientfx.network;


public class Protocol {

    // --- Commandes envoyées par le client ---

    public static final String CONNECT = "CONNECT";          // Connexion au serveur avec un pseudo
    public static final String CREATE_GAME = "CREATE_GAME";  // Création d'une nouvelle partie
    public static final String JOIN_GAME = "JOIN_GAME";      // Rejoindre une partie existante
    public static final String MOVE_PIECE = "MOVE_PIECE";    // Déplacer une pièce
    public static final String LIST_GAMES = "LIST_GAMES";    // Demander la liste des parties disponibles
    public static final String LEAVE_GAME = "LEAVE_GAME";    // Quitter une partie
    public static final String DISCONNECT = "DISCONNECT";    // Déconnexion du serveur


    public static String formatConnect(String pseudo) {
        return String.format("%s;%s", CONNECT, pseudo);
    }
}