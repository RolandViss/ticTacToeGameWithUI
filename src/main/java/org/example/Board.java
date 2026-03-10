package org.example;

public class Board {

    private final Mark[][] field = new Mark[3][3];

    public Board() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                field[r][c] = Mark.EMPTY;
            }
        }
    }

    public Mark[][] getField() {
        return field;
    }

    /** @return Mark.X or Mark.O if winner exists; otherwise null. */
    public Mark getWinner() {
        // rows
        for (int r = 0; r < 3; r++) {
            if (field[r][0] != Mark.EMPTY && field[r][0] == field[r][1] && field[r][1] == field[r][2]) {
                return field[r][0];
            }
        }
        // cols
        for (int c = 0; c < 3; c++) {
            if (field[0][c] != Mark.EMPTY && field[0][c] == field[1][c] && field[1][c] == field[2][c]) {
                return field[0][c];
            }
        }
        // diagonals
        if (field[0][0] != Mark.EMPTY && field[0][0] == field[1][1] && field[1][1] == field[2][2]) {
            return field[0][0];
        }
        if (field[0][2] != Mark.EMPTY && field[0][2] == field[1][1] && field[1][1] == field[2][0]) {
            return field[0][2];
        }
        return null;
    }

    public boolean isDraw() {
        if (getWinner() != null) return false;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (field[r][c] == Mark.EMPTY) return false;
            }
        }
        return true;
    }

    public boolean isGameOver() {
        return getWinner() != null || isDraw();
    }

    public static boolean isWinningState(Mark[][] f, Mark m) {
        // rows
        for (int r = 0; r < 3; r++) {
            if (f[r][0] == m && f[r][1] == m && f[r][2] == m) return true;
        }
        // cols
        for (int c = 0; c < 3; c++) {
            if (f[0][c] == m && f[1][c] == m && f[2][c] == m) return true;
        }
        // diagonals
        if (f[0][0] == m && f[1][1] == m && f[2][2] == m) return true;
        if (f[0][2] == m && f[1][1] == m && f[2][0] == m) return true;
        return false;
    }
}