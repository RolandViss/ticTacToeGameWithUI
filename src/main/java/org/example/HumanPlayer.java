package org.example;

public class HumanPlayer extends Player {

	public HumanPlayer(String namePlayer, Mark mark) {
		super(namePlayer, mark);
	}

	@Override
	public void doMove(Mark[][] field) {
		// Human moves are handled by the UI/REST API
		// This method is called by the game logic but the actual move
		// comes from the frontend via GameController.makeMove()
	}

	@Override
	public String toString() {
		return "HumanPlayer{" +
				"name='" + getNamePlayer() + '\'' +
				", mark=" + getMark() +
				'}';
	}
}
