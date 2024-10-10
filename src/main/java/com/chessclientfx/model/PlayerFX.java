package com.chessclientfx.model;


import com.chessgame.model.Player;

public class PlayerFX {


    private String pseudo;

    private String password;

    private Game Currentgame;

    private Boolean inGame;

    private Player player;

    public PlayerFX(String pseudo){
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

    public void setCurrentGame(Game game) {
        this.Currentgame = game;
    }
}
