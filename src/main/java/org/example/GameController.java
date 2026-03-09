package org.example;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private Map<String, Game> games = new HashMap<>();

    @PostMapping("/start")
    public Game startGame(@RequestBody PlayerNames playerNames) {
        Player playerOne;
        playerOne = new Player("", Mark.X) {
            @Override
            public void doMove(Mark[][] field) {

            }
        };
        Player playerTwo;
        playerTwo = new Player("", Mark.O) {
            @Override
            public void doMove(Mark[][] field) {

            }
        };
        Game game = new Game(playerOne, playerTwo);
        games.put(game.getGameId(), game);
        return game;
    }

    @PostMapping("/{gameId}/move")
    public Game makeMove(@PathVariable String gameId, @RequestBody MoveRequest moveRequest) {
        Game game = games.get(gameId);
        if (game != null) {
            boolean validMove = game.applyMove(moveRequest.getIndex());
            // Optionally check if the game is over after the move
            if (!validMove) {
                throw new IllegalArgumentException("Invalid move. Please try again.");
            }
        } else {
            throw new IllegalArgumentException("Game not found.");
        }
        return game;
    }

    @GetMapping("/{gameId}")
    public Game getGameState(@PathVariable String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found.");
        }
        return game;
    }

    @PostMapping("/{gameId}/reset")
    public Game resetGame(@PathVariable String gameId) {
        Game game = new Game(games.get(gameId).getPlayerOne(), games.get(gameId).getPlayerTwo());
        games.put(gameId, game);
        return game;
    }
}
