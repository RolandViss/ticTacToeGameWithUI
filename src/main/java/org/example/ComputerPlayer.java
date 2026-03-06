package org.example;

public class ComputerPlayer extends Player {

	public ComputerPlayer(String namePlayer, Mark mark) {
		super(namePlayer, mark);
	}

	@Override
	public void doMove(Mark[][] field) {
		// AI logic: find best move
		int[] bestMove = findBestMove(field);
		if (bestMove != null) {
			field[bestMove[0]][bestMove[1]] = getMark();
		}
	}

	private int[] findBestMove(Mark[][] field) {
		// Win if possible
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (field[r][c] == Mark.EMPTY) {
					field[r][c] = getMark();
					// Check if this move wins (you'll need a helper method in Board)
					field[r][c] = Mark.EMPTY;
					// If winning move found, return it
				}
			}
		}

		// Block opponent if possible
		Mark opponent = getMark() == Mark.X ? Mark.O : Mark.X;
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (field[r][c] == Mark.EMPTY) {
					field[r][c] = opponent;
					// Check if opponent would win
					field[r][c] = Mark.EMPTY;
					// If blocking move needed, return it
				}
			}
		}

		// Take center if available
		if (field[1][1] == Mark.EMPTY) {
			return new int[]{1, 1};
		}

		// Take any corner
		int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
		for (int[] corner : corners) {
			if (field[corner[0]][corner[1]] == Mark.EMPTY) {
				return corner;
			}
		}

		// Take any remaining space
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (field[r][c] == Mark.EMPTY) {
					return new int[]{r, c};
				}
			}
		}

		return null; // No moves available
	}

	@Override
	public String toString() {
		return "ComputerPlayer{" +
				"name='" + getNamePlayer() + '\'' +
				", mark=" + getMark() +
				'}';
	}
}
