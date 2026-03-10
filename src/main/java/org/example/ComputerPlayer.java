package org.example;

public class ComputerPlayer extends Player {

    public ComputerPlayer(String namePlayer, Mark mark) {
        super(namePlayer, mark);
    }

    @Override
    public void doMove(Mark[][] field) {
        int[] bestMove = findBestMove(field);
        if (bestMove != null) {
            field[bestMove[0]][bestMove[1]] = getMark();
        }
    }

    private int[] findBestMove(Mark[][] field) {
        Mark me = getMark();
        Mark opp = (me == Mark.X) ? Mark.O : Mark.X;

        // 1) win
        int[] move = findMoveThatWins(field, me);
        if (move != null) return move;

        // 2) block
        move = findMoveThatWins(field, opp);
        if (move != null) return move;

        // 3) center
        if (field[1][1] == Mark.EMPTY) return new int[]{1, 1};

        // 4) corners
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] c : corners) {
            if (field[c[0]][c[1]] == Mark.EMPTY) return c;
        }

        // 5) any empty
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (field[r][c] == Mark.EMPTY) return new int[]{r, c};
            }
        }
        return null;
    }

    private int[] findMoveThatWins(Mark[][] field, Mark markToTest) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (field[r][c] != Mark.EMPTY) continue;

                field[r][c] = markToTest;
                boolean wins = Board.isWinningState(field, markToTest);
                field[r][c] = Mark.EMPTY;

                if (wins) return new int[]{r, c};
            }
        }
        return null;
    }
}