package sk.tuke.gamestudio.game.shikaku.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameRulesTest {

    @Test
    public void rectangleCorrectTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rect = new RectangleArea(new Position(0, 0), new Position(1, 1));

        assertTrue(rules.canPlace(board, clue, rect));
    }

    @Test
    public void wrongAreaTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rect = new RectangleArea(new Position(0, 0), new Position(0, 1));

        assertFalse(rules.canPlace(board, clue, rect));
    }

    @Test
    public void rectangleDoesNotContainClueTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rect = new RectangleArea(new Position(1, 0), new Position(2, 1));

        assertFalse(rules.canPlace(board, clue, rect));
    }

    @Test
    public void rectangleContainsAnotherClueTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        Clue clue = board.findClueAt(new Position(0, 0));
        RectangleArea rect = new RectangleArea(new Position(0, 0), new Position(0, 3));

        assertFalse(rules.canPlace(board, clue, rect));
    }

    @Test
    public void overlapTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        Clue firstClue = board.findClueAt(new Position(0, 0));
        RectangleArea firstRect = new RectangleArea(new Position(0, 0), new Position(1, 1));
        board.setPlacement(firstClue, firstRect);

        Clue secondClue = board.findClueAt(new Position(2, 0));
        RectangleArea secondRect = new RectangleArea(new Position(1, 0), new Position(2, 0));

        assertFalse(rules.canPlace(board, secondClue, secondRect));
    }

    @Test
    public void notSolvedTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        assertFalse(rules.isSolved(board));
    }

    @Test
    public void solvedTest() {
        Board board = new PuzzleFactory().createSampleBoard();
        GameRules rules = new GameRules();

        board.setPlacement(board.findClueAt(new Position(0, 0)),
                new RectangleArea(new Position(0, 0), new Position(1, 1)));
        board.setPlacement(board.findClueAt(new Position(0, 2)),
                new RectangleArea(new Position(0, 2), new Position(0, 3)));
        board.setPlacement(board.findClueAt(new Position(1, 3)),
                new RectangleArea(new Position(1, 2), new Position(1, 3)));
        board.setPlacement(board.findClueAt(new Position(2, 0)),
                new RectangleArea(new Position(2, 0), new Position(2, 1)));
        board.setPlacement(board.findClueAt(new Position(2, 2)),
                new RectangleArea(new Position(2, 2), new Position(2, 3)));

        assertTrue(rules.isSolved(board));
    }
}
