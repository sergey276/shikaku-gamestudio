package sk.tuke.gamestudio.game.shikaku.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    public void boardInitializationTest() {
        Board board = new PuzzleFactory().createSampleBoard();

        assertEquals(3, board.getRowCount());
        assertEquals(4, board.getColumnCount());
        assertEquals(5, board.getClues().size());
    }

    @Test
    public void findExistingClueTest() {
        Board board = new PuzzleFactory().createSampleBoard();

        Clue clue = board.findClueAt(new Position(0, 0));

        assertNotNull(clue);
        assertEquals(4, clue.getValue());
    }

    @Test
    public void findMissingClueTest() {
        Board board = new PuzzleFactory().createSampleBoard();

        Clue clue = board.findClueAt(new Position(1, 1));

        assertNull(clue);
    }

    @Test
    public void addPlacementTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rectangle = new RectangleArea(new Position(0, 0), new Position(1, 1));

        board.setPlacement(clue, rectangle);

        assertEquals(rectangle, board.getPlacement(clue));
        assertEquals(1, board.getPlacements().size());
    }

    @Test
    public void removePlacementTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rectangle = new RectangleArea(new Position(0, 0), new Position(1, 1));

        board.setPlacement(clue, rectangle);
        board.removePlacement(clue);

        assertNull(board.getPlacement(clue));
        assertTrue(board.getPlacements().isEmpty());
    }

    @Test
    public void isInsideTest() {
        Board board = new PuzzleFactory().createSampleBoard();

        assertTrue(board.isInside(new Position(0, 0)));
        assertTrue(board.isInside(new Position(2, 3)));
        assertFalse(board.isInside(new Position(-1, 0)));
        assertFalse(board.isInside(new Position(3, 0)));
        assertFalse(board.isInside(new Position(0, 4)));
    }
}
