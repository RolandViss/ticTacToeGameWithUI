package org.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private GameSessionManager sessionManager;

    // Start a new game
    @PostMapping("/start")
    public Map<String, Object> startGame(@RequestBody Map<String, String> request) {
        String mode = request.get("mode");
        String p1Name = request.getOrDefault("p1Name", "Player 1");
        String p2Name = request.getOrDefault("p2Name", "Player 2");

        String sessionId = sessionManager.createGame(p1Name, p2Name, mode);
        Game game = sessionManager.getGame(sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("board", game.getBoardState());
        response.put("currentPlayer", game.getCurrentPlayer().toString());
        response.put("p1Name", game.getPlayerOne().getNamePlayer());
        response.put("p2Name", game.getPlayerTwo().getNamePlayer());
        response.put("status", "ongoing");
        return response;
    }

    // Make a move
    @PostMapping("/move")
    public Map<String, Object> makeMove(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        int row = ((Number) request.get("row")).intValue();
        int col = ((Number) request.get("col")).intValue();

        Game game = sessionManager.getGame(sessionId);

        // Apply move
        if (!game.applyMove(row, col)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid move");
            return error;
        }

        // Check game status
        String status = game.getGameStatus();
        String winner = game.getWinnerName();

        Map<String, Object> response = new HashMap<>();
        response.put("board", game.getBoardState());
        response.put("currentPlayer", game.getCurrentPlayer().toString());
        response.put("status", status);
        if (winner != null) {
            response.put("winner", winner);
        }
        return response;
    }

    // Reset game
    @PostMapping("/reset")
    public Map<String, String> resetGame(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        sessionManager.removeGame(sessionId);
        return Map.of("message", "Game reset successfully");
    }

    // Health check
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "✅ Tic-Tac-Toe server is running");
    }
}
