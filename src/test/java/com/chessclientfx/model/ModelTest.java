package com.chessclientfx.model;

import com.chessclientfx.network.ServerMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private static final GameInfo INFO = GameInfo.from(ServerMessage.parse("GAME_START;ab12;Blitz;alice;bob;b"));

    @Test
    void parsesGameStart() {
        assertEquals("alice", INFO.white());
        assertFalse(INFO.iAmWhite());
        assertEquals("alice", INFO.opponent());
    }

    @Test
    void parsesStateAndDescribesCheckmate() {
        GameState state = GameState.from(ServerMessage.parse(
                "STATE;rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3;CHECKMATE;b;1;d8h4;f3 e5 g4 Qh4#;"));
        assertTrue(state.whiteToMove());
        assertTrue(state.isOver());
        assertEquals(Boolean.FALSE, state.winnerIsWhite());
        assertEquals("d8h4", state.lastMove());
        assertEquals(List.of("f3", "e5", "g4", "Qh4#"), state.history());
        assertTrue(state.legalMoves().isEmpty());
        assertEquals("0-1 · Échec et mat. Vous gagnez.", Outcome.describe(state, INFO));
    }

    @Test
    void readsPositionFromFen() {
        Position p = Position.fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
        assertEquals('P', p.at(4, 3));
        assertTrue(p.isEmpty(4, 1));
        assertEquals("e8", p.kingSquare(false));
    }

    @Test
    void translatesSanToFrench() {
        assertEquals("Cxf3+", Outcome.frenchSan("Nxf3+"));
        assertEquals("exd8=D#", Outcome.frenchSan("exd8=Q#"));
        assertEquals("O-O", Outcome.frenchSan("O-O"));
    }

    @Test
    void errorTextKeepsSemicolons() {
        assertEquals("a;b", ServerMessage.parse("ERROR;CODE;a;b").errorText());
    }
}
