package com.chessclientfx.model;


import com.chessgame.model.Player;

public class PlayerFX {


    private String pseudo;

    private String password;

    private Boolean inGame;

    public Player player;

    public boolean isWhite;

    public PlayerFX(String pseudo){
        this.pseudo = pseudo;
        this.inGame = false;
    }

    public void setPlayer(Player player) {
        this.player = player;
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

}
