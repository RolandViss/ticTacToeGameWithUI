package org.example;
public class Game {
	private Player playerOne;
	private Player playerTwo;
	private Mark currentPlayer;
	private Board board = new Board();
//	private String gameMode; // "hvh", "hvc", "cvc"

	public Game(Player playerOne, Player playerTwo) {
		this.playerOne = playerOne;
		this.playerTwo = playerTwo;
//		this.gameMode = gameMode;
		this.currentPlayer = Mark.X;
	}

	// ... existing methods ...

	// NEW: Get board state as flat String array for JSON response
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

	// NEW: Apply move from UI
	public boolean applyMove(int row, int col) {
		Mark[][] field = board.getField();

		// Validate move
		if (row < 0 || row > 2 || col < 0 || col > 2 || field[row][col] != Mark.EMPTY) {
			return false;
		}

		// Apply human move
		field[row][col] = currentPlayer;
		switchTurn();

		// If next player is computer, auto-play
		Player next = (currentPlayer == Mark.X) ? playerOne : playerTwo;
		if (next instanceof ComputerPlayer && !board.checkForWinAndDrow()) {
			next.doMove(field);
			switchTurn();
		}

		return true;
	}

	// NEW: Switch turn
	private void switchTurn() {
		currentPlayer = (currentPlayer == Mark.X) ? Mark.O : Mark.X;
	}

	// NEW: Get current player info
	public Player getCurrentPlayerObj() {
		return (currentPlayer == Mark.X) ? playerOne : playerTwo;
	}

	// NEW: Get game status
	public String getGameStatus() {
		if (!board.checkForWinAndDrow()) {
			return "ongoing";
		}
		Mark winner = board.getWinner();
		return winner != null ? "win" : "draw";
	}

	// NEW: Get winner name
	public String getWinnerName() {
		Mark winner = board.getWinner();
		if (winner == null) return null;
		return (winner == Mark.X) ? playerOne.getNamePlayer() : playerTwo.getNamePlayer();
	}

	public Mark getCurrentPlayer() { return currentPlayer; }
	public Player getPlayerOne() { return playerOne; }
	public Player getPlayerTwo() { return playerTwo; }
}

