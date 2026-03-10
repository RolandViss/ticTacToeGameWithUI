package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumanPlayerTest {

    @Test
    void constructor_shouldSetNameAndMark() {
        HumanPlayer player = new HumanPlayer("Alice", Mark.X);

        assertEquals("Alice", player.getNamePlayer());
        assertEquals(Mark.X, player.getMark());
    }

    @Test
    void doMove_shouldNotThrow_andShouldNotChangeField() {
        HumanPlayer player = new HumanPlayer("Bob", Mark.O);

        Mark[][] field = new Mark[3][3];
        field[0][0] = Mark.X;
        field[1][1] = Mark.O;

        // snapshot
        Mark[][] before = copy(field);

        assertDoesNotThrow(() -> player.doMove(field));
        assertBoardEquals(before, field);
    }

    @Test
    void doMove_shouldAcceptNullField_withoutThrowing() {
        HumanPlayer player = new HumanPlayer("Bob", Mark.O);

        // The current implementation ignores the argument, so null should be fine.
        assertDoesNotThrow(() -> player.doMove(null));
    }

    @Test
    void toString_shouldMatchExpectedFormat() {
        HumanPlayer player = new HumanPlayer("Charlie", Mark.X);

        String expected = "HumanPlayer{" +
                "name='Charlie'" +
                ", mark=" + Mark.X +
                '}';

        assertEquals(expected, player.toString());
    }

    // --- helpers ---

    private static Mark[][] copy(Mark[][] src) {
        if (src == null) return null;
        Mark[][] out = new Mark[src.length][];
        for (int r = 0; r < src.length; r++) {
            out[r] = src[r] == null ? null : src[r].clone();
        }
        return out;
    }

    private static void assertBoardEquals(Mark[][] expected, Mark[][] actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.length, actual.length, "Row count differs");
        for (int r = 0; r < expected.length; r++) {
            assertArrayEquals(expected[r], actual[r], "Row " + r + " differs");
        }
    }
}