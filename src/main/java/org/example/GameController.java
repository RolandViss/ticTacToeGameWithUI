package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private GameSessionManager sessionManager;

    // ---- DTOs (typed API) ----
    public record StartRequest(String mode, String p1Name, String p2Name) {}
    public record MoveRequest(String sessionId, int row, int col) {}
    public record SessionRequest(String sessionId) {}

    public record GameStateResponse(
            String sessionId,
            String[] board,
            String currentPlayer,
            String p1Name,
            String p2Name,
            String status,
            String winner
    ) {}

    public record ErrorResponse(String error) {}

    // ---- helpers ----
    private GameStateResponse buildState(String sessionId, Game game) {
        String winner = game.getWinnerName();
        return new GameStateResponse(
                sessionId,
                game.getBoardState(),
                game.getCurrentPlayer().toString(),
                game.getPlayerOne().getNamePlayer(),
                game.getPlayerTwo().getNamePlayer(),
                game.getGameStatus(),
                winner
        );
    }

    // ---- endpoints ----

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StartRequest req) {
        String sessionId = sessionManager.createGame(req.p1Name(), req.p2Name(), req.mode());
        Game game = sessionManager.getGame(sessionId);
        return ResponseEntity.ok(buildState(sessionId, game));
    }

    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody MoveRequest req) {
        Game game = sessionManager.getGame(req.sessionId());

        boolean ok = game.applyHumanMove(req.row(), req.col());
        if (!ok) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid move (or not human turn / game over)"));
        }

        return ResponseEntity.ok(buildState(req.sessionId(), game));
    }

    /**
     * For CC mode animation (CPU vs CPU):
     * Call this repeatedly until status != "ongoing".
     */
    @PostMapping("/cpuStep")
    public ResponseEntity<?> cpuStep(@RequestBody SessionRequest req) {
        Game game = sessionManager.getGame(req.sessionId());

        boolean ok = game.cpuStep();
        if (!ok) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Cannot CPU-step (not CPU turn / game over)"));
        }

        return ResponseEntity.ok(buildState(req.sessionId(), game));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody SessionRequest req) {
        sessionManager.removeGame(req.sessionId());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Game reset successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(java.util.Map.of("status", "✅ Tic-Tac-Toe server is running"));
    }
}