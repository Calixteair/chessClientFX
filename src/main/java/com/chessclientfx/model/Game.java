package com.chessclientfx.model;

import java.util.ArrayList;
import java.util.List;

import com.chessgame.model.Board;
import com.chessgame.model.ChessGame;
import com.chessgame.utils.Move;

public class Game {

    private String id;
    private boolean whiteTurn;
    private List<Move> moveHistory;
    public ChessGame game;
    private String gameName;

    public Game(String id) {
        this.id = id;
        this.whiteTurn = true;
        this.moveHistory = new ArrayList<>();
    }

    public Game(String id, String gameName) {
        this.id = id;
        this.gameName = gameName;
        this.whiteTurn = true; // Les blancs commencent toujours
        this.moveHistory = new ArrayList<>();
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }


    public String getId() {
        return id;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

}