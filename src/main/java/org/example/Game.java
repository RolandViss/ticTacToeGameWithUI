package org.example;

public class Game {

    private final Player playerOne; // X
    private final Player playerTwo; // O
    private Mark currentPlayer;     // X or O
    private final Board board = new Board();

    public Game(Player playerOne, Player playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.currentPlayer = Mark.X;
    }

    public Player getPlayerOne() { return playerOne; }
    public Player getPlayerTwo() { return playerTwo; }
    public Mark getCurrentPlayer() { return currentPlayer; }

    public Player getCurrentPlayerObj() {
        return (currentPlayer == Mark.X) ? playerOne : playerTwo;
    }

    public String[] getBoardState() {
        Mark[][] f = board.getField();
        String[] flat = new String[9];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Mark m = f[r][c];
                flat[r * 3 + c] = (m == Mark.EMPTY) ? "" : m.toString();
            }
        }
        return flat;
    }

    public String getGameStatus() {
        if (board.getWinner() != null) return "win";
        if (board.isDraw()) return "draw";
        return "ongoing";
    }

    public String getWinnerName() {
        Mark w = board.getWinner();
        if (w == null) return null;
        return (w == Mark.X) ? playerOne.getNamePlayer() : playerTwo.getNamePlayer();
    }

    /** Human move only. If next player is CPU (HC), CPU auto-plays exactly one move. */
    public boolean applyHumanMove(int row, int col) {
        if (board.isGameOver()) return false;

        Player current = getCurrentPlayerObj();
        if (!(current instanceof HumanPlayer)) return false;

        if (!isValidCell(row, col)) return false;

        Mark[][] f = board.getField();
        if (f[row][col] != Mark.EMPTY) return false;

        f[row][col] = currentPlayer;

        if (board.isGameOver()) return true;

        switchTurn();

        Player next = getCurrentPlayerObj();
        if (next instanceof ComputerPlayer && !board.isGameOver()) {
            next.doMove(f);
            if (!board.isGameOver()) {
                switchTurn();
            }
        }
        return true;
    }

    /** CPU step (one move) used for autoplay CC. */
    public boolean cpuStep() {
        if (board.isGameOver()) return false;

        Player current = getCurrentPlayerObj();
        if (!(current instanceof ComputerPlayer)) return false;

        Mark[][] f = board.getField();
        current.doMove(f);

        if (!board.isGameOver()) {
            switchTurn();
        }
        return true;
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row <= 2 && col >= 0 && col <= 2;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == Mark.X) ? Mark.O : Mark.X;
    }
}