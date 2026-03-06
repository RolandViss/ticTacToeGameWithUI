package org.example;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GameSessionManager {
    private Map<String, Game> sessions = new HashMap<>();

    public String createGame(String p1Name, String p2Name, String mode) {
        mode = (mode == null) ? "" : mode.trim().toUpperCase();

        Game game;
        switch (mode) {
            case "HH" -> game = new Game(new HumanPlayer(p1Name,Mark.O), new HumanPlayer(p1Name,Mark.O));
            case "HC" -> game = new Game(new HumanPlayer(p1Name,Mark.O), new ComputerPlayer(p1Name,Mark.O));
            case "CC" -> game = new Game(new ComputerPlayer(p1Name,Mark.O), new ComputerPlayer(p1Name,Mark.O));
            default -> throw new IllegalArgumentException("Invalid game mode: " + mode);
        }

        String sessionId = java.util.UUID.randomUUID().toString();
        sessions.put(sessionId, game);
        return sessionId;
    }


    public Game getGame(String sessionId) {
        Game game = sessions.get(sessionId);
        if (game == null) {
            throw new IllegalArgumentException("Game session not found: " + sessionId);
        }
        return game;
    }

    public void removeGame(String sessionId) {
        sessions.remove(sessionId);
    }
}
