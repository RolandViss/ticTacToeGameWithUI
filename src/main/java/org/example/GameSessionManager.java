package org.example;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSessionManager {

    private final Map<String, Game> sessions = new ConcurrentHashMap<>();

    public String createGame(String p1Name, String p2Name, String mode) {
        String m = (mode == null) ? "" : mode.trim().toUpperCase();

        String p1 = (p1Name == null || p1Name.isBlank()) ? "Player 1" : p1Name.trim();
        String p2 = (p2Name == null || p2Name.isBlank()) ? "Player 2" : p2Name.trim();

        Game game;
        switch (m) {
            case "HH" -> game = new Game(
                    new HumanPlayer(p1, Mark.X),
                    new HumanPlayer(p2, Mark.O)
            );
            case "HC" -> game = new Game(
                    new HumanPlayer(p1, Mark.X),
                    new ComputerPlayer("CPU", Mark.O)
            );
            case "CC" -> game = new Game(
                    new ComputerPlayer("CPU X", Mark.X),
                    new ComputerPlayer("CPU O", Mark.O)
            );
            default -> throw new IllegalArgumentException("Invalid game mode: " + m);
        }

        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, game);
        return sessionId;
    }

    public Game getGame(String sessionId) {
        Game game = sessions.get(sessionId);
        if (game == null) throw new IllegalArgumentException("Game session not found: " + sessionId);
        return game;
    }

    public void removeGame(String sessionId) {
        sessions.remove(sessionId);
    }
}