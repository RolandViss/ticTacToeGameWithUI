package org.example;

public abstract class Player {
	private String namePlayer;
	private Mark mark;

	public Player(String namePlayer, Mark mark) {
		this.namePlayer = namePlayer;
		this.mark = mark;
	}

	public abstract void doMove(Mark[][] field);

	public String getNamePlayer() {
		return namePlayer;
	}

	public Mark getMark() {
		return mark;
	}

	@Override
	public String toString() {
		return mark.toString();
	}
}
