package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private GameSessionManager sessionManager;

    // ---- DTOs ----
    public record StartRequest(String mode, String p1Name, String p2Name) {}
    public record MoveRequest(String sessionId, int row, int col) {}
    public record SessionRequest(String sessionId) {}
    public record ErrorResponse(String error) {}

    public record GameStateResponse(
            String sessionId,
            String[] board,
            String currentPlayer,
            String p1Name,
            String p2Name,
            String status,
            String winner
    ) {}

    // ---- SSE subscribers per session ----
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    // ---- Prevent multiple autoplay loops per session ----
    private final ConcurrentHashMap<String, AtomicBoolean> autoplayRunning = new ConcurrentHashMap<>();

    // ---- Background executor ----
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // ---- Helpers ----
    private GameStateResponse buildState(String sessionId, Game game) {
        return new GameStateResponse(
                sessionId,
                game.getBoardState(),
                game.getCurrentPlayer().toString(),
                game.getPlayerOne().getNamePlayer(),
                game.getPlayerTwo().getNamePlayer(),
                game.getGameStatus(),
                game.getWinnerName()
        );
    }

    private void sendToAll(String sessionId, String eventName, Object data) {
        List<SseEmitter> list = subscribers.get(sessionId);
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
            } catch (IOException ex) {
                list.remove(emitter);
                try { emitter.complete(); } catch (Exception ignored) {}
            }
        }
    }

    private void closeAll(String sessionId) {
        List<SseEmitter> list = subscribers.remove(sessionId);
        if (list == null) return;

        for (SseEmitter emitter : list) {
            try { emitter.send(SseEmitter.event().name("done").data("done")); } catch (Exception ignored) {}
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    // ---- endpoints ----
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "✅ Tic-Tac-Toe server is running"));
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StartRequest req) {
        try {
            String sessionId = sessionManager.createGame(req.p1Name(), req.p2Name(), req.mode());
            Game game = sessionManager.getGame(sessionId);
            return ResponseEntity.ok(buildState(sessionId, game));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * SSE stream. UI connects once:
     * GET /api/game/stream?sessionId=...
     *
     * Server pushes:
     *  event: state  data: GameStateResponse
     *  event: done   data: "done"
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String sessionId) {
        // validate session exists
        Game game = sessionManager.getGame(sessionId);

        // no timeout
        SseEmitter emitter = new SseEmitter(0L);

        subscribers.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            List<SseEmitter> list = subscribers.get(sessionId);
            if (list != null) list.remove(emitter);
        });
        emitter.onTimeout(() -> {
            List<SseEmitter> list = subscribers.get(sessionId);
            if (list != null) list.remove(emitter);
            try { emitter.complete(); } catch (Exception ignored) {}
        });
        emitter.onError(e -> {
            List<SseEmitter> list = subscribers.get(sessionId);
            if (list != null) list.remove(emitter);
            try { emitter.completeWithError(e); } catch (Exception ignored) {}
        });

        // push an initial snapshot immediately
        GameStateResponse state = buildState(sessionId, game);
        try { emitter.send(SseEmitter.event().name("state").data(state, MediaType.APPLICATION_JSON)); }
        catch (IOException ignored) {}

        return emitter;
    }

    /**
     * Human move (HH/HC). After move, server pushes updated state to SSE subscribers.
     */
    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody MoveRequest req) {
        try {
            Game game = sessionManager.getGame(req.sessionId());

            boolean ok = game.applyHumanMove(req.row(), req.col());
            if (!ok) return ResponseEntity.badRequest().body(new ErrorResponse("Invalid move (or not human turn / game over)"));

            GameStateResponse state = buildState(req.sessionId(), game);
            sendToAll(req.sessionId(), "state", state);

            if (!"ongoing".equalsIgnoreCase(state.status())) {
                closeAll(req.sessionId());
            }

            return ResponseEntity.ok(state);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Start CPU-vs-CPU autoplay server-side and PUSH each step to SSE.
     * UI calls once for CC mode. No polling needed.
     *
     * Returns 202 immediately if autoplay successfully started.
     */
    @PostMapping("/autoPlay")
    public ResponseEntity<?> autoPlay(@RequestBody SessionRequest req) {
        try {
            final String sessionId = req.sessionId();
            final Game game = sessionManager.getGame(sessionId);

            AtomicBoolean flag = autoplayRunning.computeIfAbsent(sessionId, k -> new AtomicBoolean(false));
            if (!flag.compareAndSet(false, true)) {
                return ResponseEntity.status(409).body(new ErrorResponse("Autoplay already running for this session"));
            }

            executor.submit(() -> {
                try {
                    // sanity: must be CPU turn to do cpuStep; CC mode is CPU on both turns.
                    while ("ongoing".equalsIgnoreCase(game.getGameStatus())) {
                        boolean stepped = game.cpuStep();
                        if (!stepped) break;

                        GameStateResponse state = buildState(sessionId, game);
                        sendToAll(sessionId, "state", state);

                        // pace animation
                        try { Thread.sleep(450); } catch (InterruptedException ignored) {}
                    }

                    GameStateResponse finalState = buildState(sessionId, game);
                    sendToAll(sessionId, "state", finalState);
                    closeAll(sessionId);

                } finally {
                    flag.set(false);
                }
            });

            return ResponseEntity.accepted().body(Map.of("message", "Autoplay started"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    /**
     * Reset session and close SSE.
     */
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody SessionRequest req) {
        String sessionId = req.sessionId();
        closeAll(sessionId);
        autoplayRunning.remove(sessionId);
        sessionManager.removeGame(sessionId);
        return ResponseEntity.ok(Map.of("message", "Game reset successfully"));
    }
}