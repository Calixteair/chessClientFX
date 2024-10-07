package com.chessclientfx.model;

public class Player {


    private String pseudo;

    private String password;

    private Game Currentgame;

    private Boolean inGame;

    public Player(String pseudo){
        this.pseudo = pseudo;
        this.inGame = false;
    }


    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Game getCurrentgame() {
        return Currentgame;
    }
}
