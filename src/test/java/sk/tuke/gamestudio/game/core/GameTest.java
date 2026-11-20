package sk.tuke.gamestudio.game.shikaku.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Test
    public void initialStateTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        assertEquals(GameState.NEW, game.getState());
    }

    @Test
    public void validMoveChangesStateToRunningTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        boolean result = game.placeRectangle(
                new Position(0, 0),
                new RectangleArea(new Position(0, 0), new Position(1, 1))
        );

        assertTrue(result);
        assertEquals(GameState.RUNNING, game.getState());
    }

    @Test
    public void invalidMoveReturnsFalseTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        boolean result = game.placeRectangle(
                new Position(0, 0),
                new RectangleArea(new Position(0, 0), new Position(0, 1))
        );

        assertFalse(result);
        assertEquals(GameState.NEW, game.getState());
    }

    @Test
    public void removeRectangleTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        game.placeRectangle(
                new Position(0, 0),
                new RectangleArea(new Position(0, 0), new Position(1, 1))
        );

        boolean removed = game.removeRectangle(new Position(0, 0));

        assertTrue(removed);
        assertEquals(GameState.NEW, game.getState());
    }

    @Test
    public void removeNonExistingRectangleTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        boolean removed = game.removeRectangle(new Position(0, 0));

        assertTrue(removed);
    }

    @Test
    public void solvedStateTest() {
        Game game = new Game(new PuzzleFactory().createSampleBoard());

        game.placeRectangle(new Position(0, 0),
                new RectangleArea(new Position(0, 0), new Position(1, 1)));
        game.placeRectangle(new Position(0, 2),
                new RectangleArea(new Position(0, 2), new Position(0, 3)));
        game.placeRectangle(new Position(1, 3),
                new RectangleArea(new Position(1, 2), new Position(1, 3)));
        game.placeRectangle(new Position(2, 0),
                new RectangleArea(new Position(2, 0), new Position(2, 1)));
        game.placeRectangle(new Position(2, 2),
                new RectangleArea(new Position(2, 2), new Position(2, 3)));

        assertEquals(GameState.SOLVED, game.getState());
    }
}