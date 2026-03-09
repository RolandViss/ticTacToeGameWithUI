package org.example;

import java.util.UUID;

public class Game {
	private String gameId;
	private Player playerOne;
	private Player playerTwo;
	private Mark currentPlayer;
	private Board board;
	private String status;
	private String winner;

	public Game(Player playerOne, Player playerTwo) {
		this.gameId = UUID.randomUUID().toString(); // Generate unique game ID
		this.playerOne = playerOne;
		this.playerTwo = playerTwo;
		this.currentPlayer = Mark.X;
		this.board = new Board();
		this.status = "ongoing"; // Initial game status
		this.winner = null; // No winner at the start
	}

	// Get board state as flat String array for JSON response
	public String[] getBoardState() {
		Mark[][] field = board.getField();
		String[] flat = new String[9];
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				Mark m = field[r][c];
				flat[r * 3 + c] = m == Mark.EMPTY ? "" : m.toString();
			}
		}
		return flat;
	}

	// Apply move from UI (REST API)
	public boolean applyMove(int index) {
		int row = index / 3; // Calculate row based on index
		int col = index % 3; // Calculate column based on index

		Mark[][] field = board.getField();

		// Validate move
		if (row < 0 || row > 2 || col < 0 || col > 2 || field[row][col] != Mark.EMPTY) {
			return false; // Invalid move
		}

		// Apply human move
		field[row][col] = currentPlayer;

		// Check for win or draw
		if (board.checkForWinAndDrow()) {
			if (board.getWinner() != null) {
				winner = currentPlayer.toString();
				status = "win"; // Update status to win
			} else {
				status = "draw"; // Update status to draw
			}
		} else {
			switchTurn(); // Switch to the next player
		}

		return true; // Move applied successfully
	}

	// Switch turn
	private void switchTurn() {
		currentPlayer = (currentPlayer == Mark.X) ? Mark.O : Mark.X;
	}

	// Get current player info
	public Player getCurrentPlayerObj() {
		return (currentPlayer == Mark.X) ? playerOne : playerTwo;
	}

	// Get game status
	public String getGameStatus() {
		return status; // Return current game status
	}

	// Get winner name
	public String getWinnerName() {
		if (winner == null) return null;
		return (winner.equals("X")) ? playerOne.getNamePlayer() : playerTwo.getNamePlayer();
	}

	// Getters for REST API
	public String getGameId() { return gameId; }
	public Mark getCurrentPlayer() { return currentPlayer; }
	public Player getPlayerOne() { return playerOne; }
	public Player getPlayerTwo() { return playerTwo; }
}
