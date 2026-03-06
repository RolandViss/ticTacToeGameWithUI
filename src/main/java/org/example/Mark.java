package org.example;

public enum Mark {

	X("X"), O("O"), EMPTY("_");

	public String value;

	Mark(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
