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

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public Mark getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getCurrentPlayerObj() {
        return (currentPlayer == Mark.X) ? playerOne : playerTwo;
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

    /**
     * Human move endpoint.
     * - Only valid if it's currently a HumanPlayer turn.
     * - After a human move, if next is CPU and game is still ongoing, CPU auto-plays exactly ONE move.
     */
    public boolean applyHumanMove(int row, int col) {
        if (board.isGameOver()) return false;

        Player current = getCurrentPlayerObj();
        if (!(current instanceof HumanPlayer)) return false;

        if (!isValidCell(row, col)) return false;

        Mark[][] f = board.getField();
        if (f[row][col] != Mark.EMPTY) return false;

        // place
        f[row][col] = currentPlayer;

        // if game ends, stop
        if (board.isGameOver()) return true;

        // switch to next
        switchTurn();

        // auto CPU once (for HC)
        Player next = getCurrentPlayerObj();
        if (next instanceof ComputerPlayer && !board.isGameOver()) {
            next.doMove(f);
            if (!board.isGameOver()) {
                switchTurn();
            }
        }

        return true;
    }

    /**
     * CPU step endpoint (for CC mode animation).
     * - Only valid if it's currently a ComputerPlayer turn.
     * - Plays exactly ONE computer move.
     */
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